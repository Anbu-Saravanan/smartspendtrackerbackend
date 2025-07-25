package com.SmartSpendExpense.repository;

import com.SmartSpendExpense.model.Expense;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface ExpenseRepository extends MongoRepository<Expense, String> {
    List<Expense> findByUserId(String userId);
    // Find by userId and optional filters
    List<Expense> findByUserIdAndCategoryContainingIgnoreCaseAndTypeContainingIgnoreCaseAndDateBetween(
            String userId, String category, Date startDate, Date endDate);

    @Query("{'userId': ?0, 'category': ?1, $expr: { $and: [ { $eq: [ { $month: '$date' }, ?2 ] }, { $eq: [ { $year: '$date' }, ?3 ] } ] } }")
    List<Expense> findByUserIdAndCategoryAndMonthAndYear(String userId, String category, int month, int year);

    List<Expense> findByUserIdAndDateBetween(String userId, LocalDate start, LocalDate end);
}