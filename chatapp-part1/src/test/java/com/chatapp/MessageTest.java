package com.chatapp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MessageTest {

    // Test data from the brief
    private static final String RECIPIENT_1 = "+27718693002";
    private static final String TEXT_1 = "Hi Mike, can you join us for dinner tonight?";
    private static final String RECIPIENT_2 = "08575975889";
    private static final String TEXT_2 = "Hi Keegan, did you receive the payment?";

    @BeforeEach
    void setUp() {
        Message.resetMessages();
    }

    // --- Message length ---

    @Test
    void messageLengthSuccess() {
        Message message = new Message("0012345678", 0, RECIPIENT_1, TEXT_1);
        assertEquals("Message ready to send.", message.checkMessageLength());
    }

    @Test
    void messageLengthFailure() {
        String longText = "a".repeat(255);
        Message message = new Message("0012345678", 0, RECIPIENT_1, longText);
        assertEquals("Message exceeds 250 characters by 5; please reduce the size.",
                message.checkMessageLength());
    }

    // --- Recipient number ---

    @Test
    void recipientNumberSuccess() {
        Message message = new Message("0012345678", 0, RECIPIENT_1, TEXT_1);
        assertEquals("Cell phone number successfully captured.", message.checkRecipientCell());
    }

    @Test
    void recipientNumberFailure() {
        Message message = new Message("0112345678", 1, RECIPIENT_2, TEXT_2);
        assertEquals("Cell phone number is incorrectly formatted or does not contain an international code. "
                + "Please correct the number and try again.", message.checkRecipientCell());
    }

    // --- Message hash ---

    @Test
    void messageHashTestCase1() {
        Message message = new Message("0012345678", 0, RECIPIENT_1, TEXT_1);
        assertEquals("00:0:HITONIGHT", message.createMessageHash());
    }

    @Test
    void messageHashesInLoop() {
        String[] ids = {"0012345678", "0198765432", "9923456789", "4500000001"};
        String[] texts = {TEXT_1, TEXT_2, "Thanks", "See you at 5 pm."};
        String[] expected = {"00:0:HITONIGHT", "01:1:HIPAYMENT", "99:2:THANKSTHANKS", "45:3:SEEPM"};

        for (int i = 0; i < ids.length; i++) {
            Message message = new Message(ids[i], i, RECIPIENT_1, texts[i]);
            assertEquals(expected[i], message.createMessageHash(), "Hash for message " + i);
        }
    }

    // --- Message ID ---

    @Test
    void messageIdIsCreated() {
        Message message = new Message(0, RECIPIENT_1, TEXT_1);
        assertTrue(message.checkMessageID());
        assertEquals(10, message.getMessageID().length());
        assertTrue(message.getMessageID().matches("\\d{10}"));
        assertEquals("Message ID generated: " + message.getMessageID(), message.getMessageIDStatus());
    }

    @Test
    void messageIdLongerThanTenCharactersFails() {
        Message message = new Message("12345678901", 0, RECIPIENT_1, TEXT_1);
        assertFalse(message.checkMessageID());
    }

    // --- Send / disregard / store ---

    @Test
    void sentMessageOptions() {
        Message message = new Message("0012345678", 0, RECIPIENT_1, TEXT_1);
        assertEquals("Message successfully sent.", message.sentMessage(1));
        assertEquals("Press 0 to delete the message.", message.sentMessage(2));
    }

    @Test
    void storeMessageWritesJson(@TempDir Path tempDir) throws IOException {
        Message message = new Message("0012345678", 0, RECIPIENT_1, TEXT_1);
        Path file = tempDir.resolve("storedMessages.json");

        assertTrue(message.storeMessage(file.toString()));

        String json = Files.readString(file);
        assertTrue(json.contains("\"messageID\": \"0012345678\""));
        assertTrue(json.contains("\"messageHash\": \"00:0:HITONIGHT\""));
        assertTrue(json.contains("\"recipient\": \"+27718693002\""));
    }

    // --- Totals (Test Data: 2 messages; first sent, second discarded) ---

    @Test
    void totalMessagesSent() {
        Message first = new Message("0012345678", 0, RECIPIENT_1, TEXT_1);
        Message second = new Message("0198765432", 1, RECIPIENT_2, TEXT_2);

        first.sentMessage(1);   // Select Send
        second.sentMessage(2);  // Select Discard

        assertEquals(1, Message.returnTotalMessages());
        assertTrue(Message.printMessages().contains(TEXT_1));
        assertFalse(Message.printMessages().contains(TEXT_2));
    }
}
