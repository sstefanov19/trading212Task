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

    private final TradingService tradingService;
    private final StrategyFactory strategyFactory;
    private final ScheduledExecutorService scheduler;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicReference<BigDecimal> latestPrice = new AtomicReference<>();
    private volatile Long userId;
    private volatile TradingStrategy strategy;

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
        this.userId = userId;
        this.strategy = strategyFactory.createStrategy(strategyType);
        running.set(true);
        logger.info("Trading bot STARTED for user {} with strategy {}", userId, strategyType);
    }

    public void stop() {
        running.set(false);
        logger.info("Trading bot STOPPED");
    }

    public boolean isRunning() {
        return running.get();
    }

    public void onPriceUpdate(BigDecimal price) {
        latestPrice.set(price);
    }

    void evaluateLatestPrice() {
        if (!running.get() || userId == null) {
            return;
        }

        BigDecimal price = latestPrice.get();
        if (price == null) {
            return;
        }

        try {
            TradeSignal signal = strategy.evaluate(
                    price,
                    tradingService.getUsdtBalance(userId),
                    tradingService.getBtcHoldings(userId)
            );

            switch (signal) {
                case BUY -> {
                    logger.info("BUY at {}", price);
                    tradingService.executeBuy(userId, SYMBOL, price);
                }
                case SELL -> {
                    logger.info("SELL at {}", price);
                    tradingService.executeSell(userId, SYMBOL, price);
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