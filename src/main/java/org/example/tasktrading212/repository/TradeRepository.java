package org.example.tasktrading212.repository;

import org.example.tasktrading212.model.Trade;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class TradeRepository {

    private final JdbcTemplate jdbcTemplate;

    public TradeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    public void saveTrade(Trade trade) {
        jdbcTemplate.update(
                "INSERT INTO trades (userid, type, symbol, price, quantity, total, timestamp) VALUES (?,?,?,?,?,?,?)",
                trade.getUserId(),
                trade.getType().name(),
                trade.getSymbol(),
                trade.getPrice(),
                trade.getQuantity(),
                trade.getTotal(),
                trade.getTimestamp()
        );
    }



}
