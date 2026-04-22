package com.campus.smart_campus.modules.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final GoogleOAuth2SuccessHandler googleOAuth2SuccessHandler;
    private final GoogleOAuth2FailureHandler googleOAuth2FailureHandler;

    public SecurityConfig(
            GoogleOAuth2SuccessHandler googleOAuth2SuccessHandler,
            GoogleOAuth2FailureHandler googleOAuth2FailureHandler
    ) {
        this.googleOAuth2SuccessHandler = googleOAuth2SuccessHandler;
        this.googleOAuth2FailureHandler = googleOAuth2FailureHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            ObjectProvider<ClientRegistrationRepository> clientRegistrationRepositoryProvider
    ) throws Exception {
        var builder = http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/auth/**", "/oauth2/**", "/login/oauth2/**"))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/favicon.ico",
                                "/assets/**",
                                "/oauth2/**",
                                "/login/oauth2/**",
                                "/api/auth/**",
                                "/auth/**",
                                "/api/health",
                                "/health"
                        ).permitAll()
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                ;

        if (clientRegistrationRepositoryProvider.getIfAvailable() != null) {
            builder.oauth2Login(oauth2 -> oauth2
                    .successHandler(googleOAuth2SuccessHandler)
                    .failureHandler(googleOAuth2FailureHandler)
                    .userInfoEndpoint(Customizer.withDefaults())
            );
        }

        return builder.build();
    }
}

