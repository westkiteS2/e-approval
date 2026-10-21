package com.example.approval.notification;
import com.example.approval.domain.User;
import com.example.approval.exception.ApiException;
import com.example.approval.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
@Service
public class NotificationService {
 private final NotificationRepository notifications; private final UserRepository users;
 public NotificationService(NotificationRepository notifications,UserRepository users){this.notifications=notifications;this.users=users;}
 public void send(User recipient,Long documentId,String message){notifications.save(new Notification(recipient,documentId,message));}
 private User me(String loginId){return users.findByLoginId(loginId).orElseThrow(()->new ApiException(HttpStatus.UNAUTHORIZED,"로그인이 필요합니다."));}
 public record Item(Long id,Long documentId,String message,boolean read,LocalDateTime createdAt){}
 @Transactional(readOnly=true) public List<Item> list(String loginId){return notifications.findTop50ByRecipientOrderByCreatedAtDesc(me(loginId)).stream().map(n->new Item(n.getId(),n.getDocumentId(),n.getMessage(),n.isReadFlag(),n.getCreatedAt())).toList();}
 @Transactional public void read(String loginId,Long id){Notification n=notifications.findById(id).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"알림을 찾을 수 없습니다."));if(!n.getRecipient().getId().equals(me(loginId).getId()))throw new ApiException(HttpStatus.NOT_FOUND,"알림을 찾을 수 없습니다.");n.markRead();}
}
