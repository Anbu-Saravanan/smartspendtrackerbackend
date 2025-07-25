package com.SmartSpendExpense.dto.request;

import lombok.Data;

@Data
public class OtpVerifyRequest {
    private String email;
    private String otp;
}
