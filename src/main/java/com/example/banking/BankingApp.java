package com.example.banking;

import com.example.banking.ui.BankingUI;

/**
 * Main class for the banking application.
 */
public class BankingApp {
    public static void main(String[] args) {
        UserManager userManager = new UserManager();
        BankingUI ui = new BankingUI(userManager);
        ui.start();
    }
}