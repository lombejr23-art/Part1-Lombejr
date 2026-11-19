package com.chatapp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Message.java
 *
 * Represents a single QuickChat message (Parts 2 and 3 of the PoE) and keeps
 * track of every message handled while the program is running.
 *
 * Assumptions:
 * - The recipient rule reuses the Part 1 cell phone check (Login), as the
 *   brief hints: "+27" followed by no more than ten digits.
 * - The message number is zero-based, so the first message's hash is
 *   00:0:HITONIGHT-style, matching the example in the brief.
 * - Messages are stored in a JSON file (storedMessages.json) written by
 *   hand, so no external library is needed. The same file is read back
 *   at start-up with a regular expression (see loadStoredMessages).
 * - Part 3 arrays: sent, disregarded, stored, message hashes and message IDs.
 *   Every message that is sent, stored or disregarded has its hash and ID
 *   recorded in the hash / ID arrays.
 */
public class Message {

    public static final int MAX_MESSAGE_LENGTH = 250;
    public static final String DEFAULT_JSON_FILE = "storedMessages.json";

    // Part 3 arrays (populated at run time, nothing is hard-coded)
    private static final List<Message> SENT_MESSAGES = new ArrayList<>();
    private static final List<Message> DISREGARDED_MESSAGES = new ArrayList<>();
    private static final List<Message> STORED_MESSAGES = new ArrayList<>();
    private static final List<String> MESSAGE_HASHES = new ArrayList<>();
    private static final List<String> MESSAGE_IDS = new ArrayList<>();

    /** Matches one message object in the JSON file written by toJson(). */
    private static final Pattern JSON_OBJECT = Pattern.compile(
            "\\{\\s*\"messageID\":\\s*\"((?:[^\"\\\\]|\\\\.)*)\",\\s*"
                    + "\"messageNumber\":\\s*(\\d+),\\s*"
                    + "\"messageHash\":\\s*\"((?:[^\"\\\\]|\\\\.)*)\",\\s*"
                    + "\"recipient\":\\s*\"((?:[^\"\\\\]|\\\\.)*)\",\\s*"
                    + "\"message\":\\s*\"((?:[^\"\\\\]|\\\\.)*)\"\\s*\\}");

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
     * For option 2 the message is only added to the disregarded array
     * once the user confirms with disregardMessage().
     */
    public String sentMessage(int choice) {
        switch (choice) {
            case 1:
                SENT_MESSAGES.add(this);
                recordHashAndID();
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

    /** Adds this message to the disregarded array (called after the user presses 0). */
    public void disregardMessage() {
        DISREGARDED_MESSAGES.add(this);
        recordHashAndID();
    }

    private void recordHashAndID() {
        MESSAGE_HASHES.add(messageHash);
        MESSAGE_IDS.add(messageID);
    }

    /** Stores this message in storedMessages.json. */
    public boolean storeMessage() {
        return storeMessage(DEFAULT_JSON_FILE);
    }

    /**
     * Adds this message to the stored array and rewrites the JSON file
     * (a JSON array of message objects) at the given path.
     */
    public boolean storeMessage(String filePath) {
        STORED_MESSAGES.add(this);
        recordHashAndID();
        return writeStoredJson(filePath);
    }

    /** Rewrites the JSON file from the stored array. */
    private static boolean writeStoredJson(String filePath) {
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

    /** Reverses escape() when reading the JSON file back in. */
    private static String unescape(String value) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '\\' && i + 1 < value.length()) {
                char next = value.charAt(++i);
                switch (next) {
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    case 'u':
                        if (i + 4 < value.length()) {
                            sb.append((char) Integer.parseInt(value.substring(i + 1, i + 5), 16));
                            i += 4;
                        }
                        break;
                    default: sb.append(next); // covers \" and \\
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    // ---------------------------------------------------------------
    // Part 3: reading the JSON file into the stored array
    // ---------------------------------------------------------------

    /** Loads storedMessages.json into the stored array. Returns how many were loaded. */
    public static int loadStoredMessages() {
        return loadStoredMessages(DEFAULT_JSON_FILE);
    }

    /**
     * Reads the JSON file written by storeMessage() into the stored array.
     * A missing or unreadable file simply loads nothing (returns 0).
     */
    public static int loadStoredMessages(String filePath) {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            return 0;
        }
        String json;
        try {
            json = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return 0;
        }

        STORED_MESSAGES.clear();
        Matcher matcher = JSON_OBJECT.matcher(json);
        int count = 0;
        while (matcher.find()) {
            Message message = new Message(
                    unescape(matcher.group(1)),
                    Integer.parseInt(matcher.group(2)),
                    unescape(matcher.group(4)),
                    unescape(matcher.group(5)));
            STORED_MESSAGES.add(message);
            if (!MESSAGE_HASHES.contains(message.messageHash)) {
                MESSAGE_HASHES.add(message.messageHash);
            }
            if (!MESSAGE_IDS.contains(message.messageID)) {
                MESSAGE_IDS.add(message.messageID);
            }
            count++;
        }
        return count;
    }

    // ---------------------------------------------------------------
    // Part 3: array accessors (copies, so callers cannot change the arrays)
    // ---------------------------------------------------------------

    private static List<String> texts(List<Message> messages) {
        List<String> result = new ArrayList<>();
        for (Message message : messages) {
            result.add(message.messageText);
        }
        return result;
    }

    public static List<String> getSentMessageTexts() {
        return texts(SENT_MESSAGES);
    }

    public static List<String> getDisregardedMessageTexts() {
        return texts(DISREGARDED_MESSAGES);
    }

    public static List<String> getStoredMessageTexts() {
        return texts(STORED_MESSAGES);
    }

    public static List<String> getMessageHashes() {
        return new ArrayList<>(MESSAGE_HASHES);
    }

    public static List<String> getMessageIDs() {
        return new ArrayList<>(MESSAGE_IDS);
    }

    // ---------------------------------------------------------------
    // Part 3: stored messages menu features
    // ---------------------------------------------------------------

    /** (a) Sender and recipient of every stored message. */
    public static String displaySendersAndRecipients(String sender) {
        if (STORED_MESSAGES.isEmpty()) {
            return "No stored messages.";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < STORED_MESSAGES.size(); i++) {
            sb.append("Sender: ").append(sender)
                    .append(" | Recipient: ").append(STORED_MESSAGES.get(i).recipient);
            if (i < STORED_MESSAGES.size() - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    /** (b) The longest stored message (the first one if there is a tie). */
    public static String longestStoredMessage() {
        if (STORED_MESSAGES.isEmpty()) {
            return "No stored messages.";
        }
        Message longest = STORED_MESSAGES.get(0);
        for (Message message : STORED_MESSAGES) {
            if (message.messageText.length() > longest.messageText.length()) {
                longest = message;
            }
        }
        return longest.messageText;
    }

    /** (c) Finds a sent or stored message by its ID, or returns null. */
    public static Message findByMessageID(String id) {
        for (Message message : SENT_MESSAGES) {
            if (message.messageID.equals(id)) {
                return message;
            }
        }
        for (Message message : STORED_MESSAGES) {
            if (message.messageID.equals(id)) {
                return message;
            }
        }
        return null;
    }

    /** (c) Recipient and message for the given ID, ready to display. */
    public static String searchByMessageID(String id) {
        Message message = findByMessageID(id);
        if (message == null) {
            return "No message found with ID " + id + ".";
        }
        return "Recipient: " + message.recipient + "\nMessage: " + message.messageText;
    }

    /** (d) Text of every sent or stored message for one recipient. */
    public static List<String> getMessagesForRecipient(String recipient) {
        List<String> result = new ArrayList<>();
        for (Message message : SENT_MESSAGES) {
            if (message.recipient.equals(recipient)) {
                result.add(message.messageText);
            }
        }
        for (Message message : STORED_MESSAGES) {
            if (message.recipient.equals(recipient)) {
                result.add(message.messageText);
            }
        }
        return result;
    }

    /** (e) Deletes a stored message by hash and rewrites the default JSON file. */
    public static String deleteByHash(String hash) {
        return deleteByHash(hash, DEFAULT_JSON_FILE);
    }

    /** (e) Deletes a stored message by hash and rewrites the JSON file at filePath. */
    public static String deleteByHash(String hash, String filePath) {
        for (int i = 0; i < STORED_MESSAGES.size(); i++) {
            Message message = STORED_MESSAGES.get(i);
            if (message.messageHash.equals(hash)) {
                STORED_MESSAGES.remove(i);
                MESSAGE_HASHES.remove(message.messageHash);
                MESSAGE_IDS.remove(message.messageID);
                writeStoredJson(filePath);
                return "Message: \"" + message.messageText + "\" successfully deleted.";
            }
        }
        return "No stored message found with hash " + hash + ".";
    }

    /** (f) Report of the full details of every stored message. */
    public static String storedMessagesReport() {
        return buildReport(STORED_MESSAGES, "No stored messages.");
    }

    /** Report of every sent message: hash, recipient, message. */
    public static String sentMessagesReport() {
        return buildReport(SENT_MESSAGES, "No messages have been sent.");
    }

    private static String buildReport(List<Message> messages, String emptyText) {
        if (messages.isEmpty()) {
            return emptyText;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < messages.size(); i++) {
            Message message = messages.get(i);
            sb.append("Message Hash: ").append(message.messageHash).append("\n")
                    .append("Recipient: ").append(message.recipient).append("\n")
                    .append("Message: ").append(message.messageText);
            if (i < messages.size() - 1) {
                sb.append("\n\n");
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

    /** Clears every array (used by the unit tests). */
    public static void resetMessages() {
        SENT_MESSAGES.clear();
        DISREGARDED_MESSAGES.clear();
        STORED_MESSAGES.clear();
        MESSAGE_HASHES.clear();
        MESSAGE_IDS.clear();
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
