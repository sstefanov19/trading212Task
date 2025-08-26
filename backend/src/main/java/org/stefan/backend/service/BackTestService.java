package org.stefan.backend.service;

import org.springframework.stereotype.Service;
import org.stefan.backend.model.Candle;
import org.stefan.backend.model.TradeType;

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

    private final BinanceHistoryService binanceHistoryService;
    private final StrategyEvaluator strategyEvaluator;
    private final BalanceService balanceService;
    private final TradeService tradeService;

    public BackTestService(
            BinanceHistoryService binanceHistoryService,
            StrategyEvaluator strategyEvaluator,
            BalanceService balanceService,
            TradeService tradeService
    ) {
        this.binanceHistoryService = binanceHistoryService;
        this.strategyEvaluator = strategyEvaluator;
        this.balanceService = balanceService;
        this.tradeService = tradeService;
    }

    public void runBackTest(String symbol) {
        List<Candle> candles = binanceHistoryService.getHistoricalCandles(symbol, "1h", 60);
        List<BigDecimal> historicalPrices = new ArrayList<>();
        BigDecimal balance = balanceService.getBalanceById(2);
        BigDecimal initialBalance = balance; // Store initial balance
        BigDecimal position = BigDecimal.ZERO;
        BigDecimal entryPrice = BigDecimal.ZERO; // Store entry price for P/L calculation


        for(Candle candle: candles) {
            BigDecimal currentPrice = BigDecimal.valueOf(candle.getClose());
            historicalPrices.add(currentPrice); // Add new price to history
            LocalDateTime tradeTime = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(candle.getOpenTime()),
                    ZoneId.systemDefault()
            );

            System.out.println(tradeTime);

            // Only evaluate once we have enough data
            if (historicalPrices.size() > PERIOD + 1) {
                TradeType signal = strategyEvaluator.evaluate(historicalPrices, currentPrice);

                switch (signal) {
                    case BUY -> {
                        if (balance.compareTo(BigDecimal.ZERO) > 0) {
                            position = balance.divide(currentPrice, 8, RoundingMode.HALF_UP);
                            entryPrice = currentPrice; // Store entry price
                            balance = BigDecimal.ZERO;

                            tradeService.placeOrder(tradeTime , String.valueOf(TradeType.BUY), position.intValue(), currentPrice , 0.00 , "BACKTEST");
                            System.out.println("Bought " + position + " at " + currentPrice);
                        }
                    }
                    case SELL -> {
                        if (position.compareTo(BigDecimal.ZERO) > 0) {
                            BigDecimal currentValue = position.multiply(currentPrice);
                            // Current value - previous value (which was the initial balance)
                            BigDecimal profitLoss = position.multiply(currentPrice.subtract(entryPrice)).setScale(2, RoundingMode.HALF_UP);


                            tradeService.placeOrder(
                                    tradeTime,
                                    String.valueOf(TradeType.SELL),
                                    position.intValue(),
                                    currentPrice,
                                    profitLoss.doubleValue(),
                                    "BACKTEST"
                            );

                            balance = currentValue;
                            position = BigDecimal.ZERO;

                            System.out.println("Sold for " + balance + " at " + currentPrice
                                    + " (P/L: " + profitLoss + ")");
                        }
                    }
                }
            }
        }
    }
}
