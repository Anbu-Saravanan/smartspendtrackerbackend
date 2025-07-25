package com.SmartSpendExpense.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

// MonthlySummaryDTO.java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonthlySummaryDTO {
    private int month;
    private int year;
    private BigDecimal totalSpent;
    private Map<String, BigDecimal> categoryTotals;
    private Map<String, BigDecimal> budgets;
}
