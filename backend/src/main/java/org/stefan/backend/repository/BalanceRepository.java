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

    public BigDecimal getBalanceByIdFromDB(int id) {
        String sql = "SELECT total_balance FROM BALANCE WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, BigDecimal.class  , id);
    }

    public void insertBalance(BigDecimal balance) {
        String sql = "INSERT INTO BALANCE (total_balance) VALUES(?)";

        jdbcTemplate.update(sql , balance);
    }

    public void removeFromBalance(BigDecimal balance, int id ) {
        String sql = "UPDATE BALANCE SET total_balance = total_balance - ? WHERE id = ?";

        jdbcTemplate.update(sql  , balance , id);
    }


    public int updateBalance( BigDecimal balance , int id) {
        String sql = "UPDATE BALANCE SET total_balance = total_balance + ? WHERE id = ?";
        return jdbcTemplate.update(sql, balance, id);
    }
}
