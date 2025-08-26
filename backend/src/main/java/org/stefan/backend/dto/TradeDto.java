package org.stefan.backend.dto;

import lombok.Getter;
import lombok.Setter;
import org.stefan.backend.model.TradeType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class TradeDto {


    private LocalDateTime date;

    private TradeType action;

    private Integer quantity;

    private BigDecimal price;

    private Double profit;

    private String source;

    public String getActionAsString() {
        return action != null ? action.name() : null;
    }

}
