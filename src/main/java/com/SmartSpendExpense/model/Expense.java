package com.SmartSpendExpense.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Document(collection = "expenses")
public class Expense {
    @Id
    private String id;
    private String userId;      // Reference to User (as String)
    private String category;    // Category name (embedded info)
    private String title;
    private BigDecimal amount;
    private String type;        // "EXPENSE" or "INCOME"
    private Date date;          // When it occurred
    private String description;
    private Date createdAt;
}
