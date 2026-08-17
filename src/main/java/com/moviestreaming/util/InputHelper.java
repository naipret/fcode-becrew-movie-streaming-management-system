package com.moviestreaming.util;

import com.moviestreaming.config.AppConstants;
import java.util.Scanner;

/**
 * Utility helper for robust, crash-proof console input handling.
 * Traps parsing exceptions, enforces boundaries, and prompts user until valid.
 */
public final class InputHelper {

    private InputHelper() {
        // Utility class constructor
    }

    /**
     * Reads a non-blank string from the scanner, rejecting the delimiter character.
     *
     * @param scanner the console scanner
     * @param prompt  the message prompt
     * @return the trimmed, valid string
     */
    public static String readNonEmptyString(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine();
            if (line == null || line.trim().isEmpty()) {
                System.out.println("[ERROR] Input cannot be empty. Please try again.");
                continue;
            }
            String trimmed = line.trim();
            if (trimmed.contains(AppConstants.CSV_DELIMITER)) {
                System.out.println("[ERROR] Input cannot contain the character '" + AppConstants.CSV_DELIMITER + "'.");
                continue;
            }
            return trimmed;
        }
    }

    /**
     * Reads an integer within a specified range [min, max].
     *
     * @param scanner the console scanner
     * @param prompt  the message prompt
     * @param min     the minimum inclusive value
     * @param max     the maximum inclusive value
     * @return the validated integer
     */
    public static int readInt(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine();
            try {
                int value = Integer.parseInt(line.trim());
                if (value < min || value > max) {
                    System.out.println(String.format("[ERROR] Value must be between %d and %d. Please try again.", min, max));
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("[ERROR] Invalid number format. Please enter a valid integer.");
            }
        }
    }

    /**
     * Reads a floating point double within a specified range [min, max].
     *
     * @param scanner the console scanner
     * @param prompt  the message prompt
     * @param min     the minimum inclusive value
     * @param max     the maximum inclusive value
     * @return the validated double
     */
    public static double readDouble(Scanner scanner, String prompt, double min, double max) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine();
            try {
                double value = Double.parseDouble(line.trim());
                if (value < min || value > max) {
                    System.out.println(String.format("[ERROR] Value must be between %.1f and %.1f. Please try again.", min, max));
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("[ERROR] Invalid number format. Please enter a valid decimal number.");
            }
        }
    }
}
