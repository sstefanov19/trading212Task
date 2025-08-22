package org.stefan.backend.dto;

import java.math.BigDecimal;

public record BalanceRequest(
        BigDecimal total_balance
) {
}
