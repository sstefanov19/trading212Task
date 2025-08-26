package org.stefan.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.stefan.backend.model.Candle;

import java.util.ArrayList;
import java.util.List;

@Service
public class BinanceHistoryService {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String BASE_URL = "https://api.binance.com/api/v3/klines";

    public List<Candle> getHistoricalCandles(String symbol, String interval , int limit) {
        String url = String.format("%s?symbol=%s&interval=%s&limit=%d" ,
                BASE_URL,
                symbol,
                interval,
                limit);

        List<List<Object>> response = restTemplate.getForObject(url , List.class);
        List<Candle> candles = new ArrayList<>();

        if(response != null) {
            for(List<Object> c : response) {
                Candle candle = new Candle();
                candle.setOpenTime(((Number) c.get(0)).longValue());
                candle.setOpen(Double.parseDouble(c.get(1).toString()));
                candle.setHigh(Double.parseDouble(c.get(2).toString()));
                candle.setLow(Double.parseDouble(c.get(3).toString()));
                candle.setClose(Double.parseDouble(c.get(4).toString()));
                candle.setVolume(Double.parseDouble(c.get(5).toString()));
                candle.setCloseTime(((Number) c.get(6)).longValue());

                candles.add(candle);
            }
        }
        return candles;
    }
}
