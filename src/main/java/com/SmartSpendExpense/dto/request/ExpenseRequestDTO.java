package com.SmartSpendExpense.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class ExpenseRequestDTO {
    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    private BigDecimal amount;

    @NotBlank(message = "Category is required")
    private String category;

    @NotBlank(message = "Type is required")
    @Pattern(regexp = "EXPENSE|INCOME", message = "Type must be either 'EXPENSE' or 'INCOME'")
    private String type;

    @NotNull(message = "Date is required")
    private Date date;

    @Size(max = 250, message = "Description must be at most 250 characters")
    private String description;}
