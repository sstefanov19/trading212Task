import "./App.css"
import { Link } from "@tanstack/react-router";

function App() {




    async function startTrading() {

        try {
            const response = await fetch("http://localhost:8080/api/v1/trade/start", {
                method: "POST"
            });
            if (!response.ok) throw new Error("Couldn't start trading");

        } catch (err: any) {
            console.error(err.message);
        }
    }


    async function startHistoricalTrade(symbol : string) {
        try {
            const response = await fetch(`http://localhost:8080/api/v1/backtest/${symbol}` , {
                method: "POST"
            });
            if(!response.ok) throw new Error("Couldn't start historical trade")


        }catch(err : any) {
            console.error(err.message);
        }
    }


  return (
    <div className="main-page">
      <h1>Automated Trading bot task for Trading212</h1>
      <div className="modes">
        <Link to="/trade">
          <button onClick={() => startTrading()}>Live Trade</button>
        </Link>
        <Link to="/historytrade">
          <button onClick={() => startHistoricalTrade("ETHUSDT")}>Historical Trade on ETH</button>
        </Link>
      </div>
    </div>
  )
}

export default App
