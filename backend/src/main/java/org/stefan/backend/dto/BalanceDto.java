package org.stefan.backend.dto;

import java.math.BigDecimal;

public record BalanceDto(
        Long id,
        BigDecimal total_balance
) {
}
