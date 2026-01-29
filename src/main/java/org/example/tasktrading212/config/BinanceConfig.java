package org.example.tasktrading212.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BinanceConfig {

    @Value("${BINANCE_API_KEY}")
    private String apiKey;

    @Value("${BINANCE_SECRET_KEY}")
    private String secretKey;

    public String getApiKey() {
        return apiKey;
    }

    public String getSecretKey() {
        return secretKey;
    }
}