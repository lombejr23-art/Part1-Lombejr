package com.chatapp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Part 3 unit tests, using the five test messages from the brief.
 *
 * Messages 1 and 4 are Sent, 2 and 5 are Stored, 3 is Disregarded.
 * Message 4's ID is 0838884567 (the number given in the "Search for
 * messageID" test), and the text uses "time!" with no space before the "!".
 */
class StoredMessagesTest {

    private static final String TEXT_1 = "Did you get the cake?";
    private static final String TEXT_2 = "Where are you? You are late! I have asked you to be on time.";
    private static final String TEXT_3 = "Yohoooo, I am at your gate.";
    private static final String TEXT_4 = "It is dinner time!";
    private static final String TEXT_5 = "Ok, I am leaving without you.";

    @TempDir
    Path tempDir;

    private String jsonFile;
    private Message message1;
    private Message message2;
    private Message message3;
    private Message message4;
    private Message message5;

    @BeforeEach
    void setUp() {
        Message.resetMessages();
        jsonFile = tempDir.resolve("storedMessages.json").toString();

        message1 = new Message("0000000001", 0, "+27834557896", TEXT_1);
        message2 = new Message("0000000002", 1, "+27838884567", TEXT_2);
        message3 = new Message("0000000003", 2, "+27834484567", TEXT_3);
        message4 = new Message("0838884567", 3, "0838884567", TEXT_4);
        message5 = new Message("0000000005", 4, "+27838884567", TEXT_5);

        // Populate the arrays through the same methods the app uses
        message1.sentMessage(1);          // Sent
        message2.storeMessage(jsonFile);  // Stored
        message3.disregardMessage();      // Disregard
        message4.sentMessage(1);          // Sent
        message5.storeMessage(jsonFile);  // Stored
    }

    @Test
    void sentMessagesArrayCorrectlyPopulated() {
        assertEquals(List.of("Did you get the cake?", "It is dinner time!"),
                Message.getSentMessageTexts());
    }

    @Test
    void displayLongestMessage() {
        assertEquals("Where are you? You are late! I have asked you to be on time.",
                Message.longestStoredMessage());
    }

    @Test
    void searchForMessageID() {
        assertEquals("It is dinner time!", Message.findByMessageID("0838884567").getMessageText());
    }

    @Test
    void searchMessagesForRecipient() {
        assertEquals(
                List.of("Where are you? You are late! I have asked you to be on time.",
                        "Ok, I am leaving without you."),
                Message.getMessagesForRecipient("+27838884567"));
    }

    @Test
    void deleteMessageUsingHash() throws IOException {
        String result = Message.deleteByHash(message2.getMessageHash(), jsonFile);

        assertEquals("Message: \"Where are you? You are late! I have asked you to be on time.\" "
                + "successfully deleted.", result);
        assertEquals(List.of(TEXT_5), Message.getStoredMessageTexts());
        assertFalse(Files.readString(Path.of(jsonFile)).contains("Where are you?"));
    }

    @Test
    void displayReport() {
        String expected = "Message Hash: 00:0:DIDCAKE\n"
                + "Recipient: +27834557896\n"
                + "Message: Did you get the cake?\n\n"
                + "Message Hash: 08:3:ITTIME\n"
                + "Recipient: 0838884567\n"
                + "Message: It is dinner time!";
        assertEquals(expected, Message.sentMessagesReport());
    }

    // --- Extra tests: remaining arrays and reading the JSON file back in ---

    @Test
    void disregardedHashAndIdArraysPopulated() {
        assertEquals(List.of(TEXT_3), Message.getDisregardedMessageTexts());
        assertEquals(5, Message.getMessageHashes().size());
        assertEquals(5, Message.getMessageIDs().size());
    }

    @Test
    void storedMessagesLoadedFromJson() {
        Message.resetMessages();
        assertEquals(2, Message.loadStoredMessages(jsonFile));
        assertEquals(List.of(TEXT_2, TEXT_5), Message.getStoredMessageTexts());
    }
}
