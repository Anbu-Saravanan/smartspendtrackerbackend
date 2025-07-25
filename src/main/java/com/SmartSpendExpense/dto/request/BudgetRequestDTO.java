package com.SmartSpendExpense.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class BudgetRequestDTO {
    private String category;
    private int month;
    private int year;
    private BigDecimal limitAmount;
}
