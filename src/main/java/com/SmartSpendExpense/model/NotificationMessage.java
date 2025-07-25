package com.SmartSpendExpense.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
public class NotificationMessage {
    private String title;
    private String message;


}
