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
                trade.getTimestamp());
    }

    public void deleteByUserId(Long userId) {
        jdbcTemplate.update("DELETE FROM trades WHERE userid = ?", userId);
    }

    public java.util.List<Trade> findByUserId(Long userId) {
        return jdbcTemplate.query(
                "SELECT userid, type, symbol, price, quantity, total, timestamp FROM trades WHERE userid = ? ORDER BY timestamp DESC",
                (rs, rowNum) -> new Trade(
                        rs.getLong("userid"),
                        org.example.tasktrading212.model.TradeType.valueOf(rs.getString("type")),
                        rs.getString("symbol"),
                        rs.getBigDecimal("price"),
                        rs.getBigDecimal("quantity"),
                        rs.getBigDecimal("total"),
                        rs.getTimestamp("timestamp").toLocalDateTime()),
                userId);
    }

}
