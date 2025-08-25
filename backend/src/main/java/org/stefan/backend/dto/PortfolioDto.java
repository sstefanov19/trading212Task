package org.stefan.backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PortfolioDto {

    Long id;

    BigDecimal balance;

    Double profit;

    int quantity;

}
