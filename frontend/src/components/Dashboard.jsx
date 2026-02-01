import { useState } from 'react';
import { useTrading, usePortfolio } from '../hooks/useTrading.js';
import ProfitLossChart from './ProfitLossChart.jsx';
import TradeHistory from './TradeHistory.jsx';
import Backtest from './Backtest.jsx';
import './Dashboard.css';

function Dashboard() {
  const [mode, setMode] = useState('live');
  const [selectedStrategy, setSelectedStrategy] = useState('MEAN_REVERSION');

  const strategies = ['MEAN_REVERSION', 'MOVING_AVERAGE'];

  // Custom hooks handle all the complexity
  const {
    botStatus,
    loading,
    start,
    stop,
    pause,
    resume,
    reset
  } = useTrading();

  const {
    portfolioData,
    chartData,
    refresh: refreshPortfolio
  } = usePortfolio(botStatus.running && !botStatus.paused);

  const handleStart = async () => {
    await start(selectedStrategy);
    await refreshPortfolio();
  };

  const handleStop = async () => {
    await stop();
    await refreshPortfolio();
  };

  const handleReset = async () => {
    await reset();
    await refreshPortfolio();
  };

  return (
    <div className="dashboard">
      <div className="dashboard-header">
        <h1>Trading Dashboard</h1>
        <div className="mode-toggle">
          <button
            className={mode === 'live' ? 'mode-btn active' : 'mode-btn'}
            onClick={() => setMode('live')}
          >
            Live Sim
          </button>
          <button
            className={mode === 'backtest' ? 'mode-btn active' : 'mode-btn'}
            onClick={() => setMode('backtest')}
          >
            Backtest
          </button>
        </div>
      </div>

      {mode === 'backtest' ? (
        <Backtest />
      ) : (
      <>
      <div className="dashboard-grid">
        <div className="card status-card">
          <h3>Bot Status</h3>
          <div className={`status-indicator ${botStatus.running ? (botStatus.paused ? 'paused' : 'running') : 'stopped'}`}>
            {botStatus.running ? (botStatus.paused ? 'PAUSED' : 'RUNNING') : 'STOPPED'}
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
              <>
                {botStatus.paused ? (
                  <button className="success" onClick={resume} disabled={loading}>
                    Resume
                  </button>
                ) : (
                  <button className="warning" onClick={pause} disabled={loading}>
                    Pause
                  </button>
                )}
                <button className="danger" onClick={handleStop} disabled={loading}>
                  Stop Bot
                </button>
              </>
            ) : (
              <>
                <button className="success" onClick={handleStart} disabled={loading}>
                  Start Bot
                </button>
                <button className="danger" onClick={handleReset} disabled={loading}>
                  Reset
                </button>
              </>
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
          )}
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

      <div className="card trades-card">
        <h3>Trade History</h3>
        <TradeHistory />
      </div>
      </>
      )}
    </div>
  );
}

export default Dashboard;
