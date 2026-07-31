package com.expensetracker.service;

import com.expensetracker.dto.ExpenseRequest;
import com.expensetracker.dto.TotalResponse;
import com.expensetracker.exception.ExpenseNotFoundException;
import com.expensetracker.model.Expense;
import com.expensetracker.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExpenseServiceTest {

    private ExpenseService service;

    @BeforeEach
    void setUp() {
        // Fresh repository + service per test so tests don't leak state into each other.
        service = new ExpenseService(new ExpenseRepository());
    }

    private ExpenseRequest request(String title, String amount, String category, String date) {
        ExpenseRequest req = new ExpenseRequest();
        req.setTitle(title);
        req.setAmount(new BigDecimal(amount));
        req.setCategory(category);
        req.setDate(LocalDate.parse(date));
        return req;
    }

    @Test
    void addExpense_assignsIdAndPersists() {
        Expense saved = service.addExpense(request("Coffee", "150.00", "Food", "2026-07-01"));

        assertNotNull(saved.getId());
        assertEquals("Coffee", saved.getTitle());
        assertEquals(0, new BigDecimal("150.00").compareTo(saved.getAmount()));
    }

    @Test
    void getExpenses_returnsAllWhenNoFilter() {
        service.addExpense(request("Coffee", "150.00", "Food", "2026-07-01"));
        service.addExpense(request("Metro card", "500.00", "Transport", "2026-07-02"));

        List<Expense> all = service.getExpenses(null);

        assertEquals(2, all.size());
    }

    @Test
    void getExpenses_filtersByCategoryCaseInsensitive() {
        service.addExpense(request("Coffee", "150.00", "Food", "2026-07-01"));
        service.addExpense(request("Metro card", "500.00", "Transport", "2026-07-02"));

        List<Expense> filtered = service.getExpenses("food");

        assertEquals(1, filtered.size());
        assertEquals("Coffee", filtered.get(0).getTitle());
    }

    @Test
    void getExpenses_sortedByDateDescending() {
        service.addExpense(request("Old", "10.00", "Food", "2026-01-01"));
        service.addExpense(request("New", "10.00", "Food", "2026-07-01"));

        List<Expense> all = service.getExpenses(null);

        assertEquals("New", all.get(0).getTitle());
        assertEquals("Old", all.get(1).getTitle());
    }

    @Test
    void deleteExpense_removesExisting() {
        Expense saved = service.addExpense(request("Coffee", "150.00", "Food", "2026-07-01"));

        service.deleteExpense(saved.getId());

        assertTrue(service.getExpenses(null).isEmpty());
    }

    @Test
    void deleteExpense_throwsWhenIdDoesNotExist() {
        assertThrows(ExpenseNotFoundException.class, () -> service.deleteExpense(999L));
    }

    @Test
    void getTotals_overallSumsAllExpenses() {
        service.addExpense(request("Coffee", "150.00", "Food", "2026-07-01"));
        service.addExpense(request("Lunch", "250.00", "Food", "2026-07-02"));
        service.addExpense(request("Metro card", "500.00", "Transport", "2026-07-03"));

        TotalResponse totals = service.getTotals(null);

        assertEquals(0, new BigDecimal("900.00").compareTo(totals.getOverallTotal()));
        assertEquals(0, new BigDecimal("400.00").compareTo(totals.getByCategory().get("Food")));
        assertEquals(0, new BigDecimal("500.00").compareTo(totals.getByCategory().get("Transport")));
    }

    @Test
    void getTotals_withCategoryFilterReturnsThatCategoryAsOverall() {
        service.addExpense(request("Coffee", "150.00", "Food", "2026-07-01"));
        service.addExpense(request("Metro card", "500.00", "Transport", "2026-07-02"));

        TotalResponse totals = service.getTotals("Food");

        assertEquals(0, new BigDecimal("150.00").compareTo(totals.getOverallTotal()));
        // byCategory still shows the full breakdown for context
        assertEquals(2, totals.getByCategory().size());
    }

    @Test
    void getTotals_unknownCategoryReturnsZero() {
        service.addExpense(request("Coffee", "150.00", "Food", "2026-07-01"));

        TotalResponse totals = service.getTotals("Entertainment");

        assertEquals(0, BigDecimal.ZERO.compareTo(totals.getOverallTotal()));
    }
}
