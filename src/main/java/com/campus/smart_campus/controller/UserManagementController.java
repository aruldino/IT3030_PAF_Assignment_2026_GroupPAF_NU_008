package com.campus.smart_campus.controller;

import com.campus.smart_campus.dto.UserProfileResponse;
import com.campus.smart_campus.service.AuthService;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserManagementController {

    private final AuthService authService;

    public UserManagementController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping
    public List<UserProfileResponse> listUsers(HttpSession session) {
        return authService.listUsers(session);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> deleteUser(@PathVariable Long id, HttpSession session) {
        authService.deleteUserById(id, session);
        return Map.of("message", "User account deleted successfully.");
    }
}
