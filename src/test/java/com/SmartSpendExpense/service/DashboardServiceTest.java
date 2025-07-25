package com.SmartSpendExpense.service;

import com.SmartSpendExpense.dto.request.MonthlySummaryDTO;
import com.SmartSpendExpense.model.Budget;
import com.SmartSpendExpense.model.Expense;
import com.SmartSpendExpense.repository.BudgetRepository;
import com.SmartSpendExpense.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class DashboardServiceTest {

    @InjectMocks
    private DashboardService dashboardService;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetMonthlySummary_basic() {
        String userId = "user1";
        int month = 7, year = 2025;

        // Prepare test expenses
        List<Expense> expenses = Arrays.asList(
                createExpense(userId, "Food", BigDecimal.valueOf(200)),
                createExpense(userId, "Transport", BigDecimal.valueOf(150)),
                createExpense(userId, "Food", BigDecimal.valueOf(100))
        );

        // Prepare test budgets
        List<Budget> budgets = Arrays.asList(
                createBudget(userId, "Food", month, year, BigDecimal.valueOf(500)),
                createBudget(userId, "Transport", month, year, BigDecimal.valueOf(300))
        );

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        // Mocks
        when(expenseRepository.findByUserIdAndDateBetween(userId, start, end)).thenReturn(expenses);
        when(budgetRepository.findByUserIdAndMonthAndYear(userId, month, year)).thenReturn(budgets);

        MonthlySummaryDTO summary = dashboardService.getMonthlySummary(userId, month, year);

        assertThat(summary).isNotNull();
        assertThat(summary.getTotalSpent()).isEqualTo(BigDecimal.valueOf(450));
        assertThat(summary.getCategoryTotals())
                .containsEntry("Food", BigDecimal.valueOf(300))
                .containsEntry("Transport", BigDecimal.valueOf(150));
        assertThat(summary.getBudgets())
                .containsEntry("Food", BigDecimal.valueOf(500))
                .containsEntry("Transport", BigDecimal.valueOf(300));
    }

    @Test
    void testGetMonthlySummary_empty() {
        String userId = "userX";
        int month = 8, year = 2024;

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        when(expenseRepository.findByUserIdAndDateBetween(userId, start, end)).thenReturn(Collections.emptyList());
        when(budgetRepository.findByUserIdAndMonthAndYear(userId, month, year)).thenReturn(Collections.emptyList());

        MonthlySummaryDTO summary = dashboardService.getMonthlySummary(userId, month, year);

        assertThat(summary.getTotalSpent()).isEqualTo(BigDecimal.ZERO);
        assertThat(summary.getCategoryTotals()).isEmpty();
        assertThat(summary.getBudgets()).isEmpty();
    }

    // Helper methods to create test entities
    private Expense createExpense(String userId, String category, BigDecimal amount) {
        Expense e = new Expense();
        e.setUserId(userId);
        e.setCategory(category);
        e.setAmount(amount);
        return e;
    }

    private Budget createBudget(String userId, String category, int month, int year, BigDecimal limit) {
        Budget b = new Budget();
        b.setUserId(userId);
        b.setCategory(category);
        b.setMonth(month);
        b.setYear(year);
        b.setLimitAmount(limit);
        return b;
    }
}
