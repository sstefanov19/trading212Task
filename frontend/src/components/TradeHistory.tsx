import React from 'react'
import { useFetchLiveTrades } from '../hooks/useFetchLiveTrades';
import './TradeHistory.css';

type Trade = {
    date: string;
    action: string;
    quantity: number;
    price: number;
    profit: number;
    source: string;
};

type TradeHistoryProps = {
    source: string;
};

export default function TradeHistory({ source }: TradeHistoryProps) {
    const { data, error, isLoading } = useFetchLiveTrades(source);
    if (isLoading) return <div>Loading...</div>;
    if (error) return <div>{error.message}</div>;

    const trades: Trade[] = data?.data ?? [];

    return (
        <div style={{ marginTop: 42 }}>
            <h2>Live Trade History</h2>
            <table className="live-trade-history-table">
                <thead>
                    <tr>
                        <th>Date</th>
                        <th>Action</th>
                        <th>Quantity</th>
                        <th>Price</th>
                        <th>Profit</th>
                        <th>Source</th>
                    </tr>
                </thead>
                <tbody>
                    {trades.length === 0 ? (
                        <tr>
                            <td colSpan={6} className="live-trade-history-empty">No trades found.</td>
                        </tr>
                    ) : (
                        trades.map((trade, idx) => (
                            <tr key={idx}>
                                <td>{trade.date}</td>
                                <td>{trade.action}</td>
                                <td>{trade.quantity}</td>
                                <td>{trade.price}</td>
                                <td>{trade.profit}</td>
                                <td>{trade.source}</td>
                            </tr>
                        ))
                    )}
                </tbody>
            </table>
        </div>
    );
}
