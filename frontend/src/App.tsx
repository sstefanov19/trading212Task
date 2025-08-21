import { useEffect } from "react"

function App() {


    useEffect(() => {
        const fetchData = async () => {
            const response = await fetch("http://localhost:8080/api/v1/test");

            if(!response.ok) throw new Error("Error while fetching");

            const data = await response.text();

            console.log(data);
        }

        fetchData();
    } , []);

  return (
    <>
        <h1>hello world</h1>
    </>
  )
}

export default App
