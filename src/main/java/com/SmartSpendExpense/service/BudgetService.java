package com.SmartSpendExpense.service;

import com.SmartSpendExpense.dto.request.BudgetRequestDTO;
import com.SmartSpendExpense.dto.response.BudgetResponseDTO;
import com.SmartSpendExpense.model.Budget;
import com.SmartSpendExpense.repository.BudgetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BudgetService {
    @Autowired
    private BudgetRepository budgetRepository;
    @Autowired
    private ExpenseService expenseService;

    public BudgetResponseDTO createOrUpdateBudget(BudgetRequestDTO dto, String userId) {
        Optional<Budget> opt = budgetRepository.findByUserIdAndCategoryAndMonthAndYear(
                userId, dto.getCategory(), dto.getMonth(), dto.getYear());
        Budget budget = opt.orElse(new Budget());
        budget.setUserId(userId);
        budget.setCategory(dto.getCategory());
        budget.setMonth(dto.getMonth());
        budget.setYear(dto.getYear());
        budget.setLimitAmount(dto.getLimitAmount());
        if (budget.getId() == null) {
            budget.setCreatedAt(new Date());
        }
        Budget saved = budgetRepository.save(budget);
        // Call the budget check for this category/month/year
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, saved.getYear());
        cal.set(Calendar.MONTH, saved.getMonth() - 1); // Java months are 0-based
        cal.set(Calendar.DAY_OF_MONTH, 1); // Any day in that month
        Date budgetMonthDate = cal.getTime();
        expenseService.checkBudgetAndNotify(userId, budget.getCategory(), new Date()); // Use current month/year

        return toResponseDTO(saved);
    }

    public List<BudgetResponseDTO> getBudgets(String userId) {
        return budgetRepository.findByUserId(userId)
                .stream().map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // Utility
    private BudgetResponseDTO toResponseDTO(Budget budget) {
        BudgetResponseDTO dto = new BudgetResponseDTO();
        dto.setId(budget.getId());
        dto.setCategory(budget.getCategory());
        dto.setMonth(budget.getMonth());
        dto.setYear(budget.getYear());
        dto.setLimitAmount(budget.getLimitAmount());
        return dto;
    }
}
