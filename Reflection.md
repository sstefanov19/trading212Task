# Reflection

**Solution and Key Design decision**

The trading bot works with two modes (trading and training). For the prices we used **Binance** because of the better rate limits compared to other platforms. For live trading we used Websocket streaming, and for historical date we used REST.

Design decision storing data in relational database **Postgres** to ensure ACID transactions.

The frontend and backend are fully decoupled — a React single-page application communicates with the Spring Boot backend through a RESTful API secured with JWT authentication. The backend acts as the single point of integration
with external services (Binance WebSocket for live prices, Binance REST API for historical data), keeping all trading logic and data persistence server-side while the frontend handles presentation and user interaction.

+ **Pros:** Clean separation of concerns with independently deployable, testable layers where the backend acts as a single secure gateway to external services.
+ **Cons:** Added complexity of maintaining two separate applications with CORS configuration and HTTP overhead between them.

Another key design decision was offloading all trading operations onto a single-threaded scheduled executor. When market data arrives through the WebSocket, the main thread stores the latest price and returns immediately —
keeping it free to handle HTTP requests. A dedicated executor thread then picks up the work: evaluating the strategy, executing trades, updating the database, and logging results.

+ **Pros:** Guarantees strict event ordering, eliminates race conditions without explicit through immutable state and atomic operations, keeps the main thread responsive, and simplifies debugging with a linear execution trace.

+ **Cons:** Lower throughput than a parallelized design, though negligible at the current scale since strategy evaluation is lightweight arithmetic.

The bot supports two strategies — **Moving Average Crossover**, which detects momentum shifts by comparing short and long-period moving averages, and **Mean Reversion**, which trades on the assumption that price reverts to its mean when
it deviates beyond a configurable threshold — the focus was on development and correctness rather than maximize profit.

External tools or  AI was used mainly to develop boilerplate code for Websockets and extend on different trading strategies , also used it to validate edge cases and help me find fitting React components for the frontend and verified the generated code through manual testing and edge case validation.

What I would like to add if I had more time
+ More integration tests
+ Explore more trading strategies, maybe try the algorithmic trading strategy
+ Think of how we can scale to more tickers
+ Polish the UI more
