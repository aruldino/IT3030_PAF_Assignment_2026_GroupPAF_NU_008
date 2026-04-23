package com.smartcampus.controller;

import com.smartcampus.model.User;
import com.smartcampus.repository.UserRepository;
import com.smartcampus.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication REST controller — Member 4 responsibility.
 * Base path: /api/auth
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    /** GET /api/auth/me — Get the currently authenticated user's profile */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        return userRepository.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** POST /api/auth/logout — Logout (client-side token removal) */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok().build();
    }

    /**
     * POST /api/auth/test-token — FOR TESTING ONLY, remove before production.
     * Lets teammates get a JWT without going through Google login.
     * Usage: POST /api/auth/test-token?email=test@gmail.com&role=USER
     */
    @PostMapping("/test-token")
    public ResponseEntity<String> getTestToken(
            @RequestParam String email,
            @RequestParam(defaultValue = "USER") String role) {
        String token = jwtService.generateToken(email, role);
        return ResponseEntity.ok(token);
    }
}