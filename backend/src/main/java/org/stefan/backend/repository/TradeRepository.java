package org.stefan.backend.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.stefan.backend.dto.TradeDto;

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
}
