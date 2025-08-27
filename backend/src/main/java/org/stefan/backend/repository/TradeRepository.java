package org.stefan.backend.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.stefan.backend.dto.TradeDto;
import org.stefan.backend.model.Trade;
import org.stefan.backend.model.TradeType;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Repository
public class TradeRepository {

    private final JdbcTemplate jdbcTemplate;

    public TradeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(TradeDto trade) {
        String sql =
                """
                        INSERT INTO TRADES (date, action , quantity , price , profit , source)
                        VALUES (? , ? ,? ,? ,? , ?)
                """;

                jdbcTemplate.update(sql,
                        Timestamp.valueOf(trade.getDate()),
                        trade.getActionAsString(),
                        trade.getQuantity(),
                        trade.getPrice(),
                        trade.getProfit(),
                        trade.getSource()
                        );
    }

    public Trade findTopByActionOrderByDateDesc(TradeType action) {
        String sql = "SELECT * FROM TRADES WHERE action = ? ORDER BY date DESC LIMIT 1";

        try {
            return jdbcTemplate.queryForObject(sql,
                    (rs, rowNum) -> {
                        Trade trade = new Trade();
                        trade.setId(rs.getLong("id"));
                        trade.setDate(rs.getTimestamp("date").toLocalDateTime());
                        trade.setAction(TradeType.valueOf(rs.getString("action")));
                        trade.setQuantity(rs.getInt("quantity"));
                        trade.setPrice(new BigDecimal(rs.getString("price")));
                        trade.setProfit(rs.getDouble("profit"));
                        trade.setSource(rs.getString("source"));
                        return trade;
                    },
                    action.toString());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}
