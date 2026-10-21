package com.example.approval.controller;

import com.example.approval.domain.*;
import com.example.approval.dto.UserResponse;
import com.example.approval.service.OrganizationService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class OrganizationController {
    private final OrganizationService service;
    public OrganizationController(OrganizationService service) { this.service = service; }
    @GetMapping("/all") public List<UserResponse> all() { return service.all(); }
    public record PositionUpdate(Position position) {}
    public record RoleUpdate(Role role) {}
    @PatchMapping("/{id}/position") public UserResponse position(@PathVariable Long id, @RequestBody PositionUpdate body, Authentication auth) {
        return service.changePosition(id, body.position(), auth.getName());
    }
    @PatchMapping("/{id}/role") public UserResponse role(@PathVariable Long id, @RequestBody RoleUpdate body, Authentication auth) {
        return service.changeRole(id, body.role(), auth.getName());
    }
}
