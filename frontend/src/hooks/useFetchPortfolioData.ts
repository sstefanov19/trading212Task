import { useQuery } from "@tanstack/react-query";
import axios from "axios";

type Portfolio = {
    balance: number;
    profit: number;
    quantity: number;
};


export const useFetchPortfolioData = (id) => {
    // eslint-disable-next-line react-hooks/rules-of-hooks
    return useQuery({
        queryKey: ['portfolio'],
        queryFn : () => {
            return axios.get<Portfolio>(`http://localhost:8080/api/v1/portfolio/${id}`);
        },
        staleTime: 1000,
        refetchInterval: 2000,
    });
}
