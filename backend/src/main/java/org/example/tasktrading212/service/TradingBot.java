package org.example.tasktrading212.service;

import jakarta.annotation.PreDestroy;
import org.example.tasktrading212.model.TradeSignal;
import org.example.tasktrading212.strategy.StrategyFactory;
import org.example.tasktrading212.strategy.StrategyType;
import org.example.tasktrading212.strategy.TradingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;


@Service
public class TradingBot {

    private static final Logger logger = LoggerFactory.getLogger(TradingBot.class);
    private static final String SYMBOL = "BTCUSDT";

    private record BotState(Long userId, TradingStrategy strategy) {}

    private final TradingService tradingService;
    private final StrategyFactory strategyFactory;
    private final ScheduledExecutorService scheduler;

    private final AtomicReference<BotState> state = new AtomicReference<>();
    private final AtomicReference<BigDecimal> latestPrice = new AtomicReference<>();
    private final AtomicBoolean paused = new AtomicBoolean(false);

    public TradingBot(TradingService tradingService, StrategyFactory strategyFactory,
                      @Value("${trading.sample-interval-seconds:3}") long sampleIntervalSeconds) {
        this.tradingService = tradingService;
        this.strategyFactory = strategyFactory;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "trading-engine");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(this::evaluateLatestPrice, sampleIntervalSeconds, sampleIntervalSeconds, TimeUnit.SECONDS);
        logger.info("Trading bot initialized (sample interval: {}s)", sampleIntervalSeconds);
    }

    public void start(Long userId, StrategyType strategyType) {
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
        try {
            TradingStrategy strategy = strategyFactory.createStrategy(strategyType);
            state.set(new BotState(userId, strategy));
            logger.info("Trading bot STARTED for user {} with strategy {}", userId, strategyType);
        } catch (Exception e) {
            logger.error("Trading bot couldn't start: {}", e.getMessage());
            state.set(null);
        }
    }

    public void stop(Long userId) {
        BotState current = state.get();
        if (current == null) {
            return;
        }
        if (!current.userId().equals(userId)) {
            throw new IllegalStateException("Cannot stop another user's bot");
        }
        state.set(null);
        paused.set(false);
        logger.info("Trading bot STOPPED for user {}", userId);
    }

    public void pause() {
            paused.set(true);
            logger.info("Trading bot PAUSED");
    }

    public void resume() {
        paused.set(false);
        logger.info("Trading bot RESUMED");
    }

    public boolean isRunning() {
        return state.get() != null;
    }

    public boolean isPaused() {
        return paused.get();
    }

    public void onPriceUpdate(BigDecimal price) {
        latestPrice.set(price);
    }

    void evaluateLatestPrice() {
        BotState current = state.get();
        if (current == null) {
            return;
        }

        if (paused.get()) {
            return;
        }

        BigDecimal price = latestPrice.get();
        if (price == null) {
            return;
        }

        try {
            TradeSignal signal = current.strategy().evaluate(
                    price,
                    tradingService.getUsdtBalance(current.userId()),
                    tradingService.getBtcHoldings(current.userId())
            );

            switch (signal) {
                case BUY -> {
                    logger.info("BUY at {}", price);
                    tradingService.executeBuy(current.userId(), SYMBOL, price);
                }
                case SELL -> {
                    logger.info("SELL at {}", price);
                    tradingService.executeSell(current.userId(), SYMBOL, price);
                }
                case HOLD -> { }
            }
        } catch (Exception e) {
            logger.error("Error processing price {}: {}", price, e.getMessage());
        }
    }

    @PreDestroy
    public void shutdown() {
        logger.info("Shutting down trading engine");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}