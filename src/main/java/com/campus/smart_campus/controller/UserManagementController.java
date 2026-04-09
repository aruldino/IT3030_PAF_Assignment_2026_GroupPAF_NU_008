package com.campus.smart_campus.controller;

import com.campus.smart_campus.dto.UserProfileResponse;
import com.campus.smart_campus.service.AuthService;
import jakarta.servlet.http.HttpSession;
import com.campus.smart_campus.dto.RoleUpdateRequest;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/users", "/users"})
public class UserManagementController {

    private final AuthService authService;

    public UserManagementController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping
    public List<UserProfileResponse> listUsers(HttpSession session) {
        return authService.listUsers(session);
    }

    @GetMapping("/me")
    public UserProfileResponse me(HttpSession session) {
        return authService.getCurrentUser(session);
    }

    @PutMapping("/role")
    public UserProfileResponse updateRole(@RequestBody RoleUpdateRequest request, HttpSession session) {
        return authService.updateUserRole(request.userId(), request.role(), session);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> deleteUser(@PathVariable Long id, HttpSession session) {
        authService.deleteUserById(id, session);
        return Map.of("message", "User account deleted successfully.");
    }
}
