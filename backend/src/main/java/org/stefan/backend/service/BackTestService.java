package org.stefan.backend.service;

import org.springframework.stereotype.Service;
import org.stefan.backend.model.Candle;
import org.stefan.backend.model.TradeType;
import org.stefan.backend.repository.BalanceRepository;
import org.stefan.backend.repository.PortfolioRepository;
import org.stefan.backend.repository.TradeRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.stefan.backend.service.StrategyEvaluator.PERIOD;

@Service
public class BackTestService {

    private static final int BALANCE_ID = 2;
    private static final Long PORTFOLIO_ID = 2L;

    private final BinanceHistoryService binanceHistoryService;
    private final PortfolioService portfolioService;
    private final StrategyEvaluator strategyEvaluator;
    private final BalanceService balanceService;
    private final TradeService tradeService;

    public BackTestService(
            BinanceHistoryService binanceHistoryService,
            PortfolioService portfolioService,
            StrategyEvaluator strategyEvaluator,
            BalanceService balanceService,
            TradeService tradeService) {
        this.binanceHistoryService = binanceHistoryService;
        this.portfolioService = portfolioService;
        this.strategyEvaluator = strategyEvaluator;
        this.balanceService = balanceService;
        this.tradeService = tradeService;
    }

    public void runBackTest(String symbol) {

        tradeService.deleteAllBacktestTrades();
        BigDecimal initialBalance = new BigDecimal("100000.00");
        // Reset balance and portfolio to initial state
        balanceService.setInitialBalance(BALANCE_ID, initialBalance);
        portfolioService.setInitialPortfolio(initialBalance, 0.00, 0.00, PORTFOLIO_ID);

        BigDecimal currentBalance = balanceService.getBalanceById(BALANCE_ID);
        if (!initialBalance.equals(currentBalance)) {
            throw new IllegalStateException("Failed to reset balance");
        }

        List<Candle> candles = binanceHistoryService.getHistoricalCandles(symbol, "1h", 720);
        List<BigDecimal> historicalPrices = new ArrayList<>();

        BigDecimal position = BigDecimal.ZERO;
        BigDecimal entryPrice = BigDecimal.ZERO;

        for (Candle candle : candles) {
            BigDecimal currentPrice = BigDecimal.valueOf(candle.getClose());
            LocalDateTime tradeTime = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(candle.getOpenTime()),
                    ZoneId.systemDefault()
            );

            historicalPrices.add(currentPrice);

            if (historicalPrices.size() >= PERIOD + 1) {
                TradeType signal = strategyEvaluator.evaluate(historicalPrices, currentPrice);
                currentBalance = balanceService.getBalanceById(BALANCE_ID);

                System.out.println(signal.toString());

                try {
                    if (signal == TradeType.BUY && position.compareTo(BigDecimal.ZERO) == 0
                    && currentBalance.compareTo(BigDecimal.ZERO) > 0) {
                        System.out.println(signal);
                        position = currentBalance.divide(currentPrice, 8, RoundingMode.HALF_UP);
                        entryPrice = currentPrice;

                        tradeService.placeOrder(tradeTime, "BUY", position.doubleValue(),
                                currentPrice, 0.00, "BACKTEST");

                        balanceService.removeFromBalance(BALANCE_ID, currentBalance);
                        portfolioService.updatePortfolio(BigDecimal.ZERO, 0.00,
                                position.doubleValue(), PORTFOLIO_ID);
                        System.out.println(tradeTime + " BUY order placed: " + position + " at " + currentPrice);

                    } else if (signal == TradeType.SELL &&  position.compareTo(BigDecimal.ZERO) > 0) {
                        System.out.println(signal);
                        BigDecimal currentValue = position.multiply(currentPrice)
                                .setScale(2, RoundingMode.HALF_UP);
                        BigDecimal profitLoss = position.multiply(currentPrice.subtract(entryPrice))
                                .setScale(2, RoundingMode.HALF_UP);

                        tradeService.placeOrder(tradeTime, "SELL", position.doubleValue(),
                                currentPrice, profitLoss.doubleValue(), "BACKTEST");

                        balanceService.updateBalance(BALANCE_ID, currentValue);
                        portfolioService.updatePortfolio(currentValue, profitLoss.doubleValue(),
                                0.00, PORTFOLIO_ID);
                        position = BigDecimal.ZERO;
                        System.out.println(tradeTime + " SELL order placed: " + currentValue + " at " + currentPrice);
                    }
                } catch (Exception e) {
                    System.err.println("Failed to place order: " + e.getMessage());
                }
            }
        }
    }
}