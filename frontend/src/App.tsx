import { useState } from "react";


function App() {
    const [price , setPrice] = useState("");
   const socket = new WebSocket("ws://localhost:8080/ws/bitcoin");

   socket.onmessage = (event) => {
        setPrice(event.data);
   }

  return (
    <>
        <h1>{price}</h1>
    </>
  )
}

export default App
