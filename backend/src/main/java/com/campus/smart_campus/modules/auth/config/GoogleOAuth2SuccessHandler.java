package com.campus.smart_campus.modules.auth.config;

import com.campus.smart_campus.modules.auth.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class GoogleOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;
    private final String frontendSuccessUrl;
    private final String frontendFailureUrl;

    public GoogleOAuth2SuccessHandler(
            AuthService authService,
            @Value("${app.oauth2.google.frontend-success-url:http://localhost:5173/?oauth2=success}") String frontendSuccessUrl,
            @Value("${app.oauth2.google.frontend-failure-url:http://localhost:5173/?oauth2=error}") String frontendFailureUrl
    ) {
        this.authService = authService;
        this.frontendSuccessUrl = frontendSuccessUrl;
        this.frontendFailureUrl = frontendFailureUrl;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        try {
            if (!(authentication instanceof OAuth2AuthenticationToken oauth2Authentication)) {
                response.sendRedirect(buildFailureRedirect("Unsupported authentication type."));
                return;
            }

            Object principal = oauth2Authentication.getPrincipal();
            if (!(principal instanceof OAuth2User oauth2User)) {
                response.sendRedirect(buildFailureRedirect("Google profile data could not be loaded."));
                return;
            }

            Map<String, Object> attributes = oauth2User.getAttributes();
            String email = readAttribute(attributes, "email");
            if (email.isBlank()) {
                response.sendRedirect(buildFailureRedirect("Google account did not provide an email address."));
                return;
            }

            Boolean emailVerified = readBooleanAttribute(attributes, "email_verified");
            if (emailVerified != null && !emailVerified) {
                response.sendRedirect(buildFailureRedirect("Google email address is not verified."));
                return;
            }

            String fullName = readAttribute(attributes, "name");
            if (fullName.isBlank()) {
                String givenName = readAttribute(attributes, "given_name");
                String familyName = readAttribute(attributes, "family_name");
                fullName = (givenName + " " + familyName).trim();
            }

            authService.loginOrCreateGoogleUser(fullName, email, request.getSession(true));
            response.sendRedirect(frontendSuccessUrl);
        } catch (Exception exception) {
            response.sendRedirect(buildFailureRedirect(exception.getMessage() == null ? "Google sign-in failed." : exception.getMessage()));
        }
    }

    private String buildFailureRedirect(String message) {
        return appendQueryParam(frontendFailureUrl, "message", URLEncoder.encode(message, StandardCharsets.UTF_8));
    }

    private String readAttribute(Map<String, Object> attributes, String key) {
        Object value = attributes.get(key);
        return value == null ? "" : value.toString().trim();
    }

    private Boolean readBooleanAttribute(Map<String, Object> attributes, String key) {
        Object value = attributes.get(key);
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        if (value == null) {
            return null;
        }
        return Boolean.parseBoolean(value.toString());
    }

    private String appendQueryParam(String url, String key, String value) {
        String delimiter = url.contains("?") ? "&" : "?";
        return url + delimiter + key + "=" + value;
    }
}


