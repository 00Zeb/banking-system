package com.example.banking.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.CodeSource;
import java.util.jar.JarFile;

/**
 * Service to locate the banking-application JAR file from Maven dependencies.
 * This eliminates the need for file system navigation and works consistently
 * across different execution contexts (tests, runtime, different working directories).
 */
@Service
public class JarLocatorService {
    
    private static final Logger logger = LoggerFactory.getLogger(JarLocatorService.class);
    
    private String jarPath;
    
    @PostConstruct
    public void init() {
        try {
            jarPath = locateBankingApplicationJar();
            logger.info("Banking application JAR located at: {}", jarPath);
        } catch (Exception e) {
            logger.error("Failed to locate banking application JAR", e);
            throw new RuntimeException("Could not locate banking application JAR", e);
        }
    }
    
    /**
     * Get the path to the banking application JAR file.
     */
    public String getJarPath() {
        return jarPath;
    }
    
    /**
     * Locate the banking-application JAR file.
     * Prioritize file system since that's where the executable JAR with dependencies is.
     */
    private String locateBankingApplicationJar() throws IOException {
        // First, try the file system approach to find the executable JAR
        String jarFromFileSystem = findJarInFileSystem();
        if (jarFromFileSystem != null) {
            return jarFromFileSystem;
        }

        // If not found in file system, try to find the JAR in the classpath
        String jarFromClasspath = findJarInClasspath();
        if (jarFromClasspath != null) {
            return jarFromClasspath;
        }

        // If not found in classpath, try to extract from resources
        String jarFromResources = extractJarFromResources();
        if (jarFromResources != null) {
            return jarFromResources;
        }

        throw new IOException("Banking application JAR not found in any location");
    }
    
    /**
     * Try to find the JAR file in the classpath.
     */
    private String findJarInClasspath() {
        try {
            // Look for the banking application main class
            Class<?> bankingAppClass = Class.forName("com.example.banking.BankingApp");
            CodeSource codeSource = bankingAppClass.getProtectionDomain().getCodeSource();
            
            if (codeSource != null) {
                URL location = codeSource.getLocation();
                File file = new File(location.toURI());
                
                if (file.exists() && file.getName().endsWith(".jar")) {
                    logger.debug("Found banking application JAR in classpath: {}", file.getAbsolutePath());
                    return file.getAbsolutePath();
                }
            }
        } catch (Exception e) {
            logger.debug("Could not find JAR in classpath: {}", e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Try to extract the JAR from resources (if packaged within this JAR).
     */
    private String extractJarFromResources() {
        try {
            // Look for the JAR as a resource
            InputStream jarStream = getClass().getClassLoader()
                .getResourceAsStream("banking-application-1.0-SNAPSHOT-jar-with-dependencies.jar");
            
            if (jarStream == null) {
                // Try alternative names
                jarStream = getClass().getClassLoader()
                    .getResourceAsStream("banking-application-1.0-SNAPSHOT.jar");
            }
            
            if (jarStream != null) {
                // Create a temporary file
                Path tempFile = Files.createTempFile("banking-application", ".jar");
                Files.copy(jarStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
                
                // Make sure it's a valid JAR
                try (JarFile jarFile = new JarFile(tempFile.toFile())) {
                    logger.debug("Extracted banking application JAR to: {}", tempFile.toAbsolutePath());
                    return tempFile.toAbsolutePath().toString();
                }
            }
        } catch (Exception e) {
            logger.debug("Could not extract JAR from resources: {}", e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Fallback: try to find the JAR in the file system using relative paths.
     * Prioritize the jar-with-dependencies version since that's the executable one.
     */
    private String findJarInFileSystem() {
        String[] possiblePaths = {
            "../banking-application/target/banking-application-1.0-SNAPSHOT-jar-with-dependencies.jar",
            "banking-application/target/banking-application-1.0-SNAPSHOT-jar-with-dependencies.jar",
            "../banking-application/target/banking-application-1.0-SNAPSHOT.jar",
            "banking-application/target/banking-application-1.0-SNAPSHOT.jar",
            "./banking-application-1.0-SNAPSHOT-jar-with-dependencies.jar",
            "./banking-application-1.0-SNAPSHOT.jar"
        };
        
        for (String path : possiblePaths) {
            File file = new File(path);
            if (file.exists() && file.isFile()) {
                logger.debug("Found banking application JAR in file system: {}", file.getAbsolutePath());
                return file.getAbsolutePath();
            }
        }
        
        logger.debug("Banking application JAR not found in file system");
        return null;
    }
    
    /**
     * Verify that the JAR file exists and is accessible.
     */
    public boolean isJarAccessible() {
        if (jarPath == null) {
            return false;
        }
        
        File jarFile = new File(jarPath);
        return jarFile.exists() && jarFile.isFile() && jarFile.canRead();
    }
    
    /**
     * Get information about the located JAR file.
     */
    public String getJarInfo() {
        if (jarPath == null) {
            return "JAR not located";
        }
        
        File jarFile = new File(jarPath);
        return String.format("JAR: %s (size: %d bytes, exists: %s, readable: %s)",
            jarFile.getAbsolutePath(),
            jarFile.length(),
            jarFile.exists(),
            jarFile.canRead());
    }
}
