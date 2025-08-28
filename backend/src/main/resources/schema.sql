-- BALANCE table
CREATE TABLE IF NOT EXISTS balance
(
    id
    SERIAL
    PRIMARY
    KEY,
    total_balance
    NUMERIC
    NOT
    NULL
);

-- PORTFOLIO table
CREATE TABLE IF NOT EXISTS portfolio
(
    id
    SERIAL
    PRIMARY
    KEY,
    balance
    NUMERIC
    NOT
    NULL,
    profit
    NUMERIC,
    quantity
    INTEGER
    NOT
    NULL
);

-- TRADES table
CREATE TABLE IF NOT EXISTS trades
(
    id
    SERIAL
    PRIMARY
    KEY,
    date
    TIMESTAMP
    WITHOUT
    TIME
    ZONE
    NOT
    NULL,
    action
    VARCHAR
(
    10
) NOT NULL, -- BUY / SELL
    quantity NUMERIC NOT NULL,
    price NUMERIC NOT NULL,
    profit NUMERIC,
    source VARCHAR
(
    255
) -- LIVE / BACKTEST
    );
