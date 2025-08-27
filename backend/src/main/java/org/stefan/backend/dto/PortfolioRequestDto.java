package org.stefan.backend.dto;

import java.math.BigDecimal;

public record PortfolioRequestDto(
        BigDecimal balance,
        Double profit,
        Double quantity
) {
}
