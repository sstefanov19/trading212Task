package org.stefan.backend.repository;


import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public class BalanceRepository {

    private final JdbcTemplate jdbcTemplate;

    public BalanceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insertBalance(BigDecimal balance) {
        String sql = "INSERT INTO BALANCE (total_balance) VALUES(?)";

        jdbcTemplate.update(sql , balance);
    }

    public int updateBalance(Long id , BigDecimal balance) {
        String sql = "UPDATE BALANCE SET total_balance = total_balance + ? WHERE id = ?";
        return jdbcTemplate.update(sql, balance, id);
    }
}
