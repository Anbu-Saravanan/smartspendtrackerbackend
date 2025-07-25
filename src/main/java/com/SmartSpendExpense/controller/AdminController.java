package com.SmartSpendExpense.controller;

import com.SmartSpendExpense.dto.response.ExpenseResponseDTO;
import com.SmartSpendExpense.dto.response.UserResponse;
import com.SmartSpendExpense.model.User;
import com.SmartSpendExpense.repository.ExpenseRepository;
import com.SmartSpendExpense.repository.UserRepository;
import com.SmartSpendExpense.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')") // Secure all endpoints here for ADMIN only
public class AdminController {

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/users")
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
    }

    private UserResponse toUserResponse(User user) {
        UserResponse resp = new UserResponse();
        resp.setId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setEmail(user.getEmail());
        // Optionally add more fields
        return resp;
    }

    @Autowired
    private ExpenseRepository expenseRepository;

    @GetMapping("/expenses")
    public List<ExpenseResponseDTO> getAllExpenses() {
        return expenseRepository.findAll()
                .stream()
                .map(expenseService::toResponseDTO)
                .collect(Collectors.toList());
    }
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok("User deleted.");
    }

    // Optional: Block user
    @PutMapping("/users/block/{id}")
    public ResponseEntity<?> blockUser(@PathVariable String id) {
        Optional<User> opt = userRepository.findById(id);
        if (opt.isPresent()) {
            User user = opt.get();
            user.setBlocked(true); // Add blocked field in User class
            userRepository.save(user);
            return ResponseEntity.ok("User blocked.");
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/users/toggle-block/{id}")
    public ResponseEntity<?> toggleBlockUser(@PathVariable String id) {
        Optional<User> opt = userRepository.findById(id);
        if (opt.isPresent()) {
            User user = opt.get();
            user.setBlocked(!Boolean.TRUE.equals(user.getBlocked()));
            userRepository.save(user);
            return ResponseEntity.ok(user.getBlocked() ? "User blocked." : "User unblocked.");
        }
        return ResponseEntity.notFound().build();
    }




}
