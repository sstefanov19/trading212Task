import { useState, useEffect, useCallback } from 'react';
import { trading, portfolio, backtest } from '../services/api.js';

/**
 * Custom hook for managing trading bot operations
 * Handles loading states, error handling, and data fetching
 */
export function useTrading() {
  const [botStatus, setBotStatus] = useState({ running: false, paused: false });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchStatus = useCallback(async () => {
    try {
      const response = await trading.status();
      setBotStatus(response.data);
    } catch (err) {
      console.error('Failed to fetch status:', err);
      setError(err.message);
    }
  }, []);

  const withLoading = useCallback(async (asyncFn) => {
    setLoading(true);
    setError(null);
    try {
      await asyncFn();
    } catch (err) {
      console.error('Operation failed:', err);
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, []);

  const start = useCallback((strategy) => withLoading(async () => {
    await trading.start(strategy);
    await fetchStatus();
  }), [withLoading, fetchStatus]);

  const stop = useCallback(() => withLoading(async () => {
    await trading.stop();
    await fetchStatus();
  }), [withLoading, fetchStatus]);

  const pause = useCallback(() => withLoading(async () => {
    await trading.pause();
    await fetchStatus();
  }), [withLoading, fetchStatus]);

  const resume = useCallback(() => withLoading(async () => {
    await trading.resume();
    await fetchStatus();
  }), [withLoading, fetchStatus]);

  const reset = useCallback(() => withLoading(async () => {
    await trading.reset();
    await fetchStatus();
  }), [withLoading, fetchStatus]);

  useEffect(() => {
    fetchStatus();
  }, [fetchStatus]);

  return {
    botStatus,
    loading,
    error,
    start,
    stop,
    pause,
    resume,
    reset,
    refresh: fetchStatus,
  };
}

/**
 * Custom hook for portfolio data and chart
 */
export function usePortfolio(isPolling = false) {
  const [portfolioData, setPortfolioData] = useState({});
  const [chartData, setChartData] = useState([]);
  const [loading, setLoading] = useState(false);

  const fetchPortfolio = useCallback(async () => {
    try {
      const response = await portfolio.get();
      setPortfolioData(response.data);
    } catch (error) {
      console.error('Failed to fetch portfolio:', error);
    }
  }, []);

  const fetchChartData = useCallback(async () => {
    setLoading(true);
    try {
      const [tradesRes, portfolioRes] = await Promise.all([
        trading.getAll(),
        portfolio.get(),
      ]);

      const trades = tradesRes.data;
      const pData = portfolioRes.data;
      const initialBalance = Number(pData.initialBalance) || 1000;
      const currentValue = Number(pData.currentValue) || initialBalance;
      const btcHoldingsNow = Number(pData.btcHoldings) || 0;
      const usdtBalanceNow = Number(pData.usdtBalance) || 0;

      const currentBtcPrice = btcHoldingsNow > 0
        ? (currentValue - usdtBalanceNow) / btcHoldingsNow
        : (trades.length > 0 ? Number(trades[trades.length - 1].price) : 0);

      if (trades.length === 0) {
        setChartData([{ time: 'Start', value: initialBalance }]);
        setLoading(false);
        return;
      }

      const sortedTrades = [...trades].sort((a, b) =>
        new Date(a.timestamp).getTime() - new Date(b.timestamp).getTime()
      );

      const points = [{ time: 'Start', value: initialBalance }];
      let usdtBalance = initialBalance;
      let btcHoldings = 0;

      sortedTrades.forEach((trade) => {
        const quantity = Number(trade.quantity);
        const total = Number(trade.total);

        if (trade.type === 'BUY') {
          usdtBalance -= total;
          btcHoldings += quantity;
        } else {
          usdtBalance += total;
          btcHoldings -= quantity;
        }

        const portfolioValue = usdtBalance + (btcHoldings * currentBtcPrice);
        const date = new Date(trade.timestamp);
        const time = date.toLocaleDateString([], { month: 'short', day: 'numeric' }) + ' ' +
                     date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
        points.push({ time, value: parseFloat(portfolioValue.toFixed(2)) });
      });

      if (points.length > 1) {
        points[points.length - 1].value = currentValue;
      }

      setChartData(points);
    } catch (error) {
      console.error('Failed to fetch chart data:', error);
    } finally {
      setLoading(false);
    }
  }, []);

  const refreshAll = useCallback(async () => {
    await Promise.all([fetchPortfolio(), fetchChartData()]);
  }, [fetchPortfolio, fetchChartData]);

  useEffect(() => {
    fetchPortfolio();
    fetchChartData();
  }, [fetchPortfolio, fetchChartData]);

  useEffect(() => {
    if (!isPolling) return;
    const interval = setInterval(() => {
      refreshAll();
    }, 5000);
    return () => clearInterval(interval);
  }, [isPolling, refreshAll]);

  return {
    portfolioData,
    chartData,
    loading,
    refresh: refreshAll,
    refreshPortfolio: fetchPortfolio,
    refreshChart: fetchChartData,
  };
}

/**
 * Custom hook for backtest operations
 */
export function useBacktest() {
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const run = useCallback(async (initialBalance, strategy, period) => {
    setLoading(true);
    setError(null);
    try {
      const response = await backtest.run(initialBalance, strategy, period);
      setResult(response.data);
      return response.data;
    } catch (err) {
      console.error('Backtest failed:', err);
      setError(err.message || 'Backtest failed');
      return null;
    } finally {
      setLoading(false);
    }
  }, []);

  const clear = useCallback(() => {
    setResult(null);
    setError(null);
  }, []);

  return {
    result,
    loading,
    error,
    run,
    clear,
  };
}

/**
 * Custom hook for trade history
 */
export function useTrades(isPolling = false) {
  const [trades, setTrades] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchTrades = useCallback(async () => {
    setLoading(true);
    try {
      const response = await trading.getAll();
      setTrades(response.data || []);
    } catch (err) {
      console.error('Failed to fetch trades:', err);
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchTrades();
  }, [fetchTrades]);

  useEffect(() => {
    if (!isPolling) return;
    const interval = setInterval(fetchTrades, 5000);
    return () => clearInterval(interval);
  }, [isPolling, fetchTrades]);

  return {
    trades,
    loading,
    error,
    refresh: fetchTrades,
  };
}
