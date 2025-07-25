package com.SmartSpendExpense.dto.response;

import lombok.Data;

@Data
public class UserResponse {
    private String id;
    private String username;
    private String email;
    private Boolean blocked;
}
