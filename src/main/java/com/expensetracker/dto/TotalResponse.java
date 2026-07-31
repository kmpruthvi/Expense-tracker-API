package com.expensetracker.dto;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Response payload for the totals endpoint.
 * - overallTotal: sum of every expense currently stored.
 * - byCategory: sum of expenses grouped by category (always the full breakdown,
 *   regardless of whether a specific category filter was applied).
 */
public class TotalResponse {

    private BigDecimal overallTotal;
    private Map<String, BigDecimal> byCategory;

    public TotalResponse() {
    }

    public TotalResponse(BigDecimal overallTotal, Map<String, BigDecimal> byCategory) {
        this.overallTotal = overallTotal;
        this.byCategory = byCategory;
    }

    public BigDecimal getOverallTotal() {
        return overallTotal;
    }

    public void setOverallTotal(BigDecimal overallTotal) {
        this.overallTotal = overallTotal;
    }

    public Map<String, BigDecimal> getByCategory() {
        return byCategory;
    }

    public void setByCategory(Map<String, BigDecimal> byCategory) {
        this.byCategory = byCategory;
    }
}
