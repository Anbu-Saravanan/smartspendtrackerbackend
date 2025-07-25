package com.SmartSpendExpense.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String userId;
    private String email;
    private String refreshToken;
    private String role; // <-- add this
}
