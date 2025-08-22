package org.stefan.backend.service;

import org.springframework.stereotype.Service;
import org.stefan.backend.repository.BalanceRepository;

import java.math.BigDecimal;

@Service
public class BalanceService {

  private final BalanceRepository balanceRepository;

  public BalanceService(BalanceRepository balanceRepository) {
      this.balanceRepository = balanceRepository;
  }

  public void createNewBalance(BigDecimal balance) {
      balanceRepository.insertBalance(balance);
  }

  public String updateBalance(Long id , BigDecimal balance) {
     int rowEffected =  balanceRepository.updateBalance(id , balance);

     if(rowEffected == 0) {
         return "No rows affected updating balance with id: " + id;
     }
     return "Balance successfully updated!";
  }
}
