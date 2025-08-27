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

  public BigDecimal getBalanceById(int id) {
      return balanceRepository.getBalanceByIdFromDB(id);
  }

  public void removeFromBalance(int id , BigDecimal balance) {
      balanceRepository.removeFromBalance(balance , id);
  }

  public void setInitialBalance(Integer id, BigDecimal balance) {
      balanceRepository.setInitialBalance(id , balance);
  }

  public String updateBalance(int id , BigDecimal balance) {

     int rowsAffected =  balanceRepository.updateBalance( balance , id);

     if(rowsAffected == 0) {
         return "No rows affected updating balance with id: " + id;
     }
     return "Balance successfully updated!";
  }
}
