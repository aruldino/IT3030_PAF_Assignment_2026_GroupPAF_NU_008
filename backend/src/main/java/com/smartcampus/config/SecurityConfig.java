package com.smartcampus.config;

import com.smartcampus.security.JwtAuthFilter;
import com.smartcampus.security.OAuth2LoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security Configuration — Member 4 responsibility.
 * Implements OAuth2 Google login + JWT stateless authentication.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtAuthFilter jwtAuthFilter;
        private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

        @Value("${app.oauth.failure-url:http://localhost:3000/login?error=oauth}")
        private String oauthFailureUrl;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(AbstractHttpConfigurer::disable)
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/api/auth/**").permitAll()
                                                .requestMatchers("/oauth2/**", "/login/**").permitAll()
                                                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                                                .anyRequest().authenticated())
                                .oauth2Login(oauth2 -> oauth2
                                                .successHandler(oAuth2LoginSuccessHandler)
                                                .failureUrl(oauthFailureUrl))
                                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }
}