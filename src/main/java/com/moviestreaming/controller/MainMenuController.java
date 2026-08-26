package com.moviestreaming.controller;

import com.moviestreaming.service.UserSession;
import com.moviestreaming.util.InputHelper;
import com.moviestreaming.view.BannerView;
import java.util.Scanner;

/**
 * Top-level application controller managing navigation and routing between Guest, Viewer, and Admin views.
 */
public class MainMenuController {

    private final AuthServiceController authController;
    private final UserController userController;
    private final AdminController adminController;
    private final UserSession userSession;

    public MainMenuController(AuthServiceController authController, UserController userController,
                              AdminController adminController, UserSession userSession) {
        if (authController == null || userController == null || adminController == null || userSession == null) {
            throw new IllegalArgumentException("Controllers and session must not be null");
        }
        this.authController = authController;
        this.userController = userController;
        this.adminController = adminController;
        this.userSession = userSession;
    }

    /**
     * Starts and maintains the primary terminal interactive lifecycle.
     *
     * @param scanner console scanner
     */
    public void start(Scanner scanner) {
        BannerView.printAppBanner();
        boolean running = true;

        while (running) {
            if (!userSession.isLoggedIn()) {
                running = handleGuestMenu(scanner);
            } else if (userSession.isAdmin()) {
                running = handleAdminMenu(scanner);
            } else {
                running = handleUserMenu(scanner);
            }
        }

        System.out.println("\nThank you for using Netflix CLI. Have a great day! 👋\n");
    }

    private boolean handleGuestMenu(Scanner scanner) {
        BannerView.printSectionHeader("Main Menu (Guest)");
        System.out.println("1. Login");
        System.out.println("2. Register New Account");
        System.out.println("0. Exit System");

        int choice = InputHelper.readInt(scanner, "Select an option [0-2]: ", 0, 2);
        switch (choice) {
            case 1:
                authController.handleLogin(scanner);
                break;
            case 2:
                authController.handleRegister(scanner);
                break;
            case 0:
                return false;
            default:
                break;
        }
        return true;
    }

    private boolean handleUserMenu(Scanner scanner) {
        BannerView.printSectionHeader("Main Menu");
        BannerView.printUserBadge(userSession.getCurrentUser().orElse(null));
        System.out.println("1. Open Viewer Dashboard");
        System.out.println("2. Logout");
        System.out.println("0. Exit System");

        int choice = InputHelper.readInt(scanner, "Select an option [0-2]: ", 0, 2);
        switch (choice) {
            case 1:
                userController.runUserMenu(scanner);
                break;
            case 2:
                authController.handleLogout();
                break;
            case 0:
                return false;
            default:
                break;
        }
        return true;
    }

    private boolean handleAdminMenu(Scanner scanner) {
        BannerView.printSectionHeader("Main Menu (Administrator)");
        BannerView.printUserBadge(userSession.getCurrentUser().orElse(null));
        System.out.println("1. Open Administrator Dashboard");
        System.out.println("2. Open Viewer Dashboard (Preview as User)");
        System.out.println("3. Logout");
        System.out.println("0. Exit System");

        int choice = InputHelper.readInt(scanner, "Select an option [0-3]: ", 0, 3);
        switch (choice) {
            case 1:
                adminController.runAdminMenu(scanner);
                break;
            case 2:
                userController.runUserMenu(scanner);
                break;
            case 3:
                authController.handleLogout();
                break;
            case 0:
                return false;
            default:
                break;
        }
        return true;
    }
}
