

import { useFetchPortfolioData } from "../hooks/useFetchPortfolioData";

export default function HistoricalMode({ symbol }) {


      const { data, error, isLoading } = useFetchPortfolioData(2);
      if (isLoading) return <div>Loading...</div>;
      if (error) return <div>{error.message}</div>;


  return (
    <div className="historical-mode">
      <h2>Historical Backtest Results</h2>
      <div>
        <p><strong>Symbol:</strong> {symbol}</p>
      </div>
      <div>

        <ul>
          <li> {data?.data.profit}</li>

        </ul>
      </div>
    </div>
  );
};
