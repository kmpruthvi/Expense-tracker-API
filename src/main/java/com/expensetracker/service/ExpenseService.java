package com.expensetracker.service;

import com.expensetracker.dto.ExpenseRequest;
import com.expensetracker.dto.TotalResponse;
import com.expensetracker.exception.ExpenseNotFoundException;
import com.expensetracker.model.Expense;
import com.expensetracker.repository.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    private final ExpenseRepository repository;

    public ExpenseService(ExpenseRepository repository) {
        this.repository = repository;
    }

    public Expense addExpense(ExpenseRequest request) {
        Expense expense = new Expense(
                null,
                request.getTitle(),
                request.getAmount(),
                request.getCategory(),
                request.getDate()
        );
        return repository.save(expense);
    }

    /**
     * Returns all expenses, optionally filtered (case-insensitive) by category.
     * Results are sorted by date descending (most recent first) for readability.
     */
    public List<Expense> getExpenses(String categoryFilter) {
        return repository.findAll().stream()
                .filter(e -> categoryFilter == null
                        || e.getCategory().equalsIgnoreCase(categoryFilter))
                .sorted(Comparator.comparing(Expense::getDate).reversed())
                .collect(Collectors.toList());
    }

    public void deleteExpense(Long id) {
        boolean removed = repository.deleteById(id);
        if (!removed) {
            throw new ExpenseNotFoundException(id);
        }
    }

    /**
     * Computes the overall total and a per-category breakdown.
     * If categoryFilter is provided, overallTotal reflects just that category's
     * total (matching the "total by category" requirement), while byCategory
     * still shows the full breakdown for context.
     */
    public TotalResponse getTotals(String categoryFilter) {
        Map<String, BigDecimal> byCategory = repository.findAll().stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
                ));

        BigDecimal overallTotal;
        if (categoryFilter != null) {
            overallTotal = byCategory.entrySet().stream()
                    .filter(entry -> entry.getKey().equalsIgnoreCase(categoryFilter))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse(BigDecimal.ZERO);
        } else {
            overallTotal = byCategory.values().stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        return new TotalResponse(overallTotal, byCategory);
    }
}
