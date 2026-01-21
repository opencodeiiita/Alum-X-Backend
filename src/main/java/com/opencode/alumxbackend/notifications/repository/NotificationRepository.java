package com.opencode.alumxbackend.notifications.repository;

import com.opencode.alumxbackend.notifications.model.Notification;
import com.opencode.alumxbackend.users.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
}
