package com.example.banking.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConfigController {

    @Value("${banking.api.base-url}")
    private String apiBaseUrl;

    @GetMapping("/api/config")
    public ConfigResponse getConfig() {
        return new ConfigResponse(apiBaseUrl);
    }

    public static class ConfigResponse {
        private String apiBaseUrl;

        public ConfigResponse(String apiBaseUrl) {
            this.apiBaseUrl = apiBaseUrl;
        }

        public String getApiBaseUrl() {
            return apiBaseUrl;
        }

        public void setApiBaseUrl(String apiBaseUrl) {
            this.apiBaseUrl = apiBaseUrl;
        }
    }
}