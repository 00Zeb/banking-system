package com.example.banking.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for the banking application process.
 */
@Component
@ConfigurationProperties(prefix = "banking.application")
public class BankingApplicationProperties {
    
    private String jarPath = "../banking-application/target/banking-application-1.0-SNAPSHOT.jar";
    private long processTimeout = 30000; // 30 seconds
    private String javaCommand = "java";
    
    public String getJarPath() {
        return jarPath;
    }
    
    public void setJarPath(String jarPath) {
        this.jarPath = jarPath;
    }
    
    public long getProcessTimeout() {
        return processTimeout;
    }
    
    public void setProcessTimeout(long processTimeout) {
        this.processTimeout = processTimeout;
    }
    
    public String getJavaCommand() {
        return javaCommand;
    }
    
    public void setJavaCommand(String javaCommand) {
        this.javaCommand = javaCommand;
    }
}
