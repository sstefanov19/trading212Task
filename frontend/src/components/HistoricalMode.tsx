

import { Link } from "@tanstack/react-router";
import { useFetchPortfolioData } from "../hooks/useFetchPortfolioData";
import TradeHistory from "./TradeHistory";

export default function HistoricalMode({ symbol }) {


      const { data, error, isLoading } = useFetchPortfolioData(2);
      if (isLoading) return <div>Loading...</div>;
      if (error) return <div>{error.message}</div>;


  return (
    <div className="historical-mode">
        <Link to="/">
        <button>Back</button>
        </Link>
      <h2>Historical Backtest Results for the past 30 days every hour</h2>
      <div>
        <p><strong>Symbol:</strong> {symbol}</p>
      </div>
      <div>

        <ul>
          <li>Profit :  {data?.data.profit}</li>
            <div>
                <TradeHistory source="BACKTEST" />
            </div>
        </ul>
      </div>
    </div>
  );
};
