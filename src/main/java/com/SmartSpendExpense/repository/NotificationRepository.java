package com.SmartSpendExpense.repository;

import com.SmartSpendExpense.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByUserIdAndReadFalse(String userId);

    List<Notification> findByUserId(String currentUserId);
}
