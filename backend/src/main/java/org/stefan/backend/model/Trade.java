package org.stefan.backend.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.cglib.core.Local;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class Trade {

    Long id;

    LocalDateTime date;

    TradeType action;

    Integer quantity;

    BigDecimal price;

    Double profit;

}
