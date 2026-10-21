package com.example.approval.notification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/notifications")
public class NotificationController {
 private final NotificationService service;
 public NotificationController(NotificationService service){this.service=service;}
 @GetMapping public List<NotificationService.Item> list(Authentication auth){return service.list(auth.getName());}
 @PatchMapping("/{id}/read") @ResponseStatus(HttpStatus.NO_CONTENT)
 public void read(Authentication auth,@PathVariable Long id){service.read(auth.getName(),id);}
}
