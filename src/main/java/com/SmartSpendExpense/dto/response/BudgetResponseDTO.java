package com.SmartSpendExpense.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class BudgetResponseDTO {
    private String id;
    private String category;
    private int month;
    private int year;
    private BigDecimal limitAmount;
}
