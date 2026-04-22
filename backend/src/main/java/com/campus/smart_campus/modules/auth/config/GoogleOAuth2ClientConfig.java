package com.campus.smart_campus.modules.auth.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

@Configuration
@ConditionalOnProperty(prefix = "app.oauth2.google", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(GoogleOAuth2Properties.class)
public class GoogleOAuth2ClientConfig {

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(
            GoogleOAuth2Properties properties
    ) {
        ClientRegistration registration = CommonOAuth2Provider.GOOGLE
                .getBuilder("google")
                .clientId(properties.getClientId())
                .clientSecret(properties.getClientSecret())
                .redirectUri(properties.getRedirectUri())
                .build();

        return new InMemoryClientRegistrationRepository(registration);
    }
}

