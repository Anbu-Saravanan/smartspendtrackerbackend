package com.SmartSpendExpense.controller;

import com.SmartSpendExpense.dto.request.BudgetRequestDTO;
import com.SmartSpendExpense.dto.response.BudgetResponseDTO;
import com.SmartSpendExpense.model.User;
import com.SmartSpendExpense.repository.BudgetRepository;
import com.SmartSpendExpense.repository.UserRepository;
import com.SmartSpendExpense.service.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class BudgetController {
    @Autowired
    private BudgetService budgetService;


    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private UserRepository userRepository;

    private String getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email;
        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else {
            email = principal.toString();
        }
        return userRepository.findByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @PostMapping("/set")
    public ResponseEntity<BudgetResponseDTO> setBudget(@RequestBody BudgetRequestDTO dto) {
        BudgetResponseDTO created = budgetService.createOrUpdateBudget(dto, getCurrentUserId());
        return ResponseEntity.ok(created);
    }

    @GetMapping("/all")
    public ResponseEntity<List<BudgetResponseDTO>> getBudgets() {
        return ResponseEntity.ok(budgetService.getBudgets(getCurrentUserId()));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteBudget(@PathVariable String id) {
        budgetRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
