package com.example.banking.persistence;

import com.example.banking.user.User;
import com.example.banking.domain.Transaction;
import com.example.banking.domain.Account;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * File-based implementation of UserRepository.
 * Stores users and their transactions in a serialized file.
 */
public class FileUserRepository implements UserRepository, AutoCloseable {
    private final String dataFile;
    private Map<String, User> userCache;

    public FileUserRepository() {
        this("banking_data.ser");
    }

    public FileUserRepository(String dataFile) {
        this.dataFile = dataFile;
        this.userCache = new HashMap<>();
        loadData();
    }

    @Override
    public void saveUser(User user) {
        userCache.put(user.getUsername(), user);
        saveData();
    }
    
    @Override
    public void updateUser(User user) {
        if (user != null && userCache.containsKey(user.getUsername())) {
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
        File file = new File(dataFile);
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
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(dataFile))) {
            oos.writeObject(userCache);
            System.out.println("Saved " + userCache.size() + " users to storage.");
        } catch (IOException e) {
            System.err.println("Error saving user data: " + e.getMessage());
        }
    }
    @Override
    public void close() throws IOException {
        // No-op
    }
}