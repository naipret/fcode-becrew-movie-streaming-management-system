package com.moviestreaming;

import com.moviestreaming.config.AppConstants;

/**
 * Main Application Bootstrap Entry Point for Netflix CLI Movie Streaming Management System.
 */
public class App {

    private App() {
        // Utility class constructor
    }

    public static void main(String[] args) {
        printBanner();
        System.out.println("Status: Application initialized successfully (Day 1 Baseline).");
        System.out.println("Ready to proceed to Day 2: File Storage Engine & CSV Serialization.");
    }

    private static void printBanner() {
        System.out.println("================================================================================");
        System.out.println("  _   _ ______ _______ ______ _      _______  __    _____ _      _____ ");
        System.out.println(" | \\ | |  ____|__   __|  ____| |    |_   _\\ \\/ /   / ____| |    |_   _|");
        System.out.println(" |  \\| | |__     | |  | |__  | |      | |  \\  /   | |    | |      | |  ");
        System.out.println(" | . ` |  __|    | |  |  __| | |      | |  /  \\   | |    | |      | |  ");
        System.out.println(" | |\\  | |____   | |  | |    | |____ _| |_/ /\\ \\  | |____| |____ _| |_ ");
        System.out.println(" |_| \\_|______|  |_|  |_|    |______|_____/_/  \\_\\  \\_____|______|_____|");
        System.out.println("                                                                        ");
        System.out.println(" " + AppConstants.APP_NAME + " v" + AppConstants.APP_VERSION);
        System.out.println("================================================================================");
    }
}
