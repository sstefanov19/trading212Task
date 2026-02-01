import { useTrades } from '../hooks/useTrading.js';
import './TradeHistory.css';

function TradeHistory() {
  const { trades, loading } = useTrades();

  if (loading) {
    return (
      <div className="trade-history">
        <div className="loading">Loading trades...</div>
      </div>
    );
  }

  return (
    <div className="trade-history">
      <table>
        <thead>
          <tr>
            <th>Time</th>
            <th>Type</th>
            <th>Symbol</th>
            <th>Price</th>
            <th>Quantity</th>
            <th>Total</th>
          </tr>
        </thead>
        <tbody>
          {trades.map((trade, index) => (
            <tr key={trade.id || index}>
              <td className="text-secondary">{new Date(trade.timestamp).toLocaleString()}</td>
              <td className={trade.type === 'BUY' ? 'text-green' : 'text-red'}>
                {trade.type}
              </td>
              <td>{trade.symbol}</td>
              <td>${Number(trade.price).toLocaleString()}</td>
              <td>{trade.quantity}</td>
              <td>${Number(trade.total).toLocaleString()}</td>
            </tr>
          ))}
        </tbody>
      </table>

      {trades.length === 0 && (
        <div className="empty-state">
          No trades yet. Start the bot to begin trading.
        </div>
      )}
    </div>
  );
}

export default TradeHistory;
