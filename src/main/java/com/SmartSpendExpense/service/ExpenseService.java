package com.SmartSpendExpense.service;

import com.SmartSpendExpense.dto.request.ExpenseRequestDTO;
import com.SmartSpendExpense.dto.response.ExpenseResponseDTO;
import com.SmartSpendExpense.model.*;
import com.SmartSpendExpense.repository.BudgetRepository;
import com.SmartSpendExpense.repository.ExpenseRepository;
import com.SmartSpendExpense.repository.NotificationRepository;
import com.SmartSpendExpense.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ExpenseService {
    @Autowired
    private ExpenseRepository expenseRepository;
    @Autowired
    private BudgetRepository budgetRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private NotificationRepository notificationRepository;

    // CREATE
    public ExpenseResponseDTO createExpense(ExpenseRequestDTO dto, String userId) {
        Expense expense = new Expense();
        expense.setUserId(userId);
        expense.setTitle(dto.getTitle());
        expense.setAmount(dto.getAmount());
        expense.setCategory(dto.getCategory());
        expense.setType(dto.getType());
        expense.setDate(dto.getDate());
        expense.setDescription(dto.getDescription());
        expense.setCreatedAt(new Date());

        Expense saved = expenseRepository.save(expense);
        checkBudgetAndNotify(userId, dto.getCategory(), saved.getDate());
        return toResponseDTO(saved);
    }

    //send notifications

    public void checkBudgetAndNotify(String userId, String category, Date expenseDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(expenseDate);
        int month = cal.get(Calendar.MONTH) + 1;
        int year = cal.get(Calendar.YEAR);

        Optional<Budget> opt = budgetRepository.findByUserIdAndCategoryAndMonthAndYear(userId, category, month, year);
        if (opt.isPresent()) {
            Budget budget = opt.get();

            // Get all expenses for user/category/month/year
            List<Expense> expenses = expenseRepository.findByUserIdAndCategoryAndMonthAndYear(userId, category, month, year);
            BigDecimal totalSpent = expenses.stream()
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal limit = budget.getLimitAmount();
            if (totalSpent.compareTo(limit) >= 0) {
                // EXCEEDED
                sendBudgetNotification(userId, category, month, year, limit, totalSpent, true);
                saveInAppNotification(userId, category, month, year, limit, totalSpent, true);
                // SEND REAL-TIME NOTIFICATION
                notificationService.sendGlobalNotification(
                        new NotificationMessage(
                                "Budget Limit Exceeded",
                                "You have exceeded your budget for " + category +
                                        " (" + month + "/" + year + "). " +
                                        "Budget: " + limit + ", Spent: " + totalSpent
                        )
                );
            } else if (totalSpent.compareTo(limit.multiply(new BigDecimal("0.8"))) >= 0) {
                // NEARING LIMIT (80%)
                sendBudgetNotification(userId, category, month, year, limit, totalSpent, false);
                saveInAppNotification(userId, category, month, year, limit, totalSpent, false);
                // SEND REAL-TIME NOTIFICATION
                notificationService.sendGlobalNotification(
                        new NotificationMessage(
                                "Budget Nearing Limit",
                                "You are nearing your budget for " + category +
                                        " (" + month + "/" + year + "). " +
                                        "Budget: " + limit + ", Spent: " + totalSpent
                        )
                );
            }
        }
    }

    public void sendBudgetNotification(String userId, String category, int month, int year,
                                       BigDecimal limit, BigDecimal spent, boolean exceeded) {
        String email = userRepository.findById(userId).map(User::getEmail).orElse(null);
        if (email == null) return;
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(email);
        msg.setSubject(exceeded ? "Budget Limit Exceeded" : "Budget Limit Warning");
        msg.setText(
                "Category: " + category + " (" + month + "/" + year + ")\n" +
                        "Budget: " + limit + "\n" +
                        "Spent: " + spent + "\n" +
                        (exceeded ? "You have exceeded your budget!" : "You are nearing your budget limit (80%)!")
        );
        mailSender.send(msg);
    }

    public void saveInAppNotification(String userId, String category, int month, int year,
                                      BigDecimal limit, BigDecimal spent, boolean exceeded) {
        Notification notif = new Notification();
        notif.setUserId(userId);
        notif.setCreatedAt(new Date());
        notif.setRead(false);
        notif.setMessage(
                (exceeded ? "Budget Exceeded! " : "Budget Nearing Limit! ") +
                        "Category: " + category + " (" + month + "/" + year + "). " +
                        "Budget: " + limit + ", Spent: " + spent
        );
        notificationRepository.save(notif);
    }

    // READ ALL
    public List<ExpenseResponseDTO> getExpensesByUserId(String userId) {
        return expenseRepository.findByUserId(userId)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // READ ONE
    public Optional<ExpenseResponseDTO> getExpenseById(String id, String userId) {
        return expenseRepository.findById(id)
                .filter(e -> e.getUserId().equals(userId))
                .map(this::toResponseDTO);
    }

    // UPDATE
    public Optional<ExpenseResponseDTO> updateExpense(String id, ExpenseRequestDTO dto, String userId) {
        Optional<Expense> expenseOpt = expenseRepository.findById(id);
        if (expenseOpt.isPresent() && expenseOpt.get().getUserId().equals(userId)) {
            Expense expense = expenseOpt.get();
            expense.setTitle(dto.getTitle());
            expense.setAmount(dto.getAmount());
            expense.setCategory(dto.getCategory());
            expense.setType(dto.getType());
            expense.setDate(dto.getDate());
            expense.setDescription(dto.getDescription());
            Expense saved = expenseRepository.save(expense);
            checkBudgetAndNotify(userId, dto.getCategory(), saved.getDate());
            return Optional.of(toResponseDTO(saved));
        }
        return Optional.empty();
    }

    // DELETE
    public boolean deleteExpense(String id, String userId) {
        Optional<Expense> expenseOpt = expenseRepository.findById(id);
        if (expenseOpt.isPresent() && expenseOpt.get().getUserId().equals(userId)) {
            expenseRepository.deleteById(id);
            return true;
        }
        return false;
    }






    // Entity -> DTO mapping
    public ExpenseResponseDTO toResponseDTO(Expense expense) {
        ExpenseResponseDTO dto = new ExpenseResponseDTO();
        dto.setId(expense.getId());
        dto.setTitle(expense.getTitle());
        dto.setAmount(expense.getAmount());
        dto.setCategory(expense.getCategory());
        dto.setType(expense.getType());
        dto.setDate(expense.getDate());
        dto.setDescription(expense.getDescription());
        return dto;
    }
}
