import { useState, useEffect } from 'react';
import { trading, portfolio } from '../services/api';
import ProfitLossChart from './ProfitLossChart';
import './Dashboard.css';

function Dashboard() {
  const [botStatus, setBotStatus] = useState({ running: false });
  const [selectedStrategy, setSelectedStrategy] = useState('MEAN_REVERSION');
  const [loading, setLoading] = useState(false);
  const [portfolioData, setPortfolioData] = useState({});
  const [chartData, setChartData] = useState([]);

  const strategies = ['MEAN_REVERSION', 'MOVING_AVERAGE'];

  useEffect(() => {
    fetchStatus();
    fetchPortfolio();
    fetchChartData();
  }, []);

  const fetchStatus = async () => {
    try {
      const response = await trading.status();
      setBotStatus(response.data);
    } catch (error) {
      console.error('Failed to fetch status:', error);
    }
  };

  const handleStart = async () => {
    setLoading(true);
    try {
      await trading.start(selectedStrategy);
      await fetchStatus();
      await fetchChartData();
    } catch (error) {
      console.error('Failed to start bot:', error);
    }
    setLoading(false);
  };

  const handleStop = async () => {
    setLoading(true);
    try {
      await trading.stop();
      await fetchStatus();
      await fetchChartData();
      await fetchPortfolio();
    } catch (error) {
      console.error('Failed to stop bot:', error);
    }
    setLoading(false);
  };

  const fetchPortfolio = async () => {
    try {
      const response = await portfolio.get();
      console.log("Portfolio:" , response.data);
      setPortfolioData(response.data);
    } catch (error) {
      console.error('Failed to fetch portfolio:', error);
    }
  };

  const fetchChartData = async () => {
    try {
      const [tradesRes, portfolioRes] = await Promise.all([
        trading.getAll(),
        portfolio.get(),
      ]);
      const trades = tradesRes.data;
      const portfolioData = portfolioRes.data;
      const initialBalance = Number(portfolioData.initialBalance) || 1000;
      const currentValue = Number(portfolioData.currentValue) || initialBalance;
      const btcHoldingsNow = Number(portfolioData.btcHoldings) || 0;
      const usdtBalanceNow = Number(portfolioData.usdtBalance) || 0;

      const currentBtcPrice = btcHoldingsNow > 0
        ? (currentValue - usdtBalanceNow) / btcHoldingsNow
        : (trades.length > 0 ? Number(trades[trades.length - 1].price) : 0);

      if (trades.length === 0) {
        setChartData([{ time: 'Start', value: initialBalance }]);
        return;
      }

      // Sort trades by timestamp chronologically
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

      // Ensure last point matches actual current value
      if (points.length > 1) {
        points[points.length - 1].value = currentValue;
      }

      setChartData(points);
    } catch (error) {
      console.error('Failed to fetch chart data:', error);
    }
  };

  return (
    <div className="dashboard">
      <h1>Trading Dashboard</h1>

      <div className="dashboard-grid">
        <div className="card status-card">
          <h3>Bot Status</h3>
          <div className={`status-indicator ${botStatus.running ? 'running' : 'stopped'}`}>
            {botStatus.running ? 'RUNNING' : 'STOPPED'}
          </div>

          <div className="controls">
            <select
              value={selectedStrategy}
              onChange={(e) => setSelectedStrategy(e.target.value)}
              disabled={botStatus.running}
            >
              {strategies.map((s) => (
                <option key={s} value={s}>{s.replace('_', ' ')}</option>
              ))}
            </select>

            {botStatus.running ? (
              <button className="danger" onClick={handleStop} disabled={loading}>
                Stop Bot
              </button>
            ) : (
              <button className="success" onClick={handleStart} disabled={loading}>
                Start Bot
              </button>
            )}
          </div>
        </div>

        <div className="card balance-card">
          <h3>Portfolio</h3>
          <div className="balance-row">
            <span className="label">USDT Balance</span>
            <span className="value">{portfolioData.usdtBalance}</span>
          </div>
          <div className="balance-row">
            <span className="label">BTC Holdings</span>
            <span className="value">{portfolioData.btcHoldings}</span>
          </div>
          {portfolioData.btcHoldings > 0 && (

              <div className="balance-row">
            <span className="label">Current value</span>
            <span className="value">{portfolioData.currentValue}</span>
          </div>
          )
        }
        </div>

        <div className="card pnl-card">
          <h3>Profit / Loss</h3>
          <div className="pnl-value text-green">{portfolioData.profitLoss}</div>
          <div className="pnl-percent text-green">{portfolioData.profitLossPercent}%</div>
        </div>
      </div>

      <div className="card chart-card">
        <h3>Performance Chart</h3>
        <ProfitLossChart data={chartData} />
      </div>
    </div>
  );
}

export default Dashboard;
