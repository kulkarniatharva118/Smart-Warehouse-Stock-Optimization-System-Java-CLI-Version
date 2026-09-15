package com.warehouse.ui;

import java.util.Scanner;

/**
 * Utility for robust input validation preventing CLI application crashes from malformed user input.
 * Demonstrates CSE2006 Unit 1: Methods, Control Flow & Unit 3: Robust Exception Catching.
 */
public class InputValidator {

    public static int readInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println(" Invalid integer input. Please enter a valid number.");
            }
        }
    }

    public static int readPositiveInt(Scanner scanner, String prompt) {
        while (true) {
            int val = readInt(scanner, prompt);
            if (val > 0) {
                return val;
            }
            System.out.println(" Value must be greater than zero.");
        }
    }

    public static int readNonNegativeInt(Scanner scanner, String prompt) {
        while (true) {
            int val = readInt(scanner, prompt);
            if (val >= 0) {
                return val;
            }
            System.out.println(" Value cannot be negative.");
        }
    }

    public static double readDouble(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.println(" Invalid decimal number. Please enter a valid double value.");
            }
        }
    }

    public static double readNonNegativeDouble(Scanner scanner, String prompt) {
        while (true) {
            double val = readDouble(scanner, prompt);
            if (val >= 0.0) {
                return val;
            }
            System.out.println(" Value cannot be negative.");
        }
    }

    public static String readNonEmptyString(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println(" Input cannot be empty. Please enter text.");
        }
    }

    public static String readOptionalString(Scanner scanner, String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }
}
