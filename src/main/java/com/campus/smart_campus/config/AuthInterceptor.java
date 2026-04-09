package com.campus.smart_campus.config;

import com.campus.smart_campus.exception.UnauthorizedException;
import com.campus.smart_campus.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        String path = request.getRequestURI();

        if (!path.startsWith("/api/")) {
            return true;
        }

        // Allow public endpoints and OPTIONS
        if (path.startsWith("/api/auth/")
                || path.startsWith("/api/health")
                || path.startsWith("/api/resources")
                || path.startsWith("/api/bookings")
                || path.startsWith("/api/maintenance")
                || "OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(AuthService.SESSION_USER_ID) == null) {
            throw new UnauthorizedException("Please log in to access this page.");
        }

        return true;
    }
}
