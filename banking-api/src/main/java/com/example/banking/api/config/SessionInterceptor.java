package com.example.banking.api.config;

import com.example.banking.api.service.session.SessionManager;
import com.example.banking.api.domain.model.UserSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

@Component
public class SessionInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionInterceptor.class);
    
    @Autowired
    private SessionManager sessionManager;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        
        logger.debug("Intercepting request: {} {}", method, requestURI);
        
        // Skip session validation for:
        // 1. Login endpoint
        // 2. Registration endpoint
        // 3. Public endpoints (health, swagger, etc.)
        // 4. OPTIONS requests (CORS preflight)
        if (shouldSkipSessionValidation(requestURI, method)) {
            logger.debug("Skipping session validation for: {} {}", method, requestURI);
            return true;
        }
        
        // Get HTTP session
        HttpSession httpSession = request.getSession(false);
        if (httpSession == null) {
            logger.warn("No HTTP session found for request: {} {}", method, requestURI);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"No active session found\"}");
            response.setContentType("application/json");
            return false;
        }
        
        // Get user session
        Optional<UserSession> userSessionOpt = sessionManager.getSession(httpSession);
        if (userSessionOpt.isEmpty()) {
            logger.warn("No valid user session found for sessionId: {}", httpSession.getId());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Invalid or expired session\"}");
            response.setContentType("application/json");
            return false;
        }
        
        UserSession userSession = userSessionOpt.get();
        
        // Add user session to request attributes for controllers to use
        request.setAttribute("userSession", userSession);
        
        logger.debug("Session validation successful for user: {} with sessionId: {}", 
                    userSession.getUsername(), userSession.getSessionId());
        
        return true;
    }
    
    private boolean shouldSkipSessionValidation(String requestURI, String method) {
        // Skip OPTIONS requests (CORS preflight)
        if ("OPTIONS".equals(method)) {
            return true;
        }
        
        // Skip login and registration endpoints
        if (requestURI.matches(".*/api/v1/banking/(login|register)")) {
            return true;
        }
        
        // Skip public endpoints
        if (requestURI.matches(".*/api/v1/banking/health") ||
            requestURI.matches(".*/health") ||
            requestURI.matches(".*/actuator/.*") ||
            requestURI.matches(".*/swagger-ui.*") ||
            requestURI.matches(".*/api-docs.*") ||
            requestURI.matches(".*/v3/api-docs.*") ||
            requestURI.matches(".*/webjars/.*") ||
            requestURI.matches(".*/favicon.ico") ||
            requestURI.matches(".*/error")) {
            return true;
        }
        
        return false;
    }
}