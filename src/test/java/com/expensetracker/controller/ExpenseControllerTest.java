package com.expensetracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end tests hitting real HTTP endpoints via MockMvc, backed by the
 * actual in-memory repository (no mocks) to exercise the full request flow.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String expenseJson(String title, String amount, String category, String date) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "title", title,
                "amount", amount,
                "category", category,
                "date", date
        ));
    }

    @Test
    void addExpense_returns201WithCreatedResource() throws Exception {
        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(expenseJson("Groceries", "1200.50", "Food", "2026-07-15")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Groceries"))
                .andExpect(jsonPath("$.category").value("Food"));
    }

    @Test
    void addExpense_missingTitleReturns400() throws Exception {
        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(expenseJson("", "100", "Food", "2026-07-15")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.title").exists());
    }

    @Test
    void addExpense_negativeAmountReturns400() throws Exception {
        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(expenseJson("Refund", "-50", "Food", "2026-07-15")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.amount").exists());
    }

    @Test
    void getExpenses_filterByCategory() throws Exception {
        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(expenseJson("Bus pass", "300", "Transport", "2026-07-10")))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/expenses").param("category", "Transport"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("Transport"));
    }

    @Test
    void deleteExpense_thenGetTotals_reflectsRemoval() throws Exception {
        String response = mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(expenseJson("Movie", "400", "Entertainment", "2026-07-20")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/expenses/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/expenses/total").param("category", "Entertainment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overallTotal").value(0));
    }

    @Test
    void deleteExpense_nonExistentIdReturns404() throws Exception {
        mockMvc.perform(delete("/api/expenses/{id}", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void getTotals_overallAndByCategory() throws Exception {
        mockMvc.perform(post("/api/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(expenseJson("Snacks", "100", "Food", "2026-07-05")));
        mockMvc.perform(post("/api/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(expenseJson("Taxi", "250", "Transport", "2026-07-06")));

        mockMvc.perform(get("/api/expenses/total"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overallTotal").exists())
                .andExpect(jsonPath("$.byCategory.Food").exists())
                .andExpect(jsonPath("$.byCategory.Transport").exists());
    }
}
