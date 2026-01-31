package org.example.tasktrading212.service;

import org.example.tasktrading212.dto.TradeResponse;
import org.example.tasktrading212.exceptions.ZeroBalanceException;
import org.example.tasktrading212.exceptions.ZeroBitcoinAvailableException;
import org.example.tasktrading212.model.Trade;
import org.example.tasktrading212.model.TradeType;
import org.example.tasktrading212.model.User;
import org.example.tasktrading212.repository.TradeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TradingService {

    private static final Logger logger = LoggerFactory.getLogger(TradingService.class);

    private final PortfolioService portfolioService;
    private final TradeRepository tradeRepository;
    private final UserService userService;

    public TradingService(TradeRepository tradeRepository, PortfolioService portfolioService, UserService userService) {
        this.portfolioService = portfolioService;
        this.tradeRepository = tradeRepository;
        this.userService = userService;
    }

    @Transactional
    public void resetAccount(Long userId) {
        tradeRepository.deleteByUserId(userId);
        portfolioService.resetPortfolio(userId);
        logger.info("Account reset for user {}", userId);
    }

    public BigDecimal getUsdtBalance(Long userId) {
        return portfolioService.getUsdtBalance(userId);
    }

    public BigDecimal getBtcHoldings(Long userId) {
        return portfolioService.getBtcHoldings(userId);
    }

    @Transactional
    public void executeBuy(Long userId, String symbol, BigDecimal price) {
        BigDecimal available = portfolioService.getUsdtBalance(userId);

        if (available.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ZeroBalanceException("User has no balance");
        }

        BigDecimal quantity = available.divide(price, 8, RoundingMode.DOWN);
        BigDecimal total = quantity.multiply(price);

        portfolioService.updatePortfolio(userId, available.subtract(total), portfolioService.getBtcHoldings(userId).add(quantity));



        Trade trade = new Trade(userId,TradeType.BUY, symbol, price, quantity, total, LocalDateTime.now());
        tradeRepository.saveTrade(trade);

        logger.info("User {} BUY: {} {} @ {} = {} USDT", userId, quantity, symbol, price, total);
    }

    @Transactional
    public void executeSell(Long userId, String symbol, BigDecimal price) {
        BigDecimal quantity = portfolioService.getBtcHoldings(userId);

        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ZeroBitcoinAvailableException("No BTC to sell");
        }

        BigDecimal total = quantity.multiply(price);

        portfolioService.updatePortfolio(userId, portfolioService.getUsdtBalance(userId).add(total), BigDecimal.ZERO);

        Trade trade = new Trade(userId,TradeType.SELL, symbol, price, quantity, total, LocalDateTime.now());
        tradeRepository.saveTrade(trade);


        logger.info("User {} SELL: {} {} @ {} = {} USDT", userId, quantity, symbol, price, total);
    }

    public List<TradeResponse> getTrades() {
        User user = userService.getCurrentUser();
        List<Trade> trades = tradeRepository.findByUserId(user.getId());
       return trades.stream()
            .map(t -> new TradeResponse(
                    t.getType(),
                    t.getSymbol(),
                    t.getPrice(),
                    t.getQuantity(),
                    t.getTotal(),
                    t.getTimestamp()))
            .toList();
    }
}
