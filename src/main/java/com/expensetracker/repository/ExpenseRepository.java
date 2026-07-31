package com.expensetracker.repository;

import com.expensetracker.model.Expense;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory store for expenses. No database is required per the assignment
 * brief; a ConcurrentHashMap keeps this thread-safe for concurrent requests.
 * Data does not persist across restarts by design.
 */
@Repository
public class ExpenseRepository {

    private final Map<Long, Expense> store = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(0);

    public Expense save(Expense expense) {
        long id = idSequence.incrementAndGet();
        expense.setId(id);
        store.put(id, expense);
        return expense;
    }

    public Collection<Expense> findAll() {
        return store.values();
    }

    public Optional<Expense> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    /**
     * @return true if an expense with this id existed and was removed.
     */
    public boolean deleteById(Long id) {
        return store.remove(id) != null;
    }
}
