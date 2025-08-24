package org.stefan.backend.dto;

import lombok.Getter;
import lombok.Setter;
import org.stefan.backend.model.TradeType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class TradeDto {


    LocalDateTime date;

    TradeType action;

    Integer quantity;

    BigDecimal price;

    Double profit;

    public String getActionAsString() {
        return action != null ? action.name() : null;
    }
}
