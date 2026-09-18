package com.chatapp;

import java.util.Scanner;

/**
 * Main.java
 *
 * Console entry point for Part 1 of the ChatApp PoE: registration and login.
 * No GUI / JOptionPane is used, as required by the brief.
 */
public class Main {

    private static final Scanner SCANNER = new Scanner(System.in);
    private static Login login;

    public static void main(String[] args) {
        System.out.println("=================================");
        System.out.println("      Welcome to ChatApp");
        System.out.println("=================================");

        boolean exit = false;
        while (!exit) {
            printMenu();
            String choice = SCANNER.nextLine().trim();

            switch (choice) {
                case "1":
                    registerMenu();
                    break;
                case "2":
                    loginMenu();
                    break;
                case "3":
                    exit = true;
                    System.out.println("Goodbye!");
                    break;
                default:
                    System.out.println("Invalid option, please choose 1, 2 or 3.\n");
            }
        }

        SCANNER.close();
    }

    private static void printMenu() {
        System.out.println("\nPlease choose an option:");
        System.out.println("1. Register a new account");
        System.out.println("2. Login");
        System.out.println("3. Exit");
        System.out.print("> ");
    }

    private static void registerMenu() {
        System.out.println("\n--- Register a new account ---");

        System.out.print("Enter your first name: ");
        String firstName = SCANNER.nextLine().trim();

        System.out.print("Enter your last name: ");
        String lastName = SCANNER.nextLine().trim();

        System.out.print("Enter a username (must contain '_' and be no more than 5 characters): ");
        String username = SCANNER.nextLine().trim();

        System.out.print("Enter a password (min 8 chars, 1 capital letter, 1 number, 1 special character): ");
        String password = SCANNER.nextLine().trim();

        System.out.print("Enter your cell phone number (e.g. +27838968976): ");
        String cellPhoneNumber = SCANNER.nextLine().trim();

        login = new Login(firstName, lastName, username, password, cellPhoneNumber);
        String result = login.registerUser();
        System.out.println("\n" + result);
    }

    private static void loginMenu() {
        System.out.println("\n--- Login ---");

        if (login == null || !login.isRegistered()) {
            System.out.println("No registered user found. Please register an account first.");
            return;
        }

        System.out.print("Enter your username: ");
        String username = SCANNER.nextLine().trim();

        System.out.print("Enter your password: ");
        String password = SCANNER.nextLine().trim();

        login.loginUser(username, password);
        System.out.println("\n" + login.returnLoginStatus());
    }
}
