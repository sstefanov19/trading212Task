import { useQuery } from "@tanstack/react-query";
import axios from "axios";

type Portfolio = {
    balance: number;
    profit: number;
    quantity: number;
};


export const useFetchPortfolioData = () => {
    // eslint-disable-next-line react-hooks/rules-of-hooks
    return useQuery({
        queryKey: ['portfolio'],
        queryFn : () => {
            return axios.get<Portfolio>("http://localhost:8080/api/v1/portfolio/1");
        },
        staleTime: 1000,
    });
}
