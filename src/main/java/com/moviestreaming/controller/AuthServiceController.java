package com.moviestreaming.controller;

import com.moviestreaming.exception.AppException;
import com.moviestreaming.model.User;
import com.moviestreaming.service.AuthService;
import com.moviestreaming.util.InputHelper;
import com.moviestreaming.view.BannerView;
import java.util.Scanner;

/**
 * Controller managing User Authentication workflows (Login, Register, Logout).
 */
public class AuthServiceController {

    private final AuthService authService;

    public AuthServiceController(AuthService authService) {
        if (authService == null) {
            throw new IllegalArgumentException("AuthService must not be null");
        }
        this.authService = authService;
    }

    /**
     * Handles the interactive user login prompt.
     *
     * @param scanner console scanner
     * @return true if login succeeded
     */
    public boolean handleLogin(Scanner scanner) {
        BannerView.printSectionHeader("User Login");
        String username = InputHelper.readNonEmptyString(scanner, "Enter username: ");
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        try {
            User user = authService.login(username, password);
            BannerView.printSuccess("Welcome back, " + user.getFullName() + "!");
            return true;
        } catch (AppException e) {
            BannerView.printError(e.getMessage());
            return false;
        }
    }

    /**
     * Handles the interactive user registration prompt.
     *
     * @param scanner console scanner
     * @return true if registration succeeded
     */
    public boolean handleRegister(Scanner scanner) {
        BannerView.printSectionHeader("New Account Registration");
        String username = InputHelper.readNonEmptyString(scanner, "Choose username (3-30 chars): ");
        System.out.print("Choose password (min 6 chars): ");
        String password = scanner.nextLine();
        String fullName = InputHelper.readNonEmptyString(scanner, "Enter your full name: ");
        String email = InputHelper.readNonEmptyString(scanner, "Enter email address: ");

        try {
            User user = authService.register(username, password, fullName, email);
            BannerView.printSuccess("Account created successfully! Your ID is: " + user.getId());
            return true;
        } catch (AppException e) {
            BannerView.printError(e.getMessage());
            return false;
        }
    }

    /**
     * Handles user logout.
     */
    public void handleLogout() {
        authService.logout();
        BannerView.printInfo("You have been logged out successfully.");
    }
}
