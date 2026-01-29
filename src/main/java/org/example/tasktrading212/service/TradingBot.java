package org.example.tasktrading212.service;

import jakarta.annotation.PreDestroy;
import org.example.tasktrading212.model.TradeSignal;
import org.example.tasktrading212.strategy.TradingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class TradingBot {

    private static final Logger logger = LoggerFactory.getLogger(TradingBot.class);
    private static final String SYMBOL = "BTCUSDT";

    private final TradingService tradingService;
    private final TradingStrategy strategy;
    private final ExecutorService tradingThread;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private volatile Long userId;

    public TradingBot(TradingStrategy strategy, TradingService tradingService) {
        this.strategy = strategy;
        this.tradingService = tradingService;
        this.tradingThread = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "trading-engine");
            t.setDaemon(true);
            return t;
        });
        logger.info("Trading bot initialized (not running until started)");
    }

    public void start(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
        this.userId = userId;
        running.set(true);
        logger.info("Trading bot STARTED for user {}", userId);
    }

    public void stop() {
        running.set(false);
        logger.info("Trading bot STOPPED");
    }

    public boolean isRunning() {
        return running.get();
    }

    public void onPriceUpdate(BigDecimal price) {
        tradingThread.execute(() -> processPriceUpdate(price));

    }

    private void processPriceUpdate(BigDecimal price) {
        if (!running.get() || userId == null) {
            return;
        }
        logger.info("Price update: {}", price);
        TradeSignal signal = strategy.evaluate(
                price,
                tradingService.getUsdtBalance(userId),
                tradingService.getBtcHoldings(userId)
        );

        switch (signal) {
            case BUY -> tradingService.executeBuy(userId, SYMBOL, price);
            case SELL -> tradingService.executeSell(userId, SYMBOL, price);
            case HOLD -> { }
        }
    }

    @PreDestroy
    public void shutdown() {
        logger.info("Shutting down trading engine");
        tradingThread.shutdown();
        try {
            if (!tradingThread.awaitTermination(5, TimeUnit.SECONDS)) {
                tradingThread.shutdownNow();
            }
        } catch (InterruptedException e) {
            tradingThread.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}