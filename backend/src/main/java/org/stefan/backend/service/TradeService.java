package org.stefan.backend.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.stefan.backend.BinanceClient;
import org.stefan.backend.dto.TradeDto;
import org.stefan.backend.model.Trade;
import org.stefan.backend.model.TradeType;
import org.stefan.backend.repository.TradeRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
public class TradeService {

    private static final double PRICE_CHANGE_THRESHOLD = 0.05;
    private static final Long PORTFOLIO_ID = 1L;
    private static final double STOP_LOSS_THRESHOLD = 0.04;
    private static final int BALANCE_ID = 1;

    private final BinanceClient client;
    private final BalanceService balanceService;

    private final TradeRepository tradeRepository;
    private final PortfolioService portfolioService;

    private BigDecimal referencePrice = null;
    private BigDecimal lastBuyPrice = null;
    private Integer lastBuyQuantity = 0;

    public TradeService(BinanceClient client,
                        BalanceService balanceService,
                        TradeRepository tradeRepository,
                        PortfolioService portfolioService
    ) {
        this.client = client;
        this.balanceService = balanceService;
        this.tradeRepository = tradeRepository;
        this.portfolioService = portfolioService;
    }

    @Scheduled(fixedRate = 5000)
    public void autoTrade() {
        BigDecimal currentPrice = getCurrentPrice();
        if (currentPrice == null) return;

        if (referencePrice == null) {
            referencePrice = currentPrice;
            return;
        }

        BigDecimal priceChange = calculatePriceChange(currentPrice, referencePrice);
        System.out.println("Current price: " + currentPrice + ", Change: " + priceChange + "%");

        boolean tradeMade = executeTradeStrategy(currentPrice, priceChange);

        if (tradeMade || Math.abs(priceChange.doubleValue()) > PRICE_CHANGE_THRESHOLD * 2) {
            referencePrice = currentPrice;
        }
    }

    private BigDecimal getCurrentPrice() {
        String lastPriceStr = client.getLastPrice();
        if (lastPriceStr == null || lastPriceStr.isEmpty()) {
            System.out.println("Skipping trade - no price data available");
            return null;
        }
        return new BigDecimal(lastPriceStr);
    }

    private BigDecimal calculatePriceChange(BigDecimal currentPrice, BigDecimal referencePrice) {
        return currentPrice.subtract(referencePrice)
                .divide(referencePrice, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    private boolean executeTradeStrategy(BigDecimal currentPrice, BigDecimal priceChange) {

        if (checkStopLoss(currentPrice)) {
            return true;
        }

        if (shouldSell(priceChange)) {
            return executeSell(currentPrice);
        }

        if (shouldBuy(priceChange)) {
            return executeBuy(currentPrice);
        }

        return false;
    }

    private boolean checkStopLoss(BigDecimal currentPrice) {
        if (lastBuyPrice != null) {
            // Calculate percentage loss (negative means loss)
            BigDecimal percentageChange = currentPrice.subtract(lastBuyPrice)
                    .divide(lastBuyPrice, 8, RoundingMode.HALF_UP);

            // Convert to percentage for logging
            BigDecimal percentageForLog = percentageChange.multiply(BigDecimal.valueOf(100));
            System.out.println("Stop loss check - Current price: " + currentPrice
                    + ", Buy price: " + lastBuyPrice
                    + ", Change: " + percentageForLog + "%"
                    + ", Threshold: -" + (STOP_LOSS_THRESHOLD * 100) + "%");

            // If percentage loss is greater than threshold (e.g. -0.03 for 3% loss)
            if (percentageChange.compareTo(BigDecimal.valueOf(-STOP_LOSS_THRESHOLD)) <= 0) {
                System.out.println("🚨 Stop loss triggered at " + percentageForLog + "% loss");
                return executeSell(currentPrice);
            }
        }
        return false;
    }

    private boolean shouldSell(BigDecimal priceChange) {
        return priceChange.compareTo(BigDecimal.valueOf(PRICE_CHANGE_THRESHOLD)) >= 0 && lastBuyPrice != null;
    }

    private boolean shouldBuy(BigDecimal priceChange) {
        return priceChange.compareTo(BigDecimal.valueOf(-PRICE_CHANGE_THRESHOLD)) <= 0;
    }

    private boolean executeSell(BigDecimal currentPrice) {
        Double profit = currentPrice.subtract(lastBuyPrice)
                .multiply(BigDecimal.valueOf(lastBuyQuantity))
                .doubleValue();

        BigDecimal earnedAmount = currentPrice.multiply(BigDecimal.valueOf(lastBuyQuantity))
                .setScale(2, RoundingMode.HALF_UP);

        balanceService.updateBalance(BALANCE_ID, earnedAmount);
        BigDecimal newBalance = balanceService.getBalanceById(BALANCE_ID);
        portfolioService.saveToPortfolio(newBalance, profit, 0, PORTFOLIO_ID);
        placeOrder(LocalDateTime.now(), String.valueOf(TradeType.SELL), lastBuyQuantity, currentPrice, profit);

        lastBuyPrice = null;
        lastBuyQuantity = 0;
        return true;
    }

    private boolean executeBuy(BigDecimal currentPrice) {
        int quantity = getQuantityToBuy(currentPrice);
        if (quantity > 0) {
            BigDecimal spentAmount = currentPrice.multiply(BigDecimal.valueOf(quantity))
                    .setScale(2, RoundingMode.HALF_UP);
            balanceService.removeFromBalance(BALANCE_ID, spentAmount);

            lastBuyPrice = currentPrice;
            lastBuyQuantity = quantity;

            BigDecimal newBalance = balanceService.getBalanceById(BALANCE_ID);
            portfolioService.saveToPortfolio(newBalance, 0.00, lastBuyQuantity, PORTFOLIO_ID);
            placeOrder(LocalDateTime.now(), String.valueOf(TradeType.BUY), quantity, currentPrice, null);

            return true;
        } else {
            System.out.println("Insufficient balance to buy");
            return false;
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
