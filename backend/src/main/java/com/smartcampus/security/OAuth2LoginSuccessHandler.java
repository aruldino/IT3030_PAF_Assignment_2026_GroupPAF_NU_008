package com.smartcampus.security;

import com.smartcampus.enums.UserRole;
import com.smartcampus.enums.UserStatus;
import com.smartcampus.model.User;
import com.smartcampus.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Called automatically after Google OAuth2 login succeeds.
 * Saves user to DB (if first login), generates JWT, redirects to frontend.
 * Member 4 responsibility.
 */
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");

        // Save user to DB on first login, otherwise load existing
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = User.builder()
                    .email(email)
                    .name(name)
                    .profilePicture(picture)
                    .role(UserRole.USER)
                    .status(UserStatus.PENDING)
                    .build();
            return userRepository.save(newUser);
        });

        // Generate JWT token
        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());

        // Redirect to React frontend with token and status
        getRedirectStrategy().sendRedirect(request, response,
                "http://localhost:3000/auth/callback?token=" + token
                        + "&status=" + user.getStatus().name());
    }
}