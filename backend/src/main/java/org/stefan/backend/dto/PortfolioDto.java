package org.stefan.backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PortfolioDto {

    private Long id;

    private BigDecimal balance;

    private Double profit;

    private Double quantity;

}
