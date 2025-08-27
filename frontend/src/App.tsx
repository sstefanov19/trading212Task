import TradingMode from "./components/TradingMode.tsx"
import "./App.css"
import { useState } from "react"
import HistoricalMode from "./components/HistoricalMode.tsx";

function App() {

    const [modes , setModes] = useState("");
    const [symbol , setSymbol] = useState("");


    async function startTrading() {

        try {
            const response = await fetch("http://localhost:8080/api/v1/trade/start", {
                method: "POST"
            });
            if (!response.ok) throw new Error("Couldn't start trading");
            setModes("Trade");
        } catch (err: any) {
            console.error(err.message);
        }
    }

    async function stopTrading() {

        try {
            const response = await fetch("http://localhost:8080/api/v1/trade/stop", {
                method: "POST"
            });
            if (!response.ok) throw new Error("Couldn't stop trading");
            setModes("");
        } catch (err: any) {
           console.error(err.message);
        }
    }

    async function startHistoricalTrade() {
        try {
            const response = await fetch(`http://localhost:8080/api/v1/backtest/${symbol}` , {
                method: "POST"
            });
            if(!response.ok) throw new Error("Couldn't start historical trade")

            setModes("Historical");
        }catch(err : any) {
            console.error(err.message);
        }
    }


  return (
    <div className="main-page">
        {modes !== "" ? (
            <button className="back" onClick={() => stopTrading()}>Go back</button>
            ) : (
                <h1>Automated Trading bot task for Trading212</h1>
            )}
        <div className="modes">
            {modes === "Trade" ? (
                <TradingMode />
            ) : modes === 'Historical' ? (
                <HistoricalMode symbol={symbol} />
            ):
            (
                <>
                    <button onClick={() => startTrading()}>Live Trade</button>
                    <button onClick={() => { setSymbol("BTCUSDT"); startHistoricalTrade(); }}>Historical Trade</button>
                </>
            )}
        </div>
    </div>
  )
}

export default App
