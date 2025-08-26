package org.stefan.backend.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.stefan.backend.dto.PortfolioDto;
import org.stefan.backend.dto.PortfolioRequestDto;

import java.math.BigDecimal;

@Repository
public class PortfolioRepository {

    private final JdbcTemplate jdbcTemplate;

    public PortfolioRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public PortfolioDto getPortfolioById(Long id) {
        String sql = "SELECT * FROM PORTFOLIO WHERE id = ?";

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            PortfolioDto dto = new PortfolioDto();
            dto.setId(rs.getLong("id"));
            dto.setBalance(rs.getBigDecimal("balance"));
            dto.setProfit(rs.getDouble("profit"));
            dto.setQuantity(rs.getInt("quantity"));
            return dto;
        }, id);
    }

    public Double getProfit(Long id) {
        String sql = "SELECT profit FROM PORTFOLIO WHERE id = ?";

        return jdbcTemplate.queryForObject(sql , Double.class , id);
    }

    public void updatePortfolio(BigDecimal balance , Double profit , int quantity , Long id) {

        String sql = "UPDATE PORTFOLIO SET balance = ?, profit = profit + ? , quantity = ? , id = ?";

        int rowsAffected = jdbcTemplate.update(sql , balance , profit , quantity , id);
        if (rowsAffected == 0) {
            throw new IllegalArgumentException("Portfolio with id " + id + " does not exist.");
        }
    }
}
