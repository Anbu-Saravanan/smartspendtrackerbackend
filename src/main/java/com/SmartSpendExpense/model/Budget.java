package com.SmartSpendExpense.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.Date;

@Document(collection = "budgets")
@Data
public class Budget {
    @Id
    private String id;
    private String userId;       // Linked to User
    private String category;     // E.g., "Food"
    private int month;           // 1 to 12
    private int year;            // E.g., 2025
    private BigDecimal limitAmount;
    private Date createdAt;
    private Date updatedAt;
}
