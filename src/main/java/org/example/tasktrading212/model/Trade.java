package org.example.tasktrading212.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Setter
public class Trade {
       private Long userId;
       private TradeType type;
       private String symbol;
       private BigDecimal price;
       private BigDecimal quantity;
       private BigDecimal total;
       private LocalDateTime timestamp;
}