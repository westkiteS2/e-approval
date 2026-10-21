package com.example.approval.notification;
import com.example.approval.domain.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="notifications")
public class Notification {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) private User recipient;
 @Column(nullable=false,length=200) private String message;
 private Long documentId;
 @Column(nullable=false) private boolean readFlag=false;
 @Column(nullable=false) private LocalDateTime createdAt=LocalDateTime.now();
 protected Notification() {}
 public Notification(User recipient,Long documentId,String message) {this.recipient=recipient;this.documentId=documentId;this.message=message;}
 public Long getId(){return id;} public User getRecipient(){return recipient;} public String getMessage(){return message;}
 public Long getDocumentId(){return documentId;} public boolean isReadFlag(){return readFlag;} public LocalDateTime getCreatedAt(){return createdAt;}
 public void markRead(){readFlag=true;}
}
