package com.SmartSpendExpense.service;

import com.SmartSpendExpense.dto.request.BudgetRequestDTO;
import com.SmartSpendExpense.dto.response.BudgetResponseDTO;
import com.SmartSpendExpense.model.Budget;
import com.SmartSpendExpense.repository.BudgetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class BudgetServiceTest {

    @InjectMocks
    private BudgetService budgetService;

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private ExpenseService expenseService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateOrUpdateBudget_NewBudget() {
        String userId = "u123";
        BudgetRequestDTO dto = new BudgetRequestDTO();
        dto.setCategory("Food");
        dto.setMonth(7);
        dto.setYear(2025);
        dto.setLimitAmount(BigDecimal.valueOf(1500));

        when(budgetRepository.findByUserIdAndCategoryAndMonthAndYear(
                eq(userId), eq("Food"), eq(7), eq(2025)))
                .thenReturn(Optional.empty());

        // Capture the budget saved
        ArgumentCaptor<Budget> budgetCaptor = ArgumentCaptor.forClass(Budget.class);
        when(budgetRepository.save(budgetCaptor.capture()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // ExpenseService call: we just verify it's called, ignore its logic
        doNothing().when(expenseService).checkBudgetAndNotify(any(), any(), any());

        BudgetResponseDTO result = budgetService.createOrUpdateBudget(dto, userId);

        Budget savedBudget = budgetCaptor.getValue();
        assertThat(savedBudget.getUserId()).isEqualTo(userId);
        assertThat(savedBudget.getCategory()).isEqualTo("Food");
        assertThat(savedBudget.getLimitAmount()).isEqualTo(BigDecimal.valueOf(1500));

        // Check returned DTO
        assertThat(result.getCategory()).isEqualTo("Food");
        assertThat(result.getLimitAmount()).isEqualTo(BigDecimal.valueOf(1500));
        assertThat(result.getMonth()).isEqualTo(7);
        assertThat(result.getYear()).isEqualTo(2025);

        // Verify dependencies called
        verify(expenseService).checkBudgetAndNotify(eq(userId), eq("Food"), any());
    }

    @Test
    void testCreateOrUpdateBudget_UpdateExistingBudget() {
        String userId = "u123";
        BudgetRequestDTO dto = new BudgetRequestDTO();
        dto.setCategory("Food");
        dto.setMonth(7);
        dto.setYear(2025);
        dto.setLimitAmount(BigDecimal.valueOf(2500));

        Budget existing = new Budget();
        existing.setId("b123");
        existing.setUserId(userId);
        existing.setCategory("Food");
        existing.setMonth(7);
        existing.setYear(2025);
        existing.setLimitAmount(BigDecimal.valueOf(1200));

        when(budgetRepository.findByUserIdAndCategoryAndMonthAndYear(
                eq(userId), eq("Food"), eq(7), eq(2025)))
                .thenReturn(Optional.of(existing));
        when(budgetRepository.save(any(Budget.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(expenseService).checkBudgetAndNotify(any(), any(), any());

        BudgetResponseDTO result = budgetService.createOrUpdateBudget(dto, userId);

        assertThat(result.getCategory()).isEqualTo("Food");
        assertThat(result.getLimitAmount()).isEqualTo(BigDecimal.valueOf(2500));
        assertThat(result.getMonth()).isEqualTo(7);
        assertThat(result.getYear()).isEqualTo(2025);
        verify(expenseService).checkBudgetAndNotify(eq(userId), eq("Food"), any());
    }

    @Test
    void testGetBudgets() {
        String userId = "u321";
        Budget budget1 = new Budget();
        budget1.setId("b1");
        budget1.setUserId(userId);
        budget1.setCategory("Snacks");
        budget1.setMonth(7);
        budget1.setYear(2025);
        budget1.setLimitAmount(BigDecimal.valueOf(500));

        when(budgetRepository.findByUserId(userId)).thenReturn(Collections.singletonList(budget1));

        List<BudgetResponseDTO> dtos = budgetService.getBudgets(userId);
        assertThat(dtos).hasSize(1);
        assertThat(dtos.get(0).getCategory()).isEqualTo("Snacks");
        assertThat(dtos.get(0).getLimitAmount()).isEqualTo(BigDecimal.valueOf(500));
    }
}
