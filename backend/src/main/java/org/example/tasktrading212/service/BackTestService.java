package org.example.tasktrading212.service;

import org.example.tasktrading212.dto.BacktestResult;
import org.example.tasktrading212.dto.PricePoint;
import org.example.tasktrading212.model.BacktestPeriod;
import org.example.tasktrading212.model.TradeSignal;
import org.example.tasktrading212.model.TradeType;
import org.example.tasktrading212.model.TrainingTrade;
import org.example.tasktrading212.repository.TrainingTradeRepository;
import org.example.tasktrading212.strategy.StrategyFactory;
import org.example.tasktrading212.strategy.StrategyType;
import org.example.tasktrading212.strategy.TradingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class BackTestService {

    private static final Logger logger = LoggerFactory.getLogger(BackTestService.class);
    private static final String SYMBOL = "BTCUSDT";

    private final BinanceHistoryClient historyClient;
    private final StrategyFactory strategyFactory;
    private final TrainingTradeRepository trainingTradeRepository;

    public BackTestService(BinanceHistoryClient historyClient, StrategyFactory strategyFactory,
            TrainingTradeRepository trainingTradeRepository) {
        this.historyClient = historyClient;
        this.strategyFactory = strategyFactory;
        this.trainingTradeRepository = trainingTradeRepository;
    }

    @Transactional
    public BacktestResult runBacktest(Long userId, BigDecimal initialBalance, StrategyType strategyType,
            BacktestPeriod period) {
        TradingStrategy strategy = strategyFactory.createStrategy(strategyType);
        CandleConfig config = getCandleConfig(period);

        String backtestId = UUID.randomUUID().toString();

        List<PricePoint> prices;
        try {
            prices = historyClient.getHistoricalPrices(SYMBOL, config.interval(), config.limit());
        } catch (Exception e) {
            logger.error("Failed to fetch historical prices for backtest {}: {}", backtestId, e.getMessage());
            return new BacktestResult(backtestId, 0, 0, 0, initialBalance, initialBalance, BigDecimal.ZERO, List.of());
        }

        if (prices.isEmpty()) {
            logger.warn("No price data found for the given period");
            return new BacktestResult(backtestId, 0, 0, 0, initialBalance, initialBalance, BigDecimal.ZERO, List.of());
        }

        PortfolioState state = new PortfolioState(initialBalance, BigDecimal.ZERO);
        int buyCount = 0;
        int sellCount = 0;
        List<PricePoint> portfolioOverTime = new ArrayList<>();
        portfolioOverTime.add(new PricePoint(prices.get(0).timestamp(), initialBalance));

        for (PricePoint pricePoint : prices) {
            TradeSignal signal = strategy.evaluate(pricePoint.price(), state.usdt(), state.btc());

            switch (signal) {
                case BUY -> {
                    if (state.usdt().compareTo(BigDecimal.ZERO) > 0) {
                        state = executeBuy(state, pricePoint, userId, backtestId);
                        buyCount++;
                        BigDecimal value = state.usdt().add(state.btc().multiply(pricePoint.price())).setScale(2, RoundingMode.HALF_UP);
                        portfolioOverTime.add(new PricePoint(pricePoint.timestamp(), value));
                    }
                }
                case SELL -> {
                    if (state.btc().compareTo(BigDecimal.ZERO) > 0) {
                        state = executeSell(state, pricePoint, userId, backtestId);
                        sellCount++;
                        BigDecimal value = state.usdt().add(state.btc().multiply(pricePoint.price())).setScale(2, RoundingMode.HALF_UP);
                        portfolioOverTime.add(new PricePoint(pricePoint.timestamp(), value));
                    }
                }
                case HOLD -> {
                }
            }
        }

        BigDecimal lastPrice = prices.get(prices.size() - 1).price();
        BigDecimal finalValue = state.usdt().add(state.btc().multiply(lastPrice));
        BigDecimal profitLoss = finalValue.subtract(initialBalance).setScale(2, RoundingMode.HALF_UP);
        portfolioOverTime.add(new PricePoint(prices.get(prices.size() - 1).timestamp(), finalValue.setScale(2, RoundingMode.HALF_UP)));

        logger.info("Backtest {} complete: {} buys, {} sells, P/L: {}", backtestId, buyCount, sellCount, profitLoss);

        return new BacktestResult(backtestId, prices.size(), buyCount, sellCount, initialBalance, finalValue,
                profitLoss, portfolioOverTime);
    }

    private record PortfolioState(BigDecimal usdt, BigDecimal btc) {
    }

    private PortfolioState executeBuy(PortfolioState state, PricePoint pricePoint, Long userId, String backtestId) {
        BigDecimal quantity = state.usdt().divide(pricePoint.price(), 8, RoundingMode.DOWN);
        BigDecimal total = quantity.multiply(pricePoint.price()).setScale(2, RoundingMode.HALF_UP);

        saveTrade(userId, backtestId, TradeType.BUY, pricePoint, quantity, total);

        return new PortfolioState(state.usdt().subtract(total), state.btc().add(quantity));
    }

    private PortfolioState executeSell(PortfolioState state, PricePoint pricePoint, Long userId, String backtestId) {
        BigDecimal total = state.btc().multiply(pricePoint.price()).setScale(2, RoundingMode.HALF_UP);

        saveTrade(userId, backtestId, TradeType.SELL, pricePoint, state.btc(), total);

        return new PortfolioState(state.usdt().add(total), BigDecimal.ZERO);
    }

    private void saveTrade(Long userId, String backtestId, TradeType type, PricePoint pricePoint, BigDecimal quantity,
            BigDecimal total) {
        TrainingTrade trade = new TrainingTrade();
        trade.setUserId(userId);
        trade.setType(type);
        trade.setSymbol(SYMBOL);
        trade.setPrice(pricePoint.price().setScale(2, RoundingMode.HALF_UP));
        trade.setQuantity(quantity);
        trade.setTotal(total);
        trade.setTimestamp(pricePoint.timestamp());
        trade.setBacktestId(backtestId);

        trainingTradeRepository.save(trade);
    }

    private record CandleConfig(String interval, int limit) {
    }

    private CandleConfig getCandleConfig(BacktestPeriod period) {
        return switch (period) {
            case LAST_10_MINUTES -> new CandleConfig("1s", 600);
            case LAST_10_HOURS -> new CandleConfig("1m", 600);
            case LAST_1_DAY -> new CandleConfig("3m", 480);
        };
    }
}
