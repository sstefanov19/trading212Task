package org.stefan.backend.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class Trade {

    Long id;

    LocalDateTime date;

    TradeType action;

    Double quantity;

    BigDecimal price;

    Double profit;

    String source;
}
