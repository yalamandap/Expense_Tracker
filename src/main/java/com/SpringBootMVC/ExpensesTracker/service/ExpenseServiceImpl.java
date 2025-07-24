package com.SpringBootMVC.ExpensesTracker.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.SpringBootMVC.ExpensesTracker.DTO.ExpenseDTO;
import com.SpringBootMVC.ExpensesTracker.DTO.FilterDTO;
import com.SpringBootMVC.ExpensesTracker.entity.Category;
import com.SpringBootMVC.ExpensesTracker.entity.Expense;
import com.SpringBootMVC.ExpensesTracker.repository.ExpenseRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

@Service
public class ExpenseServiceImpl implements ExpenseService {
    ExpenseRepository expenseRepository;
    ClientService clientService;
    CategoryService categoryService;
    EntityManager entityManager;

    @Autowired
    public ExpenseServiceImpl(ExpenseRepository expenseRepository, ClientService clientService
            , CategoryService categoryService, EntityManager entityManager) {
        this.expenseRepository = expenseRepository;
        this.clientService = clientService;
        this.categoryService = categoryService;
        this.entityManager = entityManager;
    }


    @Override
    public Expense findExpenseById(int id) {
        return expenseRepository.findById(id).orElse(null);
    }

    @Transactional
    @Override
    public void save(ExpenseDTO expenseDTO) {
        System.out.println(expenseDTO);
        Expense expense = new Expense();
        expense.setAmount(expenseDTO.getAmount());
        expense.setDateTime(expenseDTO.getDateTime());
        expense.setDescription(expenseDTO.getDescription());
        expense.setClient(clientService.findClientById(expenseDTO.getClientId()));
        Category category = categoryService.findCategoryByName(expenseDTO.getCategory());
        expense.setCategory(category);
        expenseRepository.save(expense);
    }

    @Override
    public void update(ExpenseDTO expenseDTO) {
        Expense existingExpense = expenseRepository.findById(expenseDTO.getExpenseId()).orElse(null);
        existingExpense.setAmount(expenseDTO.getAmount());
        existingExpense.setDateTime(expenseDTO.getDateTime());
        existingExpense.setDescription(expenseDTO.getDescription());
        Category category = categoryService.findCategoryByName(expenseDTO.getCategory());
        existingExpense.setCategory(category);
        expenseRepository.save(existingExpense);
    }

    @Override
    public List<Expense> findAllExpenses() {
        return expenseRepository.findAll();
    }

    @Override
    public List<Expense> findAllExpensesByClientId(int id) {
        return expenseRepository.findByClientId(id);
    }

    @Override
    public void deleteExpenseById(int id) {
        expenseRepository.deleteById(id);
    }

    @Override
    public List<Expense> findFilterResult(FilterDTO filter) {
        String query = "select e from Expense e where";
        if (!"all".equals(filter.getCategory())) {
            String category = filter.getCategory();
            int categoryId = categoryService.findCategoryByName(category).getId();
            query += String.format(" e.category.id = %d AND", categoryId);
        }
        int from = filter.getFrom();
        int to = filter.getTo();
        query += String.format(" e.amount between %d and %d", from, to);
        if (!"all".equals(filter.getYear())) {
            query += String.format(" AND CAST(SUBSTRING(e.dateTime, 1, 4) AS INTEGER) = %s", filter.getYear());
        }
        if (!"all".equals(filter.getMonth())) {
            query += String.format(" AND CAST(SUBSTRING(e.dateTime, 6, 2) AS INTEGER) = %s", filter.getMonth());
        }
        TypedQuery<Expense> expenseTypedQuery = entityManager.createQuery(query, Expense.class);
        List<Expense> expenseList = expenseTypedQuery.getResultList();
        return expenseList;
    }
    
@Override
public Map<String, Integer> getCategoryWiseTotalExpenses(int clientId) {
    String sql = "SELECT c.name, SUM(e.amount) FROM Expense e " +
                 "JOIN e.category c " +
                 "WHERE e.client.id = :clientId " +
                 "GROUP BY c.name";
    Query query = entityManager.createQuery(sql);
    query.setParameter("clientId", clientId);
    List<Object[]> results = query.getResultList();

    Map<String, Integer> categoryTotals = new HashMap<>();
    for (Object[] row : results) {
        String categoryName = (String) row[0];
        Number totalAmount = (Number) row[1];
        categoryTotals.put(categoryName, totalAmount.intValue());
    }

    return categoryTotals;
}

@Override
public int getTotalExpenses(int clientId) {
    String sql = "SELECT SUM(e.amount) FROM Expense e WHERE e.client.id = :clientId";
    Query query = entityManager.createQuery(sql);
    query.setParameter("clientId", clientId);
    Number result = (Number) query.getSingleResult();
    return (result != null) ? result.intValue() : 0;
}

@Override
public Map<String, Integer> getMonthlyExpenseTrend(int clientId) {
    String sql = "SELECT SUBSTRING(e.dateTime, 6, 2), SUM(e.amount) FROM Expense e " +
                 "WHERE e.client.id = :clientId " +
                 "GROUP BY SUBSTRING(e.dateTime, 6, 2) " +
                 "ORDER BY SUBSTRING(e.dateTime, 6, 2)";
    Query query = entityManager.createQuery(sql);
    query.setParameter("clientId", clientId);
    List<Object[]> results = query.getResultList();

    Map<String, Integer> monthlyTotals = new HashMap<>();
    for (Object[] row : results) {
        String month = (String) row[0];
        Number totalAmount = (Number) row[1];
        monthlyTotals.put(month, totalAmount.intValue());
    }

    return monthlyTotals;
}

    




}
