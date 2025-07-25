package com.SmartSpendExpense.repository;

import com.SmartSpendExpense.model.Budget;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends MongoRepository<Budget, String> {
    List<Budget> findByUserId(String userId);
    Optional<Budget> findByUserIdAndCategoryAndMonthAndYear(String userId, String category, int month, int year);
    List<Budget> findByUserIdAndMonthAndYear(String userId, int month, int year);
}
