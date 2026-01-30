CREATE TABLE portfolios (
    id SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    usdt_balance DECIMAL(20, 2) NOT NULL DEFAULT 0,
    btc_holdings DECIMAL(20, 2) NOT NULL DEFAULT 0,
    initial_balance DECIMAL(20, 2) NOT NULL,
    UNIQUE(user_id)
);

CREATE TABLE trades(
    id SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL
    type VARCHAR(10) NOT NULL,
    symbol VARCHAR(20) NOT NULL,
    price NUMERIC(20, 2) NOT NULL,
    quantity NUMERIC(20,2) NOT NULL,
    total NUMERIC(20,2) NOT NULL,
    timestamp TIMESTAMP WITHOUT TIME ZONE,
    FOREIGN KEY (user_id) REFERENCES users(id)
);


CREATE TABLE training_trade(
    id SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(10) NOT NULL,
    symbol VARCHAR(20) NOT NULL,
    price NUMERIC(20,2) NOT NULL,
    quantity NUMERIC(20,2) NOT NULL,
    total NUMERIC(20,2) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    backtest_id VARCHAR(36),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(20) NOT NULL,
    username VARCHAR(20) NOT NULL,
    password VARCHAR(255) NOT NULL
);