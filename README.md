# Trading212 Task

This project implements a simple crypto trading bot with **two modes**:
- **Training mode** (backtesting on historical Binance data)
- **Trading mode** (live trading on Binance WebSocket data)

It includes:
- **Backend:** Java 21 + Spring Boot, REST APIs, raw SQL via `JdbcTemplate`
- **Frontend:** React + Vite, TypeScript, interactive dashboard

---

## 🚀 How to Run

### Prerequisites
- Java 21
- Maven
- Node.js + npm
- PostgresSQL

### Database Setup
1. Create a database:
   ```sql
   CREATE DATABASE trading_bot;

Spring Boot will auto-initialize tables using schema.sql.

### Backend Setup
cd backend
mvn spring-boot:run


Backend runs at: http://localhost:8080

### Frontend Setup
cd frontend
npm install
npm run dev


Frontend runs at: http://localhost:5173

📡 API Endpoints

POST /trade/start

POST /trade/stop

GET /portfolio

GET /balance

POST /backtest/{ticker}

📊 Features

Real-time prices (Binance WebSocket)

Backtesting with historical candles

Portfolio and balance tracking

Trade history with profit/loss

Frontend dashboard with:

Start/pause/reset

Portfolio overview

Trade history table

Price chart with buy/sell markers
