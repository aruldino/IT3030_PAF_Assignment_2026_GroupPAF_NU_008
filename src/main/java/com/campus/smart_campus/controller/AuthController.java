package com.campus.smart_campus.controller;

import com.campus.smart_campus.dto.LoginRequest;
import com.campus.smart_campus.dto.RegisterRequest;
import com.campus.smart_campus.dto.UserProfileResponse;
import com.campus.smart_campus.config.GoogleOAuth2Properties;
import com.campus.smart_campus.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/auth", "/auth"})
public class AuthController {

    private final AuthService authService;
    private final GoogleOAuth2Properties googleOAuth2Properties;

    public AuthController(AuthService authService, GoogleOAuth2Properties googleOAuth2Properties) {
        this.authService = authService;
        this.googleOAuth2Properties = googleOAuth2Properties;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserProfileResponse register(@Valid @RequestBody RegisterRequest request, HttpSession session) {
        return authService.register(request, session);
    }

    @PostMapping("/login")
    public UserProfileResponse login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        return authService.login(request, session);
    }

    @PostMapping("/login/oauth")
    public UserProfileResponse oauthLogin(@Valid @RequestBody LoginRequest request, HttpSession session) {
        return authService.login(request, session);
    }

    @GetMapping("/google")
    public void googleLogin(HttpServletRequest request, HttpSession session, HttpServletResponse response) throws IOException {
        if (googleOAuth2Properties.isDevAdminShortcutEnabled()) {
            authService.loginAsAdminShortcut(
                    googleOAuth2Properties.getDevAdminName(),
                    googleOAuth2Properties.getDevAdminEmail(),
                    session
            );
            response.sendRedirect(googleOAuth2Properties.getFrontendSuccessUrl());
            return;
        }

        boolean clientConfigured = !googleOAuth2Properties.getClientId().isBlank()
                && !googleOAuth2Properties.getClientSecret().isBlank();

        if (!googleOAuth2Properties.isEnabled() || !clientConfigured) {
            String messageText = "Google login is not configured on this server.";
            if (!googleOAuth2Properties.getApiKey().isBlank() && !clientConfigured) {
                messageText = "GOOGLE_API_KEY alone is not enough. Configure GOOGLE_CLIENT_ID and GOOGLE_CLIENT_SECRET in .env.";
            }

            String message = URLEncoder.encode(
                    messageText,
                    StandardCharsets.UTF_8
            );
            String delimiter = googleOAuth2Properties.getFrontendFailureUrl().contains("?") ? "&" : "?";
            response.sendRedirect(googleOAuth2Properties.getFrontendFailureUrl() + delimiter + "message=" + message);
            return;
        }

        response.sendRedirect(resolveAuthorizationUrl(request, googleOAuth2Properties.getAuthorizationUrl()));
    }

    private String resolveAuthorizationUrl(HttpServletRequest request, String configuredAuthorizationUrl) {
        String rawUrl = configuredAuthorizationUrl == null ? "" : configuredAuthorizationUrl.trim();
        if (rawUrl.isBlank()) {
            rawUrl = "/oauth2/authorization/google";
        }

        if (rawUrl.startsWith("http://") || rawUrl.startsWith("https://")) {
            return rawUrl;
        }

        String path = rawUrl.startsWith("/") ? rawUrl : "/" + rawUrl;
        String scheme = request.getScheme();
        int port = request.getServerPort();
        boolean defaultPort = ("http".equalsIgnoreCase(scheme) && port == 80)
                || ("https".equalsIgnoreCase(scheme) && port == 443);

        String host = defaultPort
                ? scheme + "://" + request.getServerName()
                : scheme + "://" + request.getServerName() + ":" + port;

        return host + path;
    }

    @GetMapping("/me")
    public UserProfileResponse me(HttpSession session) {
        return authService.getCurrentUser(session);
    }

    @PostMapping("/logout")
    public Map<String, String> logout(HttpSession session) {
        authService.logout(session);
        return Map.of("message", "Logged out successfully.");
    }

    @DeleteMapping("/me")
    public Map<String, String> deleteMyAccount(HttpSession session) {
        authService.deleteCurrentUser(session);
        return Map.of("message", "Your account has been deleted.");
    }
}
