package com.example.approval.notification;
import com.example.approval.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface NotificationRepository extends JpaRepository<Notification,Long> {
 List<Notification> findTop50ByRecipientOrderByCreatedAtDesc(User recipient);
}
