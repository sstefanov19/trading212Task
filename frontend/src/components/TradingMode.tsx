import { useFetchPortfolioData } from "../hooks/useFetchPortfolioData";
import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer } from "recharts";
import "./TradingMode.css";
import TradeHistory from "./TradeHistory";
import { Link } from "@tanstack/react-router";
import { useEffect, useRef, useState } from "react";



export default function TradingMode() {
  const { data, error, isLoading } = useFetchPortfolioData(1);

  const [profitHistory, setProfitHistory] = useState<{ label: string; value: number }[]>([]);
  const isFirst = useRef(true);

  useEffect(() => {
      if (!data?.data) return;

      const balance = data.data.balance ?? 0;
      const now = new Date().toLocaleTimeString();

      setProfitHistory((prev) => {
          // On first load, add a "Start" point
          if (isFirst.current) {
              isFirst.current = false;
              return [
                  { label: "Start", value: balance },
                  { label: now, value: balance },
                ];
            }
           
            return [...prev, { label: now, value: balance }];
        });
    }, [data?.data?.balance]);

    if (isLoading) return <div>Loading...</div>;
    if (error) return <div>{error.message}</div>;
  const isProfitPositive = (data?.data.profit ?? 0) > 0;

  const balance = data?.data.balance ?? 0;
  const profit = data?.data.profit ?? 0;
  const basePrice = balance - profit;
  const gainLoss = balance - basePrice;
  const percentChange = basePrice !== 0 ? ((gainLoss / basePrice) * 100).toFixed(2) : "0.00";
  const isGain = gainLoss >= 0;

     async function stopTrading() {

        try {
            const response = await fetch("http://localhost:8080/api/v1/trade/stop", {
                method: "POST"
            });
            if (!response.ok) throw new Error("Couldn't stop trading");

        } catch (err: any) {
           console.error(err.message);
        }
    }


  return (
    <div className="portfolio">
        <Link to="/">
        <button onClick={() => stopTrading()}>Back</button>
        </Link>
        <h1>Live Trading</h1>
      <p>Balance : {balance}</p>
      <p className={`${isProfitPositive ? "green" : "red"}`}>Profit: {profit}</p>
      <p>Quantity of stock : {data?.data.quantity}</p>

      <p className={isGain ? "green" : "red"}>
        Total Gain/Loss: {gainLoss >= 0 ? "+" : ""}{gainLoss.toFixed(2)} ({gainLoss >= 0 ? "+" : ""}{percentChange}%)
      </p>

      <div className="chart">
        <ResponsiveContainer>
          <LineChart data={profitHistory} margin={{ top: 20, right: 30, left: 0, bottom: 0 }}>
            <XAxis dataKey="label" axisLine={false} tickLine={false} tick={{ fill: '#aaa' }} />
            <YAxis axisLine={true} tickLine={false} tick={{ fill: '#aaa' }} domain={['auto', 'auto']} />
            <Tooltip contentStyle={{ background: '#222', border: 'none', color: '#fff' }} labelStyle={{ color: '#fff' }} />
            <Line type="monotone" dataKey="value" stroke="#00e0ff" strokeWidth={2} dot={true} />
          </LineChart>
        </ResponsiveContainer>
      </div>
      <TradeHistory source="LIVE" />
    </div>
  );
}
