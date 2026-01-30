package org.example.tasktrading212.repository;

import org.example.tasktrading212.model.TrainingTrade;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class TrainingTradeRepository {

    private final JdbcTemplate jdbcTemplate;

    public TrainingTradeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(TrainingTrade trade) {
        jdbcTemplate.update(
                "INSERT INTO training_trade (user_id, type, symbol, price, quantity, total, timestamp, backtest_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                trade.getUserId(),
                trade.getType().name(),
                trade.getSymbol(),
                trade.getPrice(),
                trade.getQuantity(),
                trade.getTotal(),
                trade.getTimestamp(),
                trade.getBacktestId()
        );
    }
}