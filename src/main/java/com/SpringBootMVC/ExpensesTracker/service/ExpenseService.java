package com.SpringBootMVC.ExpensesTracker.service;

import java.util.List;
    import java.util.Map;

import com.SpringBootMVC.ExpensesTracker.DTO.ExpenseDTO;
import com.SpringBootMVC.ExpensesTracker.DTO.FilterDTO;
import com.SpringBootMVC.ExpensesTracker.entity.Expense;

public interface ExpenseService {
    Expense findExpenseById(int id);
    void save(ExpenseDTO expenseDTO);
    void update(ExpenseDTO expenseDTO);
    List<Expense> findAllExpenses();
    List<Expense> findAllExpensesByClientId(int id);
    void deleteExpenseById(int id);
    List<Expense> findFilterResult(FilterDTO filter);


    // existing methods...

    Map<String, Integer> getCategoryWiseTotalExpenses(int clientId);
    int getTotalExpenses(int clientId);
    Map<String, Integer> getMonthlyExpenseTrend(int clientId);
    


}
