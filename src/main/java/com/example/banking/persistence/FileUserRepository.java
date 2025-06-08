package com.example.banking.persistence;

import com.example.banking.User;
import com.example.banking.Transaction;
import com.example.banking.Account;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * File-based implementation of UserRepository.
 * Stores users and their transactions in a serialized file.
 */
public class FileUserRepository implements UserRepository {
    private static final String DATA_FILE = "banking_data.ser";
    private Map<String, User> userCache;

    public FileUserRepository() {
        this.userCache = new HashMap<>();
        loadData();
        
        // Register shutdown hook to save data when application exits
        Runtime.getRuntime().addShutdownHook(new Thread(this::saveData));
    }

    @Override
    public void saveUser(User user) {
        userCache.put(user.getUsername(), user);
        saveData();
    }
    
    @Override
    public void updateUser(User user) {
        // This method is called when a user's state changes (e.g., after a transaction)
        if (userCache.containsKey(user.getUsername())) {
            userCache.put(user.getUsername(), user);
            saveData();
        }
    }

    @Override
    public User getUserByUsername(String username) {
        return userCache.get(username);
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(userCache.values());
    }

    @Override
    public boolean deleteUser(String username) {
        if (userCache.containsKey(username)) {
            userCache.remove(username);
            saveData();
            return true;
        }
        return false;
    }
    
    @Override
    public void saveAllUsers() {
        saveData();
    }

    /**
     * Loads user data from file.
     */
    @SuppressWarnings("unchecked")
    private void loadData() {
        File file = new File(DATA_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                userCache = (Map<String, User>) ois.readObject();
                System.out.println("Loaded " + userCache.size() + " users from storage.");
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error loading user data: " + e.getMessage());
                userCache = new HashMap<>();
            }
        }
    }

    /**
     * Saves user data to file.
     */
    private void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(userCache);
            System.out.println("Saved " + userCache.size() + " users to storage.");
        } catch (IOException e) {
            System.err.println("Error saving user data: " + e.getMessage());
        }
    }
}