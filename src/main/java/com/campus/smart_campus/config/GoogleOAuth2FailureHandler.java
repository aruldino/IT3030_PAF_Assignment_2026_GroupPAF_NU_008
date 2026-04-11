package com.campus.smart_campus.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
public class GoogleOAuth2FailureHandler implements AuthenticationFailureHandler {

    private final String frontendFailureUrl;

    public GoogleOAuth2FailureHandler(
            @Value("${app.oauth2.google.frontend-failure-url:http://localhost:5173/?oauth2=error}") String frontendFailureUrl
    ) {
        this.frontendFailureUrl = frontendFailureUrl;
    }

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException, ServletException {
        String message = exception.getMessage() == null ? "Google sign-in failed." : exception.getMessage();
        response.sendRedirect(appendQueryParam(frontendFailureUrl, "message", URLEncoder.encode(message, StandardCharsets.UTF_8)));
    }

    private String appendQueryParam(String url, String key, String value) {
        String delimiter = url.contains("?") ? "&" : "?";
        return url + delimiter + key + "=" + value;
    }
}
