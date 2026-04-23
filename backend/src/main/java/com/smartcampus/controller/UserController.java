package com.smartcampus.controller;

import com.smartcampus.enums.UserRole;
import com.smartcampus.model.User;
import com.smartcampus.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * User management controller — Member 4 responsibility.
 * Base path: /api/users
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** GET /api/users — List all users (ADMIN only) */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /** GET /api/users/pending-count — Get count of pending users (ADMIN only) */
    @GetMapping("/pending-count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> getPendingCount() {
        return ResponseEntity.ok(Map.of("count", userService.getPendingCount()));
    }

    /** GET /api/users/{id} — Get user profile by ID */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    /** PUT /api/users/{id}/role — Change user role (ADMIN only) */
    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> updateUserRole(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        UserRole newRole = UserRole.valueOf(body.get("role").toUpperCase());
        return ResponseEntity.ok(userService.updateUserRole(id, newRole));
    }

    /** PUT /api/users/{id}/approve — Approve a user (ADMIN only) */
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> approveUser(@PathVariable String id) {
        return ResponseEntity.ok(userService.approveUser(id));
    }

    /** PUT /api/users/{id}/suspend — Suspend a user (ADMIN only) */
    @PutMapping("/{id}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> suspendUser(@PathVariable String id) {
        return ResponseEntity.ok(userService.suspendUser(id));
    }

    /** PUT /api/users/{id}/reactivate — Reactivate a user (ADMIN only) */
    @PutMapping("/{id}/reactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> reactivateUser(@PathVariable String id) {
        return ResponseEntity.ok(userService.reactivateUser(id));
    }

    /** PUT /api/users/profile — Update own profile name */
    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<User> updateMyProfile(
            @RequestBody Map<String, String> body) {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User user = userService.updateMyName(email, body.get("name"));
        return ResponseEntity.ok(user);
    }

    /** DELETE /api/users/{id} — Remove user (ADMIN only) */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}