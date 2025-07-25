package com.SmartSpendExpense.service;

import com.SmartSpendExpense.dto.request.ExpenseRequestDTO;
import com.SmartSpendExpense.dto.response.ExpenseResponseDTO;
import com.SmartSpendExpense.model.*;
import com.SmartSpendExpense.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mail.javamail.JavaMailSender;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ExpenseServiceTest {

    @InjectMocks
    private ExpenseService expenseService;

    @Mock
    private ExpenseRepository expenseRepository;
    @Mock
    private BudgetRepository budgetRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private JavaMailSender mailSender;
    @Mock
    private NotificationRepository notificationRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateExpense_shouldSaveExpenseAndNotify() {
        String userId = "user1";
        ExpenseRequestDTO dto = new ExpenseRequestDTO();
        dto.setTitle("Lunch");
        dto.setAmount(BigDecimal.valueOf(100));
        dto.setCategory("Food");
        dto.setType("EXPENSE");
        dto.setDate(new Date());
        dto.setDescription("Test expense");

        Expense saved = new Expense();
        saved.setId("exp123");
        saved.setUserId(userId);
        saved.setTitle("Lunch");
        saved.setAmount(dto.getAmount());
        saved.setCategory("Food");
        saved.setType("EXPENSE");
        saved.setDate(dto.getDate());
        saved.setDescription("Test expense");
        saved.setCreatedAt(new Date());

        when(expenseRepository.save(any(Expense.class))).thenReturn(saved);

        ExpenseResponseDTO resp = expenseService.createExpense(dto, userId);

        assertThat(resp.getId()).isEqualTo("exp123");
        assertThat(resp.getTitle()).isEqualTo("Lunch");
        verify(expenseRepository, times(1)).save(any(Expense.class));
        // Notifies for budget is covered but hard to assert due to mock/no-op
    }

    @Test
    void testGetExpenseById_foundAndMatchesUser() {
        Expense exp = new Expense();
        exp.setId("eid");
        exp.setUserId("user1");
        exp.setTitle("Test");

        when(expenseRepository.findById("eid")).thenReturn(Optional.of(exp));

        Optional<ExpenseResponseDTO> result = expenseService.getExpenseById("eid", "user1");
        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Test");
    }

    @Test
    void testGetExpenseById_notFoundOrUserMismatch() {
        Expense exp = new Expense();
        exp.setId("eid");
        exp.setUserId("otherUser");
        exp.setTitle("Test");

        when(expenseRepository.findById("eid")).thenReturn(Optional.of(exp));

        Optional<ExpenseResponseDTO> result = expenseService.getExpenseById("eid", "user1");
        assertThat(result).isNotPresent();
    }

    @Test
    void testUpdateExpense_success() {
        ExpenseRequestDTO dto = new ExpenseRequestDTO();
        dto.setTitle("Dinner");
        dto.setAmount(BigDecimal.valueOf(200));
        dto.setCategory("Food");
        dto.setType("EXPENSE");
        dto.setDate(new Date());
        dto.setDescription("Update desc");

        Expense existing = new Expense();
        existing.setId("eid");
        existing.setUserId("user1");
        existing.setTitle("Old");
        when(expenseRepository.findById("eid")).thenReturn(Optional.of(existing));
        when(expenseRepository.save(any(Expense.class))).thenReturn(existing);

        Optional<ExpenseResponseDTO> resp = expenseService.updateExpense("eid", dto, "user1");

        assertThat(resp).isPresent();
        assertThat(resp.get().getTitle()).isEqualTo("Dinner");
        verify(expenseRepository, times(1)).save(any(Expense.class));
    }

    @Test
    void testUpdateExpense_noMatch() {
        when(expenseRepository.findById("eid")).thenReturn(Optional.empty());
        ExpenseRequestDTO dto = new ExpenseRequestDTO();
        Optional<ExpenseResponseDTO> resp = expenseService.updateExpense("eid", dto, "user1");
        assertThat(resp).isNotPresent();
    }

    @Test
    void testDeleteExpense_success() {
        Expense e = new Expense();
        e.setId("eid");
        e.setUserId("user1");
        when(expenseRepository.findById("eid")).thenReturn(Optional.of(e));
        boolean result = expenseService.deleteExpense("eid", "user1");
        assertThat(result).isTrue();
        verify(expenseRepository, times(1)).deleteById("eid");
    }

    @Test
    void testDeleteExpense_notFoundOrMismatch() {
        when(expenseRepository.findById("eid")).thenReturn(Optional.empty());
        boolean result = expenseService.deleteExpense("eid", "user1");
        assertThat(result).isFalse();
        verify(expenseRepository, never()).deleteById(anyString());
    }

    @Test
    void testGetExpensesByUserId() {
        Expense e = new Expense();
        e.setId("eid");
        e.setUserId("user1");
        e.setTitle("Test");
        when(expenseRepository.findByUserId("user1")).thenReturn(List.of(e));
        List<ExpenseResponseDTO> dtos = expenseService.getExpensesByUserId("user1");
        assertThat(dtos).hasSize(1);
        assertThat(dtos.get(0).getTitle()).isEqualTo("Test");
    }

    // -- Optional: test budget warning logic & notification sending

    @Test
    void testCheckBudgetAndNotify_nearingAndExceeded() {
        String userId = "user1";
        String category = "Food";
        int month = 7, year = 2025;
        BigDecimal limit = BigDecimal.valueOf(1000);

        Budget budget = new Budget();
        budget.setUserId(userId);
        budget.setCategory(category);
        budget.setMonth(month);
        budget.setYear(year);
        budget.setLimitAmount(limit);

        when(budgetRepository.findByUserIdAndCategoryAndMonthAndYear(any(), any(), anyInt(), anyInt()))
                .thenReturn(Optional.of(budget));

        // Case 1: Nearing limit (800 spent)
        Expense e1 = new Expense(); e1.setAmount(BigDecimal.valueOf(300));
        Expense e2 = new Expense(); e2.setAmount(BigDecimal.valueOf(500));
        when(expenseRepository.findByUserIdAndCategoryAndMonthAndYear(any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(e1, e2));

        // should trigger nearing warning
        expenseService.checkBudgetAndNotify(userId, category, new Date());
        verify(notificationService, atLeastOnce()).sendGlobalNotification(any());

        // Case 2: Exceeded limit (1200 spent)
        Expense e3 = new Expense(); e3.setAmount(BigDecimal.valueOf(1200));
        when(expenseRepository.findByUserIdAndCategoryAndMonthAndYear(any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(e3));
        expenseService.checkBudgetAndNotify(userId, category, new Date());
        verify(notificationService, atLeastOnce()).sendGlobalNotification(any());
    }
}
