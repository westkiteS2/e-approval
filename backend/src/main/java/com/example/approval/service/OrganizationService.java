package com.example.approval.service;

import com.example.approval.domain.*;
import com.example.approval.dto.UserResponse;
import com.example.approval.exception.ApiException;
import com.example.approval.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class OrganizationService {
    private final UserRepository users;
    public OrganizationService(UserRepository users) { this.users = users; }
    @Transactional(readOnly = true)
    public List<UserResponse> all() { return users.findAll().stream().map(UserResponse::from).toList(); }
    private User find(Long id) { return users.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.")); }
    @Transactional
    public UserResponse changePosition(Long id, Position position, String actorLoginId) {
        User actor = users.findByLoginId(actorLoginId).orElseThrow();
        User target = find(id);
        if (target.getRole() == Role.SUPER_ADMIN || position == Position.CEO) throw new ApiException(HttpStatus.FORBIDDEN, "대표 직책은 변경할 수 없습니다.");
        if (position == Position.VICE_CEO && actor.getRole() != Role.SUPER_ADMIN) throw new ApiException(HttpStatus.FORBIDDEN, "부대표는 대표만 지정할 수 있습니다.");
        target.changePosition(position);
        return UserResponse.from(target);
    }
    @Transactional
    public UserResponse changeRole(Long id, Role role, String actorLoginId) {
        User actor = users.findByLoginId(actorLoginId).orElseThrow();
        User target = find(id);
        if (actor.getRole() != Role.SUPER_ADMIN || target.getRole() == Role.SUPER_ADMIN || role == Role.SUPER_ADMIN || target.getId().equals(actor.getId()))
            throw new ApiException(HttpStatus.FORBIDDEN, "최고 관리자 권한은 변경할 수 없습니다.");
        if (target.getStatus() != UserStatus.ACTIVE) throw new ApiException(HttpStatus.CONFLICT, "승인된 사용자만 권한을 변경할 수 있습니다.");
        target.changeRole(role);
        return UserResponse.from(target);
    }
}
