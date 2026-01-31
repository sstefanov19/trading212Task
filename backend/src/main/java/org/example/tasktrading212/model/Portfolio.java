package org.example.tasktrading212.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Portfolio {

    private Long id;
    private Long userId;
    private BigDecimal usdtBalance;
    private BigDecimal btcHoldings;
    private BigDecimal initialBalance;

    public Portfolio(Long userId, BigDecimal initialBalance) {
        this.userId = userId;
        this.initialBalance = initialBalance;
        this.usdtBalance = initialBalance;
        this.btcHoldings = BigDecimal.ZERO;
    }
}