package org.example.tasktrading212.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.tasktrading212.dto.PricePoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
public class BinanceHistoryClient {

    private static final Logger logger = LoggerFactory.getLogger(BinanceHistoryClient.class);

    @Value("${binance_api_url}")
    private String KLINES_URL;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public BinanceHistoryClient() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public List<PricePoint> getHistoricalPrices(String symbol, String interval, int limit) {
        String url = String.format("%s?symbol=%s&interval=%s&limit=%d",
                KLINES_URL, symbol, interval, limit);
        return fetchPrices(symbol, url);
    }

    private List<PricePoint> fetchPrices(String symbol, String url) {
        List<PricePoint> prices = new ArrayList<>();

        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode klines = objectMapper.readTree(response);

            for (JsonNode kline : klines) {
                long timestamp = kline.get(0).asLong();
                BigDecimal closePrice = new BigDecimal(kline.get(4).asText());

                LocalDateTime dateTime = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());

                prices.add(new PricePoint(dateTime, closePrice));
            }

            logger.info("Fetched {} price points for {}", prices.size(), symbol);

        } catch (Exception e) {
            logger.error("Failed to fetch historical prices: {}", e.getMessage());
        }

        return prices;
    }
}