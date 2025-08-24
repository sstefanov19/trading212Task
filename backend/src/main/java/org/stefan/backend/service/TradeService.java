package org.stefan.backend.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.stefan.backend.BinanceClient;
import org.stefan.backend.model.Trade;
import org.stefan.backend.model.TradeType;
import org.stefan.backend.repository.TradeRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
public class TradeService {

    private final BinanceClient client;
    private final BalanceService balanceService;
    private final TradeRepository tradeRepository;
    private BigDecimal referencePrice = null;
    private Double totalProfit = 0.0;

    private BigDecimal lastBuyPrice = null;
    private Integer lastBuyQuantity = 0;

    public TradeService(BinanceClient client,
                        BalanceService balanceService,
                        TradeRepository tradeRepository) {
        this.client = client;
        this.balanceService = balanceService;
        this.tradeRepository = tradeRepository;
    }

    @Scheduled(fixedRate = 5000) // every 15 seconds
    public void autoTrade() {

        String lastPriceStr = client.getLastPrice();
        if (lastPriceStr == null || lastPriceStr.isEmpty()) {
            System.out.println("Skipping trade - no price data available");
            return;
        }

        BigDecimal currentPrice = new BigDecimal(lastPriceStr);

        if (referencePrice == null) {
            referencePrice = currentPrice;
            return;
        }

        BigDecimal change = currentPrice.subtract(referencePrice)
                .divide(referencePrice, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        System.out.println("Current price: " + currentPrice + ", Change: " + change + "%");

        if (change.compareTo(BigDecimal.valueOf(0.05)) >= 0 && lastBuyPrice != null) {
            //SELL
            Double profit = currentPrice.subtract(lastBuyPrice)
                    .multiply(BigDecimal.valueOf(lastBuyQuantity))
                    .doubleValue();

            totalProfit = totalProfit + profit;

            BigDecimal earnedAmount = currentPrice.multiply(BigDecimal.valueOf(lastBuyQuantity))
                    .setScale(2, RoundingMode.HALF_UP);
            balanceService.updateBalance(1 , earnedAmount);

            placeOrder(LocalDateTime.now(), String.valueOf(TradeType.SELL), lastBuyQuantity, currentPrice, totalProfit);


            lastBuyPrice = null;
            lastBuyQuantity = 0;



        } else if (change.compareTo(BigDecimal.valueOf(-0.05)) <= 0) {
            // BUY

            int quantity = getQuantityToBuy(currentPrice);
            if(quantity > 0) {
                BigDecimal spentAmount = currentPrice.multiply(BigDecimal.valueOf(quantity))
                        .setScale(2, RoundingMode.HALF_UP);
                balanceService.removeFromBalance(1, spentAmount);

                lastBuyPrice = currentPrice;
                lastBuyQuantity = quantity;


                placeOrder(LocalDateTime.now(), String.valueOf(TradeType.BUY), quantity, currentPrice, null);
            }else {
                System.out.println("Insufficient balance to buy");
                return;
            }
        }
    }

    private int getQuantityToBuy(BigDecimal price) {
        try {
            BigDecimal balance = balanceService.getBalanceById(1);
            return balance.divide(price, RoundingMode.DOWN).intValue();
        } catch (Exception e) {
            System.out.println("Error getting balance: " + e.getMessage());
            return 0;
        }
    }

    public void placeOrder(LocalDateTime date, String action, int qty, BigDecimal price, Double profit) {

        Trade trade = new Trade();
        trade.setDate(date);
        trade.setAction(TradeType.valueOf(action));
        trade.setQuantity(qty);
        trade.setPrice(price);
        trade.setProfit(profit);

        System.out.println("Order sent");
        TradeDto tradeDto = mapToDto(trade);
        tradeRepository.save(tradeDto);
    }

    private TradeDto mapToDto(Trade trade) {
        TradeDto dto = new TradeDto();
        dto.setDate(trade.getDate());
        dto.setAction(trade.getAction());
        dto.setQuantity(trade.getQuantity());
        dto.setPrice(trade.getPrice());
        dto.setProfit(trade.getProfit());
        return dto;
    }
}
