package com.example.banking.api.service;

import com.example.banking.api.config.BankingApplicationProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.io.*;

/**
 * Test to see raw output from the banking application.
 */
@EnabledIfSystemProperty(named = "integration.tests", matches = "true")
class RawOutputTest {

    @Test
    void testRawOutput() throws Exception {
        JarLocatorService jarLocatorService = new JarLocatorService();
        jarLocatorService.init();
        
        BankingApplicationProperties properties = new BankingApplicationProperties();
        
        System.out.println("=== RAW OUTPUT TEST ===");
        System.out.println("JAR: " + jarLocatorService.getJarInfo());
        
        ProcessBuilder processBuilder = new ProcessBuilder(
            properties.getJavaCommand(), 
            "-jar", 
            jarLocatorService.getJarPath()
        );
        
        // Try without environment variables first
        System.out.println("Starting process WITHOUT environment variables...");
        
        Process process = processBuilder.start();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
             BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
            
            System.out.println("Process started, reading for 5 seconds...");
            
            // Read for 5 seconds
            long startTime = System.currentTimeMillis();
            StringBuilder allOutput = new StringBuilder();
            
            while (System.currentTimeMillis() - startTime < 5000) {
                // Check stdout
                while (reader.ready()) {
                    int ch = reader.read();
                    if (ch != -1) {
                        allOutput.append((char) ch);
                        System.out.print((char) ch);
                    }
                }
                
                // Check stderr
                while (errorReader.ready()) {
                    int ch = errorReader.read();
                    if (ch != -1) {
                        System.err.print((char) ch);
                    }
                }
                
                Thread.sleep(100);
            }
            
            System.out.println("\n=== RAW OUTPUT (as bytes) ===");
            byte[] bytes = allOutput.toString().getBytes();
            for (int i = 0; i < Math.min(bytes.length, 200); i++) {
                System.out.printf("%02X ", bytes[i]);
                if ((i + 1) % 16 == 0) System.out.println();
            }
            
            System.out.println("\n=== RAW OUTPUT (as string) ===");
            System.out.println("[" + allOutput.toString() + "]");
            
            System.out.println("\n=== Sending '3' to exit ===");
            writer.write("3\n");
            writer.flush();
            
            // Wait a bit more
            Thread.sleep(2000);
            
            // Read any final output
            while (reader.ready()) {
                int ch = reader.read();
                if (ch != -1) {
                    System.out.print((char) ch);
                }
            }
            
        } finally {
            if (process.isAlive()) {
                System.out.println("\nKilling process...");
                process.destroyForcibly();
            }
        }
        
        System.out.println("\n=== RAW OUTPUT TEST END ===");
    }
}
