package com.example.banking.api.service;

import com.example.banking.api.config.BankingApplicationProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.io.*;
import java.util.concurrent.TimeUnit;

/**
 * Diagnostic test to understand what's happening with process communication.
 */
@EnabledIfSystemProperty(named = "integration.tests", matches = "true")
class ProcessDiagnosticTest {

    @Test
    void diagnoseProcessOutput() throws Exception {
        JarLocatorService jarLocatorService = new JarLocatorService();
        jarLocatorService.init();
        
        BankingApplicationProperties properties = new BankingApplicationProperties();
        
        System.out.println("=== Starting Banking Application Process ===");
        System.out.println("JAR: " + jarLocatorService.getJarInfo());
        
        ProcessBuilder processBuilder = new ProcessBuilder(
            properties.getJavaCommand(), 
            "-jar", 
            jarLocatorService.getJarPath()
        );
        
        Process process = processBuilder.start();
        
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
             BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
             BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            
            System.out.println("=== Process started, reading initial output ===");
            
            // Read initial output for 5 seconds
            long startTime = System.currentTimeMillis();
            StringBuilder allOutput = new StringBuilder();
            
            while (System.currentTimeMillis() - startTime < 5000) {
                // Check for regular output
                while (reader.ready()) {
                    String line = reader.readLine();
                    if (line != null) {
                        allOutput.append("STDOUT: ").append(line).append("\n");
                        System.out.println("STDOUT: " + line);
                    }
                }
                
                // Check for error output
                while (errorReader.ready()) {
                    String line = errorReader.readLine();
                    if (line != null) {
                        allOutput.append("STDERR: ").append(line).append("\n");
                        System.out.println("STDERR: " + line);
                    }
                }
                
                Thread.sleep(100);
            }
            
            System.out.println("\n=== Sending input: 1 (login) ===");
            writer.write("1\n");
            writer.flush();
            
            // Read response for 3 seconds
            startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < 3000) {
                while (reader.ready()) {
                    String line = reader.readLine();
                    if (line != null) {
                        allOutput.append("AFTER_1: ").append(line).append("\n");
                        System.out.println("AFTER_1: " + line);
                    }
                }
                Thread.sleep(100);
            }
            
            System.out.println("\n=== Sending username: testuser ===");
            writer.write("testuser\n");
            writer.flush();
            
            // Read response for 3 seconds
            startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < 3000) {
                while (reader.ready()) {
                    String line = reader.readLine();
                    if (line != null) {
                        allOutput.append("AFTER_USER: ").append(line).append("\n");
                        System.out.println("AFTER_USER: " + line);
                    }
                }
                Thread.sleep(100);
            }
            
            System.out.println("\n=== Sending password: password123 ===");
            writer.write("password123\n");
            writer.flush();
            
            // Read response for 3 seconds
            startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < 3000) {
                while (reader.ready()) {
                    String line = reader.readLine();
                    if (line != null) {
                        allOutput.append("AFTER_PASS: ").append(line).append("\n");
                        System.out.println("AFTER_PASS: " + line);
                    }
                }
                Thread.sleep(100);
            }
            
            System.out.println("\n=== Sending exit: 3 ===");
            writer.write("3\n");
            writer.flush();
            
            // Final read
            startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < 2000) {
                while (reader.ready()) {
                    String line = reader.readLine();
                    if (line != null) {
                        allOutput.append("FINAL: ").append(line).append("\n");
                        System.out.println("FINAL: " + line);
                    }
                }
                Thread.sleep(100);
            }
            
            System.out.println("\n=== Process Status ===");
            System.out.println("Process alive: " + process.isAlive());
            
            // Wait for process to complete
            boolean finished = process.waitFor(2, TimeUnit.SECONDS);
            System.out.println("Process finished: " + finished);
            System.out.println("Exit code: " + (finished ? process.exitValue() : "N/A"));
            
            System.out.println("\n=== Complete Output ===");
            System.out.println(allOutput.toString());
            
        } finally {
            if (process.isAlive()) {
                System.out.println("Force killing process...");
                process.destroyForcibly();
                process.waitFor(1, TimeUnit.SECONDS);
            }
        }
    }
}
