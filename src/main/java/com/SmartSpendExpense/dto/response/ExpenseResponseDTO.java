package com.SmartSpendExpense.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class ExpenseResponseDTO {
    private String id;
    private String title;
    private BigDecimal amount;
    private String category;
    private String type;
    private Date date;
    private String description;
}
