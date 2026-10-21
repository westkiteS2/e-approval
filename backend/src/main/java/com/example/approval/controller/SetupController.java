package com.example.approval.controller;

import com.example.approval.domain.*;
import com.example.approval.dto.UserResponse;
import com.example.approval.exception.ApiException;
import com.example.approval.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@RestController
@RequestMapping("/api/setup")
public class SetupController {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final String setupToken;
    public SetupController(UserRepository users, PasswordEncoder encoder, @Value("${app.setup.token:}") String setupToken) {
        this.users = users; this.encoder = encoder; this.setupToken = setupToken;
    }
    public record SetupStatus(boolean available) {}
    @GetMapping("/status") public SetupStatus status() {
        return new SetupStatus(!setupToken.isBlank() && !users.existsByRole(Role.SUPER_ADMIN));
    }
    public record CeoRequest(
        @NotBlank String setupToken,
        @NotBlank @Pattern(regexp="^[a-zA-Z0-9_]{4,20}$") String loginId,
        @NotBlank @Size(min=8,max=64) String password,
        @NotBlank @Size(max=30) String name,
        @NotBlank @Size(max=50) String department
    ) {}
    @PostMapping("/ceo")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public synchronized UserResponse register(@Valid @RequestBody CeoRequest req) {
        if (setupToken.isBlank() || users.existsByRole(Role.SUPER_ADMIN))
            throw new ApiException(HttpStatus.CONFLICT, "최초 대표 등록은 이미 완료되었거나 비활성화되었습니다.");
        if (!MessageDigest.isEqual(setupToken.getBytes(StandardCharsets.UTF_8), req.setupToken().getBytes(StandardCharsets.UTF_8)))
            throw new ApiException(HttpStatus.FORBIDDEN, "초기 설정 토큰이 올바르지 않습니다.");
        if (users.existsByLoginId(req.loginId())) throw new ApiException(HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다.");
        User ceo = new User(req.loginId(), encoder.encode(req.password()), req.name(), req.department(), Position.CEO, Role.SUPER_ADMIN, UserStatus.ACTIVE);
        return UserResponse.from(users.save(ceo));
    }
}
