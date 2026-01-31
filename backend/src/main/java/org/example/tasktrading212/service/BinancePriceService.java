package org.example.tasktrading212.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BinancePriceService {

    private static final Logger logger = LoggerFactory.getLogger(BinancePriceService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String BINANCE_WS_URL = "wss://stream.binance.com:9443/ws/";
    private static final String DEFAULT_SYMBOL = "btcusdt";

    private final ConcurrentHashMap<String, BigDecimal> currentPrices = new ConcurrentHashMap<>();
    private final HttpClient httpClient;
    private final TradingBot tradingBot;
    private WebSocket webSocket;

    public BinancePriceService(@Lazy TradingBot tradingBot) {
        this.httpClient = HttpClient.newHttpClient();
        this.tradingBot = tradingBot;
    }

    @PostConstruct
    public void connect() {
        connectToStream(DEFAULT_SYMBOL);
    }

    public void connectToStream(String symbol) {
        String streamUrl = BINANCE_WS_URL + symbol.toLowerCase() + "@trade";
        logger.info("Connecting to Binance WebSocket: {}", streamUrl);

        httpClient.newWebSocketBuilder()
                .buildAsync(URI.create(streamUrl), new WebSocket.Listener() {
                    private final StringBuilder messageBuffer = new StringBuilder();

                    @Override
                    public void onOpen(WebSocket webSocket) {
                        logger.info("Connected to Binance WebSocket");
                        WebSocket.Listener.super.onOpen(webSocket);
                    }

                    @Override
                    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                        messageBuffer.append(data);
                        if (last) {
                            processMessage(messageBuffer.toString());
                            messageBuffer.setLength(0);
                        }
                        return WebSocket.Listener.super.onText(webSocket, data, last);
                    }

                    @Override
                    public void onError(WebSocket webSocket, Throwable error) {
                        logger.error("WebSocket error: {}", error.getMessage());
                    }

                    @Override
                    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
                        logger.info("Disconnected from Binance WebSocket: {} - {}", statusCode, reason);
                        return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
                    }
                })
                .thenAccept(ws -> {
                    this.webSocket = ws;
                    logger.info("Successfully connected to Binance price stream for {}", symbol.toUpperCase());
                })
                .exceptionally(e -> {
                    logger.error("Failed to connect to Binance WebSocket: {}", e.getMessage());
                    return null;
                });
    }

    private void processMessage(String message) {
        try {
            JsonNode json = objectMapper.readTree(message);
            String symbol = json.get("s").asText();
            BigDecimal price = new BigDecimal(json.get("p").asText());

            currentPrices.put(symbol, price);

            if (tradingBot != null) {
                tradingBot.onPriceUpdate(price);
            }
        } catch (Exception e) {
            logger.error("Error parsing price message: {}", e.getMessage());
        }
    }

    public BigDecimal getCurrentPrice() {
        return currentPrices.getOrDefault(DEFAULT_SYMBOL.toUpperCase(), BigDecimal.ZERO);
    }

    @PreDestroy
    public void disconnect() {
        if (webSocket != null) {
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Shutting down");
            logger.info("Disconnected from Binance WebSocket");
        }
    }
}
