package com.campus.smart_campus.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.oauth2.google")
public class GoogleOAuth2Properties {

    private boolean enabled;
    private boolean devAdminShortcutEnabled;
    private String clientId = "";
    private String clientSecret = "";
    private String apiKey = "";
    private String redirectUri = "{baseUrl}/login/oauth2/code/google";
    private String authorizationUrl = "http://localhost:8080/oauth2/authorization/google";
    private String frontendSuccessUrl = "http://localhost:5173/?oauth2=success";
    private String frontendFailureUrl = "http://localhost:5173/?oauth2=error";
    private String devAdminEmail = "admin@smartcampus.local";
    private String devAdminName = "Campus Admin";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isDevAdminShortcutEnabled() {
        return devAdminShortcutEnabled;
    }

    public void setDevAdminShortcutEnabled(boolean devAdminShortcutEnabled) {
        this.devAdminShortcutEnabled = devAdminShortcutEnabled;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getAuthorizationUrl() {
        return authorizationUrl;
    }

    public void setAuthorizationUrl(String authorizationUrl) {
        this.authorizationUrl = authorizationUrl;
    }

    public String getFrontendSuccessUrl() {
        return frontendSuccessUrl;
    }

    public void setFrontendSuccessUrl(String frontendSuccessUrl) {
        this.frontendSuccessUrl = frontendSuccessUrl;
    }

    public String getFrontendFailureUrl() {
        return frontendFailureUrl;
    }

    public void setFrontendFailureUrl(String frontendFailureUrl) {
        this.frontendFailureUrl = frontendFailureUrl;
    }

    public String getDevAdminEmail() {
        return devAdminEmail;
    }

    public void setDevAdminEmail(String devAdminEmail) {
        this.devAdminEmail = devAdminEmail;
    }

    public String getDevAdminName() {
        return devAdminName;
    }

    public void setDevAdminName(String devAdminName) {
        this.devAdminName = devAdminName;
    }
}
