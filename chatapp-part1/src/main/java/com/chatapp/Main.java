package com.chatapp;

import java.util.List;
import java.util.Scanner;

/**
 * Main.java
 *
 * Console entry point for the ChatApp PoE.
 * Part 1: registration and login.
 * Part 2: after a successful login the user enters QuickChat and can send,
 * store or disregard messages.
 * Part 3: stored messages are loaded from storedMessages.json at start-up and
 * a fourth QuickChat menu option lets the user work with them.
 * No GUI / JOptionPane is used.
 */
public class Main {

    private static final Scanner SCANNER = new Scanner(System.in);
    private static Login login;
    private static boolean exitApp = false;

    public static void main(String[] args) {
        // Part 3: read the JSON file into the stored messages array
        Message.loadStoredMessages();

        System.out.println("=================================");
        System.out.println("      Welcome to ChatApp");
        System.out.println("=================================");

        while (!exitApp) {
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
                    exitApp = true;
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

        // Messages can only be sent after a successful login
        if (login.isLoggedIn()) {
            quickChat();
        }
    }

    // ---------------------------------------------------------------
    // QuickChat
    // ---------------------------------------------------------------

    private static void quickChat() {
        System.out.println("\nWelcome to QuickChat.");

        int messagesAllowed = readPositiveInt("How many messages do you wish to enter? ");
        int messagesEntered = 0;

        while (!exitApp) {
            System.out.println("\nPlease choose an option:");
            System.out.println("1) Send Messages");
            System.out.println("2) Show recently sent messages");
            System.out.println("3) Quit");
            System.out.println("4) Stored Messages");
            System.out.print("> ");
            String choice = SCANNER.nextLine().trim();

            switch (choice) {
                case "1":
                    if (messagesEntered >= messagesAllowed) {
                        System.out.println("You have already entered all " + messagesAllowed + " messages.");
                        break;
                    }
                    while (messagesEntered < messagesAllowed) {
                        System.out.println("\n--- Message " + (messagesEntered + 1) + " of " + messagesAllowed + " ---");
                        composeMessage();
                        messagesEntered++;
                    }
                    System.out.println("\nTotal number of messages sent: " + Message.returnTotalMessages());
                    break;
                case "2":
                    System.out.println(Message.printMessages());
                    break;
                case "3":
                    System.out.println("\nSession summary:");
                    System.out.println(Message.printMessages());
                    System.out.println("\nTotal number of messages sent: " + Message.returnTotalMessages());
                    System.out.println("Goodbye!");
                    exitApp = true;
                    break;
                case "4":
                    storedMessagesMenu();
                    break;
                default:
                    System.out.println("Invalid option, please choose 1, 2, 3 or 4.");
            }
        }
    }

    /** Collects one message from the user and lets them send, disregard or store it. */
    private static void composeMessage() {
        // Message number is zero-based and follows the number of messages sent so far
        int messageNumber = Message.returnTotalMessages();

        // Recipient
        String recipient;
        while (true) {
            System.out.print("Enter the recipient's cell number (e.g. +27718693002): ");
            recipient = SCANNER.nextLine().trim();
            Message check = new Message(messageNumber, recipient, "check");
            System.out.println(check.checkRecipientCell());
            if (check.isRecipientValid()) {
                break;
            }
        }

        // Message text (250 characters maximum)
        String text;
        while (true) {
            System.out.print("Enter your message: ");
            text = SCANNER.nextLine().trim();
            if (text.isEmpty()) {
                System.out.println("The message cannot be empty.");
            } else if (text.length() > Message.MAX_MESSAGE_LENGTH) {
                System.out.println("Please enter a message of less than 250 characters.");
            } else {
                System.out.println("Message sent");
                break;
            }
        }

        Message message = new Message(messageNumber, recipient, text);
        System.out.println(message.getMessageIDStatus());

        // Send / disregard / store
        boolean done = false;
        while (!done) {
            System.out.println("\nWhat would you like to do with this message?");
            System.out.println("1) Send Message");
            System.out.println("2) Disregard Message");
            System.out.println("3) Store Message to send later");
            System.out.print("> ");
            String choice = SCANNER.nextLine().trim();

            switch (choice) {
                case "1":
                    System.out.println(message.sentMessage(1));
                    System.out.println("\n" + message.getDetails());
                    done = true;
                    break;
                case "2":
                    System.out.println(message.sentMessage(2));
                    System.out.print("> ");
                    if (SCANNER.nextLine().trim().equals("0")) {
                        message.disregardMessage();
                        System.out.println("Message deleted.");
                        done = true;
                    }
                    break;
                case "3":
                    System.out.println(message.sentMessage(3));
                    done = true;
                    break;
                default:
                    System.out.println("Invalid option, please choose 1, 2 or 3.");
            }
        }
    }

    // ---------------------------------------------------------------
    // Part 3: Stored Messages menu
    // ---------------------------------------------------------------

    private static void storedMessagesMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Stored Messages ---");
            System.out.println("1) Display sender and recipient of all stored messages");
            System.out.println("2) Display the longest stored message");
            System.out.println("3) Search for a message ID");
            System.out.println("4) Search for all messages for a recipient");
            System.out.println("5) Delete a message using its hash");
            System.out.println("6) Display report of all stored messages");
            System.out.println("0) Back");
            System.out.print("> ");
            String choice = SCANNER.nextLine().trim();

            switch (choice) {
                case "1":
                    System.out.println("\n" + Message.displaySendersAndRecipients(senderName()));
                    break;
                case "2":
                    System.out.println("\nLongest stored message: " + Message.longestStoredMessage());
                    break;
                case "3":
                    System.out.print("Enter the message ID: ");
                    System.out.println("\n" + Message.searchByMessageID(SCANNER.nextLine().trim()));
                    break;
                case "4":
                    System.out.print("Enter the recipient's cell number: ");
                    String recipient = SCANNER.nextLine().trim();
                    List<String> found = Message.getMessagesForRecipient(recipient);
                    if (found.isEmpty()) {
                        System.out.println("\nNo messages found for " + recipient + ".");
                    } else {
                        System.out.println("\nMessages for " + recipient + ":");
                        for (int i = 0; i < found.size(); i++) {
                            System.out.println((i + 1) + ". " + found.get(i));
                        }
                    }
                    break;
                case "5":
                    System.out.print("Enter the message hash: ");
                    System.out.println("\n" + Message.deleteByHash(SCANNER.nextLine().trim()));
                    break;
                case "6":
                    System.out.println("\n--- Stored Messages Report ---");
                    System.out.println(Message.storedMessagesReport());
                    break;
                case "0":
                    back = true;
                    break;
                default:
                    System.out.println("Invalid option, please choose 0 to 6.");
            }
        }
    }

    /** The logged-in user is the sender of every message. */
    private static String senderName() {
        return login.getFirstName() + " " + login.getLastName();
    }

    private static int readPositiveInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                int value = Integer.parseInt(SCANNER.nextLine().trim());
                if (value > 0) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
                // fall through to the error message
            }
            System.out.println("Please enter a whole number greater than 0.");
        }
    }
}
