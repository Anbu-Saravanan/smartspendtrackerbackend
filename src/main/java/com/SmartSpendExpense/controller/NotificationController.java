package com.SmartSpendExpense.controller;

import com.SmartSpendExpense.model.NotificationMessage;
import com.SmartSpendExpense.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/live-notifications")
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class NotificationController {

    @Autowired
    private  NotificationService notificationService;

    @PostMapping("/global")
    public String notifyAll(@RequestBody NotificationMessage msg) {
        notificationService.sendGlobalNotification(msg);
        return "Notification sent!";
    }
}
