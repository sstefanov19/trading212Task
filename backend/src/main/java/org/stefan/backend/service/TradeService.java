package org.stefan.backend.service;

import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.stefan.backend.BinanceClient;
import org.stefan.backend.dto.TradeDto;
import org.stefan.backend.model.Trade;
import org.stefan.backend.model.TradeType;
import org.stefan.backend.repository.TradeRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class TradeService {

    private static final double PRICE_CHANGE_THRESHOLD = 0.05;
    private static final Long PORTFOLIO_ID = 1L;
    private static final double STOP_LOSS_THRESHOLD = 0.07;
    private static final int BALANCE_ID = 1;

    private AtomicBoolean isTrading = new AtomicBoolean(false);
    private ScheduledFuture<?> tradingTask;
    private final TaskScheduler taskScheduler;

    private final BinanceClient client;
    private final BalanceService balanceService;

    private final TradeRepository tradeRepository;
    private final PortfolioService portfolioService;

    private BigDecimal referencePrice = null;
    private BigDecimal lastBuyPrice = null;
    private BigDecimal lastBuyQuantity = BigDecimal.ZERO;

    public TradeService(TaskScheduler taskScheduler,
                        BinanceClient client,
                        BalanceService balanceService,
                        TradeRepository tradeRepository,
                        PortfolioService portfolioService
    ) {
        this.taskScheduler = taskScheduler;
        this.client = client;
        this.balanceService = balanceService;
        this.tradeRepository = tradeRepository;
        this.portfolioService = portfolioService;
    }


    @PostConstruct
    public void init() {
        Double quantity = portfolioService.getPortfolioQuantityById(PORTFOLIO_ID);
        if (quantity > 0) {
            Trade lastBuyTrade = tradeRepository.findTopByActionOrderByDateDesc(TradeType.BUY);
            if (lastBuyTrade != null) {
                lastBuyPrice = lastBuyTrade.getPrice();
                lastBuyQuantity = BigDecimal.valueOf(lastBuyTrade.getQuantity());
            }
        }
    }

    public List<TradeDto> getAllTradesBySource(String source) {
        return tradeRepository.getAllTradesBySource(source);
    }

    public void autoTrade() {

        if(!isTrading.get()) return;

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

    public boolean startTrading() {
        if (isTrading.compareAndSet(false, true)) {
            tradingTask = taskScheduler.scheduleAtFixedRate(
                    this::autoTrade,
                    5000  // 5 seconds interval
            );
            System.out.println("Trading started");
            return true;
        }
        return false;
    }

    public boolean stopTrading() {
        if (isTrading.compareAndSet(true, false)) {
            if (tradingTask != null) {
                tradingTask.cancel(false);
                tradingTask = null;
            }
            System.out.println("Trading stopped");
            return true;
        }
        return false;
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

            BigDecimal percentageLoss = currentPrice.subtract(lastBuyPrice)
                    .divide(lastBuyPrice, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .abs();

            System.out.println("Stop loss check - Current price: " + currentPrice
                    + ", Buy price: " + lastBuyPrice
                    + ", Loss: " + percentageLoss + "%"
                    + ", Threshold: -7%");

            if (percentageLoss.compareTo(BigDecimal.valueOf(STOP_LOSS_THRESHOLD)) >= 0) {
                System.out.println("🚨 Stop loss triggered! Loss: " + percentageLoss + "%");
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

        if (lastBuyQuantity.compareTo(BigDecimal.ZERO) > 0) {
            Double profit = currentPrice.subtract(lastBuyPrice)
                    .multiply(lastBuyQuantity)
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();

            BigDecimal earnedAmount = currentPrice.multiply(lastBuyQuantity)
                    .setScale(2, RoundingMode.HALF_UP);

            balanceService.updateBalance(BALANCE_ID, earnedAmount);
            BigDecimal newBalance = balanceService.getBalanceById(BALANCE_ID);
            portfolioService.updatePortfolio(newBalance, profit, 0.0, PORTFOLIO_ID);
            placeOrder(LocalDateTime.now(), String.valueOf(TradeType.SELL), lastBuyQuantity.doubleValue(), currentPrice, profit, "LIVE");

            lastBuyPrice = null;
            lastBuyQuantity = BigDecimal.ZERO;
            return true;
        }
        return false;
    }

    private boolean executeBuy(BigDecimal currentPrice) {
        currentPrice = currentPrice.setScale(2 , RoundingMode.HALF_UP);
        BigDecimal balance = balanceService.getBalanceById(BALANCE_ID);

        BigDecimal quantity = balance.divide(currentPrice, 2, RoundingMode.HALF_UP);
        BigDecimal spentAmount = currentPrice.multiply(quantity).setScale(2, RoundingMode.HALF_UP);

        if (quantity.compareTo(BigDecimal.ZERO) > 0 && spentAmount.compareTo(balance) <= 0) {
            lastBuyPrice = currentPrice;
            lastBuyQuantity = quantity.setScale(2, RoundingMode.HALF_UP);

            balanceService.removeFromBalance(BALANCE_ID, spentAmount);
            BigDecimal newBalance = balanceService.getBalanceById(BALANCE_ID);
            portfolioService.updatePortfolio(newBalance, 0.00, lastBuyQuantity.doubleValue(), PORTFOLIO_ID);
            placeOrder(LocalDateTime.now(), String.valueOf(TradeType.BUY), lastBuyQuantity.doubleValue(), currentPrice, null, "LIVE");
            return true;
        }
            System.out.println("Insufficient balance to buy");
            return false;
        }

    public void deleteAllBacktestTrades() {
        tradeRepository.deleteAllBacktestTrades();
    }

    public void placeOrder(LocalDateTime date, String action, Double qty, BigDecimal price, Double profit , String source) {

        Trade trade = new Trade();
        trade.setDate(date);
        trade.setAction(TradeType.valueOf(action));
        trade.setQuantity(qty);
        trade.setPrice(price);
        trade.setProfit(profit);
        trade.setSource(source);

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
        dto.setSource(trade.getSource());
        return dto;
    }
}