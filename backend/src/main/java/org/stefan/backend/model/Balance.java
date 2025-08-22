package org.stefan.backend.model;

import lombok.Getter;
import lombok.Setter;


import java.math.BigDecimal;


@Getter
@Setter
public class Balance {


    private Long id;
    private BigDecimal balance;

    public Balance(Long id , BigDecimal balance) {
        this.id = id;
        this.balance = balance;
    }
}
