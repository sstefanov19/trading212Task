# Auto Trading Bot

Crypto trading bot simulator for BTCUSDT. Hooks into Binance's live WebSocket feed for real-time prices or runs backtests on historical data.

## What you need

- Java 21
- Node 18+
- PostgreSQL 16 (or just use Docker)
- Maven wrapper is included

## Quick start

**1. Spin up the database**

```bash
docker run -d \
  --name trading-postgres \
  -e POSTGRES_DB=trading212database \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=740420 \
  -p 5433:5432 \
  postgres:16
```

**2. Start the backend**

```bash
cd backend
./mvnw spring-boot:run
```

Runs on `localhost:8080`. Schema gets applied automatically on startup.

**3. Start the frontend**

```bash
cd frontend
npm install
npm run dev
```

Open `localhost:5173` and you're good to go.

## How it works

1. Register an account on the login page
2. Pick a strategy (Moving Average or Mean Reversion)
3. Hit Start - the bot connects to Binance and trades on its own
4. Watch your portfolio value change in real-time
5. Check the trade history to see what it's doing

There's also a backtest mode if you want to test strategies against historical data before going live.

## Strategies

**Moving Average Crossover** - Uses 5 and 20 period MAs. Buys when short crosses above long, sells on the flip.

**Mean Reversion** - Looks at the last 20 prices. Buys when price drops 3%+ below mean, sells when it's 5%+ above.

## Config

Tweak these in `backend/src/main/resources/application.properties`:

| Setting | Default | What it does |
|---------|---------|--------------|
| `trading.ma.short-period` | 5 | Short MA window |
| `trading.ma.long-period` | 20 | Long MA window |
| `trading.mr.period` | 20 | Mean reversion lookback |
| `trading.mr.buy-threshold` | 0.03 | Buy when 3% below mean |
| `trading.mr.sell-threshold` | 0.05 | Sell when 5% above mean |
| `trading.sample-interval-seconds` | 3 | How often bot checks prices |

## Data source

All prices come from Binance public APIs - no API key needed:
- Live: `wss://stream.binance.com:9443/ws/btcusdt@trade`
- Historical: `https://api.binance.com/api/v3/klines`

## Tests

```bash
cd backend
./mvnw test
```

62 tests covering the trading logic, strategies, and services.

## Stack

- Backend: Spring Boot 4.0.2, Java 21, JWT auth
- Frontend: React 19, Vite, Recharts
- Database: PostgreSQL with plain JDBC (no ORM)
