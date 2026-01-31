import { useState } from 'react';
import { backtest } from '../services/api.js';
import ProfitLossChart from './ProfitLossChart.jsx';
import './Backtest.css';

function Backtest() {
  const [period, setPeriod] = useState('LAST_1_DAY');
  const [initialBalance, setInitialBalance] = useState(1000);
  const [strategy, setStrategy] = useState('MEAN_REVERSION');
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);

  const strategies = ['MEAN_REVERSION', 'MOVING_AVERAGE'];
  const periods = [
    { value: 'LAST_10_MINUTES', label: 'Last 10 Minutes' },
    { value: 'LAST_10_HOURS', label: 'Last 10 Hours' },
    { value: 'LAST_1_DAY', label: 'Last 1 Day' },
  ];

  const handleRunBacktest = async () => {
    setLoading(true);
    try {
      const response = await backtest.run(initialBalance, strategy, period);
      setResult(response.data);
    } catch (error) {
      console.error('Backtest failed:', error);
      alert('Backtest failed. Check console for details.');
    }
    setLoading(false);
  };

  return (
    <div className="backtest">
      <h2>Strategy Backtest</h2>

      <div className="backtest-grid">
        <div className="card config-card">
          <h3>Configuration</h3>

          <div className="form-group">
            <label>Strategy</label>
            <select value={strategy} onChange={(e) => setStrategy(e.target.value)}>
              {strategies.map((s) => (
                <option key={s} value={s}>{s.replace('_', ' ')}</option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label>Period</label>
            <select value={period} onChange={(e) => setPeriod(e.target.value)}>
              {periods.map((p) => (
                <option key={p.value} value={p.value}>{p.label}</option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label>Initial Balance ($)</label>
            <input
              type="number"
              value={initialBalance}
              onChange={(e) => setInitialBalance(Number(e.target.value))}
            />
          </div>

          <button onClick={handleRunBacktest} disabled={loading}>
            {loading ? 'Running...' : 'Run Backtest'}
          </button>
        </div>

        {result && (
          <div className="card results-card">
            <h3>Results</h3>

            <div className="result-row">
              <span className="label">Backtest ID</span>
              <span className="value text-secondary">{result.backtestId?.slice(0, 8)}...</span>
            </div>

            <div className="result-row">
              <span className="label">Price Points</span>
              <span className="value">{result.pricePoints?.toLocaleString()}</span>
            </div>

            <div className="result-row">
              <span className="label">Buy Orders</span>
              <span className="value text-green">{result.buyCount}</span>
            </div>

            <div className="result-row">
              <span className="label">Sell Orders</span>
              <span className="value text-red">{result.sellCount}</span>
            </div>

            <div className="result-row">
              <span className="label">Initial Balance</span>
              <span className="value">${result.initialBalance?.toFixed(2)}</span>
            </div>

            <div className="result-row">
              <span className="label">Final Value</span>
              <span className="value">${result.finalValue?.toFixed(2)}</span>
            </div>

            <div className="result-row highlight">
              <span className="label">Profit / Loss</span>
              <span className={`value ${result.profitLoss >= 0 ? 'text-green' : 'text-red'}`}>
                {result.profitLoss >= 0 ? '+' : ''}${result.profitLoss?.toFixed(2)}
              </span>
            </div>
          </div>
        )}
      </div>

      {result && (
        <div className="card chart-card">
          <h3>Performance</h3>
          <ProfitLossChart data={[
            { time: 'Start', value: result.initialBalance },
            { time: 'End', value: result.finalValue }
          ]} />
        </div>
      )}
    </div>
  );
}

export default Backtest;
