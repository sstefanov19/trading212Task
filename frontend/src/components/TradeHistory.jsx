import { useState, useEffect } from 'react';
import { trading } from '../services/api.js';
import './TradeHistory.css';

function TradeHistory() {
  const [tradeList, setTradeList] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchTrades();
  }, []);

  const fetchTrades = async () => {
    try {
      const response = await trading.getAll();
      setTradeList(response.data || []);
    } catch (error) {
      console.error('Failed to fetch trades:', error);
    }
    setLoading(false);
  };

  if (loading) {
    return (
      <div className="trade-history">
        <h2>Trade History</h2>
        <div className="card loading">Loading trades...</div>
      </div>
    );
  }

  return (
    <div className="trade-history">
      <h2>Trade History</h2>
      <div className="card">
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
            {tradeList.map((trade) => (
              <tr key={trade.id}>
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

        {tradeList.length === 0 && (
          <div className="empty-state">
            No trades yet. Start the bot to begin trading.
          </div>
        )}
      </div>
    </div>
  );
}

export default TradeHistory;
