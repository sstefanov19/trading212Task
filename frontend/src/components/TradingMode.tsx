import { useFetchPortfolioData } from "../hooks/useFetchPortfolioData";
import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer } from "recharts";
import "./TradingMode.css";



export default function TradingMode() {
  const { data, error, isLoading } = useFetchPortfolioData();
  if (isLoading) return <div>Loading...</div>;
  if (error) return <div>{error.message}</div>;

  const isProfitPositive = (data?.data.profit ?? 0) > 0;

  // Calculate base price, gain/loss, and percent change
  const balance = data?.data.balance ?? 0;
  const profit = data?.data.profit ?? 0;
  const basePrice = balance - profit;
  const gainLoss = balance - basePrice;
  const percentChange = basePrice !== 0 ? ((gainLoss / basePrice) * 100).toFixed(2) : "0.00";
  const isGain = gainLoss >= 0;


  const profitHistory = [
    { label: "Start", value: basePrice },
    { label: "Now", value: balance },
  ];

  return (
    <div className="portfolio">
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
            <YAxis axisLine={false} tickLine={false} tick={{ fill: '#aaa' }} domain={['auto', 'auto']} />
            <Tooltip contentStyle={{ background: '#222', border: 'none', color: '#fff' }} labelStyle={{ color: '#fff' }} />
            <Line type="monotone" dataKey="value" stroke="#00e0ff" strokeWidth={2} dot={false} />
          </LineChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}
