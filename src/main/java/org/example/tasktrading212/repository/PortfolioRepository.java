package org.example.tasktrading212.repository;

import org.example.tasktrading212.model.Portfolio;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

@Repository
public class PortfolioRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Portfolio> portfolioRowMapper = (rs, rowNum) -> new Portfolio(
            rs.getLong("id"),
            rs.getLong("user_id"),
            rs.getBigDecimal("usdt_balance"),
            rs.getBigDecimal("btc_holdings"),
            rs.getBigDecimal("initial_balance")
    );

    public PortfolioRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Portfolio save(Portfolio portfolio) {
        if (portfolio.getId() == null) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO portfolio (user_id, usdt_balance, btc_holdings, initial_balance) VALUES (?, ?, ?, ?)",
                        new String[]{"id"}
                );
                ps.setLong(1, portfolio.getUserId());
                ps.setBigDecimal(2, portfolio.getUsdtBalance());
                ps.setBigDecimal(3, portfolio.getBtcHoldings());
                ps.setBigDecimal(4, portfolio.getInitialBalance());
                return ps;
            }, keyHolder);
            portfolio.setId(keyHolder.getKey().longValue());
        } else {
            jdbcTemplate.update(
                    "UPDATE portfolio SET usdt_balance = ?, btc_holdings = ? WHERE id = ?",
                    portfolio.getUsdtBalance(), portfolio.getBtcHoldings(), portfolio.getId()
            );
        }
        return portfolio;
    }

    public Optional<Portfolio> findByUserId(Long userId) {
        List<Portfolio> portfolios = jdbcTemplate.query(
                "SELECT * FROM portfolio WHERE user_id = ?",
                portfolioRowMapper,
                userId
        );
        return portfolios.isEmpty() ? Optional.empty() : Optional.of(portfolios.get(0));
    }

    public void updateBalancesByUserId(Long user_id, BigDecimal usdtBalance, BigDecimal btcHoldings) {
        jdbcTemplate.update(
                "UPDATE portfolio SET usdt_balance = ?, btc_holdings = ? WHERE user_id = ?",
                usdtBalance, btcHoldings, user_id
        );
    }
}