import { useQuery } from "@tanstack/react-query"
import axios from "axios";


type Trade = {
    date: string;
    action: string;
    quantity: number;
    price: number;
    profit: number;
    source: string;
}

export const useFetchLiveTrades = (source) => {

    return useQuery({
        queryKey: ['liveTrade'],
        queryFn: () => {
            return axios.get<Trade>(`http://localhost:8080/api/v1/trade/${source}`)
        },
        staleTime: 1000,
        refetchInterval: 2000,
    })
}
