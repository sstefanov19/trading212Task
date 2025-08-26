package org.stefan.backend.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Candle {

    private long openTime;

    private double open;
    private double high;
    private double low;
    private double close;
    private double volume;
    private double closeTime;
}
