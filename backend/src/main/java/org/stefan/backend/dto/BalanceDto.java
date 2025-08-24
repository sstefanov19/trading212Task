package org.stefan.backend.dto;

import java.math.BigDecimal;

public record BalanceDto(
        int id,
        BigDecimal total_balance
) {
}
