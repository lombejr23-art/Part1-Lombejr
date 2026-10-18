package com.chatapp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Message.java
 *
 * Represents a single QuickChat message (Part 2 of the PoE) and keeps
 * track of every message sent while the program is running.
 *
 * Assumptions:
 * - The recipient rule reuses the Part 1 cell phone check (Login), as the
 *   brief hints: "+27" followed by no more than ten digits.
 * - The message number is zero-based, so the first message's hash is
 *   00:0:HITONIGHT-style, matching the example in the brief.
 * - Messages are stored in a JSON file (storedMessages.json) written by
 *   hand, so no external library is needed.
 */
public class Message {

    public static final int MAX_MESSAGE_LENGTH = 250;
    public static final String DEFAULT_JSON_FILE = "storedMessages.json";

    // Messages sent / stored while the program is running
    private static final List<Message> SENT_MESSAGES = new ArrayList<>();
    private static final List<Message> STORED_MESSAGES = new ArrayList<>();

    private final String messageID;
    private final int messageNumber;
    private final String recipient;
    private final String messageText;
    private final String messageHash;

    /** Creates a message with a randomly generated ten-digit ID. */
    public Message(int messageNumber, String recipient, String messageText) {
        this(generateMessageID(), messageNumber, recipient, messageText);
    }

    /** Creates a message with a known ID (used by the unit tests). */
    public Message(String messageID, int messageNumber, String recipient, String messageText) {
        this.messageID = messageID;
        this.messageNumber = messageNumber;
        this.recipient = recipient;
        this.messageText = messageText;
        this.messageHash = createMessageHash();
    }

    // ---------------------------------------------------------------
    // Validation / creation
    // ---------------------------------------------------------------

    /** Generates a random ten-digit number (leading zeros allowed). */
    private static String generateMessageID() {
        long number = ThreadLocalRandom.current().nextLong(10_000_000_000L);
        return String.format("%010d", number);
    }

    /** Ensures the message ID is not more than ten characters. */
    public boolean checkMessageID() {
        return messageID != null && !messageID.isEmpty() && messageID.length() <= 10;
    }

    /** Confirms the ID was created, e.g. "Message ID generated: 0012345678". */
    public String getMessageIDStatus() {
        return "Message ID generated: " + messageID;
    }

    /** Checks the message is not longer than 250 characters. */
    public String checkMessageLength() {
        int length = messageText == null ? 0 : messageText.length();
        if (length <= MAX_MESSAGE_LENGTH) {
            return "Message ready to send.";
        }
        return "Message exceeds 250 characters by " + (length - MAX_MESSAGE_LENGTH)
                + "; please reduce the size.";
    }

    public boolean isMessageLengthValid() {
        return messageText != null && messageText.length() <= MAX_MESSAGE_LENGTH;
    }

    /**
     * Ensures the recipient number has an international code and is no more
     * than ten digits after it. Reuses the Part 1 cell phone check.
     */
    public String checkRecipientCell() {
        if (new Login().checkCellPhoneNumber(recipient)) {
            return "Cell phone number successfully captured.";
        }
        return "Cell phone number is incorrectly formatted or does not contain an international code. "
                + "Please correct the number and try again.";
    }

    public boolean isRecipientValid() {
        return new Login().checkCellPhoneNumber(recipient);
    }

    /**
     * Builds the hash: first two digits of the ID, a colon, the message
     * number, a colon, then the first and last words in capitals,
     * e.g. 00:0:HITONIGHT.
     */
    public String createMessageHash() {
        String idPart = messageID.length() >= 2 ? messageID.substring(0, 2) : messageID;
        String[] words = messageText.trim().split("\\s+");
        String first = cleanWord(words[0]);
        String last = cleanWord(words[words.length - 1]);
        return (idPart + ":" + messageNumber + ":" + first + last).toUpperCase();
    }

    /** Removes punctuation from the start and end of a word. */
    private static String cleanWord(String word) {
        return word.replaceAll("^[^\\p{L}\\p{N}]+|[^\\p{L}\\p{N}]+$", "");
    }

    // ---------------------------------------------------------------
    // Send / store / disregard
    // ---------------------------------------------------------------

    /**
     * Handles the user's choice: 1 = send, 2 = disregard, 3 = store.
     */
    public String sentMessage(int choice) {
        switch (choice) {
            case 1:
                SENT_MESSAGES.add(this);
                return "Message successfully sent.";
            case 2:
                return "Press 0 to delete the message.";
            case 3:
                storeMessage();
                return "Message successfully stored.";
            default:
                return "Invalid option.";
        }
    }

    /** Stores this message in storedMessages.json. */
    public boolean storeMessage() {
        return storeMessage(DEFAULT_JSON_FILE);
    }

    /**
     * Adds this message to the stored list and rewrites the JSON file
     * (a JSON array of message objects) at the given path.
     */
    public boolean storeMessage(String filePath) {
        STORED_MESSAGES.add(this);
        StringBuilder json = new StringBuilder("[\n");
        for (int i = 0; i < STORED_MESSAGES.size(); i++) {
            json.append(STORED_MESSAGES.get(i).toJson());
            json.append(i < STORED_MESSAGES.size() - 1 ? ",\n" : "\n");
        }
        json.append("]\n");
        try {
            Path path = Paths.get(filePath);
            Files.write(path, json.toString().getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private String toJson() {
        return "  {\n"
                + "    \"messageID\": \"" + escape(messageID) + "\",\n"
                + "    \"messageNumber\": " + messageNumber + ",\n"
                + "    \"messageHash\": \"" + escape(messageHash) + "\",\n"
                + "    \"recipient\": \"" + escape(recipient) + "\",\n"
                + "    \"message\": \"" + escape(messageText) + "\"\n"
                + "  }";
    }

    private static String escape(String value) {
        StringBuilder sb = new StringBuilder();
        for (char c : value.toCharArray()) {
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    // ---------------------------------------------------------------
    // Reporting
    // ---------------------------------------------------------------

    /** Full details in the order required: ID, hash, recipient, message. */
    public String getDetails() {
        return "Message ID: " + messageID + "\n"
                + "Message Hash: " + messageHash + "\n"
                + "Recipient: " + recipient + "\n"
                + "Message: " + messageText;
    }

    /** Returns every message sent while the program is running. */
    public static String printMessages() {
        if (SENT_MESSAGES.isEmpty()) {
            return "No messages have been sent.";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < SENT_MESSAGES.size(); i++) {
            sb.append(SENT_MESSAGES.get(i).getDetails());
            if (i < SENT_MESSAGES.size() - 1) {
                sb.append("\n\n");
            }
        }
        return sb.toString();
    }

    /** Returns the total number of messages sent. */
    public static int returnTotalMessages() {
        return SENT_MESSAGES.size();
    }

    /** Clears the sent and stored lists (used by the unit tests). */
    public static void resetMessages() {
        SENT_MESSAGES.clear();
        STORED_MESSAGES.clear();
    }

    // ---------------------------------------------------------------
    // Getters
    // ---------------------------------------------------------------

    public String getMessageID() {
        return messageID;
    }

    public int getMessageNumber() {
        return messageNumber;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getMessageText() {
        return messageText;
    }

    public String getMessageHash() {
        return messageHash;
    }
}
