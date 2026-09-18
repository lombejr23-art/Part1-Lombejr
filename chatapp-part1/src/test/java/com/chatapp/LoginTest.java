package com.chatapp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * LoginTest.java
 *
 * Unit tests for the Login class, using the exact test data supplied
 * in the PoE brief (Part 1, section 4).
 */
class LoginTest {

    private Login login;

    @BeforeEach
    void setUp() {
        login = new Login();
    }

    // -----------------------------------------------------------
    // checkUserName() - assertTrue / assertFalse
    // -----------------------------------------------------------

    @Test
    @DisplayName("Username correctly formatted: kyl_1")
    void testCheckUserName_CorrectlyFormatted() {
        assertTrue(login.checkUserName("kyl_1"));
    }

    @Test
    @DisplayName("Username incorrectly formatted: kyle!!!!!!")
    void testCheckUserName_IncorrectlyFormatted() {
        assertFalse(login.checkUserName("kyle!!!!!!"));
    }

    // -----------------------------------------------------------
    // checkPasswordComplexity() - assertTrue / assertFalse
    // -----------------------------------------------------------

    @Test
    @DisplayName("Password meets complexity requirements: Ch&&sec@ke99!")
    void testCheckPasswordComplexity_Valid() {
        assertTrue(login.checkPasswordComplexity("Ch&&sec@ke99!"));
    }

    @Test
    @DisplayName("Password does not meet complexity requirements: password")
    void testCheckPasswordComplexity_Invalid() {
        assertFalse(login.checkPasswordComplexity("password"));
    }

    // -----------------------------------------------------------
    // checkCellPhoneNumber() - assertTrue / assertFalse
    // -----------------------------------------------------------

    @Test
    @DisplayName("Cell phone number correctly formatted: +27838968976")
    void testCheckCellPhoneNumber_Valid() {
        assertTrue(login.checkCellPhoneNumber("+27838968976"));
    }

    @Test
    @DisplayName("Cell phone number incorrectly formatted: 08966553")
    void testCheckCellPhoneNumber_Invalid() {
        assertFalse(login.checkCellPhoneNumber("08966553"));
    }

    // -----------------------------------------------------------
    // registerUser() - assertEquals (returned messages)
    // -----------------------------------------------------------

    @Test
    @DisplayName("registerUser() rejects an incorrectly formatted username")
    void testRegisterUser_UsernameIncorrectlyFormatted() {
        login = new Login("Kyle", "Smith", "kyle!!!!!!", "Ch&&sec@ke99!", "+27838968976");
        String expected = "Username is not correctly formatted; please ensure that your username contains an "
                + "underscore and is no more than five characters in length.";
        assertEquals(expected, login.registerUser());
    }

    @Test
    @DisplayName("registerUser() rejects a password that does not meet complexity rules")
    void testRegisterUser_PasswordDoesNotMeetComplexity() {
        login = new Login("Kyle", "Smith", "kyl_1", "password", "+27838968976");
        String expected = "Password is not correctly formatted; please ensure that the password contains at least "
                + "eight characters, a capital letter, a number, and a special character.";
        assertEquals(expected, login.registerUser());
    }

    @Test
    @DisplayName("registerUser() rejects an incorrectly formatted cell phone number")
    void testRegisterUser_CellPhoneNumberIncorrectlyFormatted() {
        login = new Login("Kyle", "Smith", "kyl_1", "Ch&&sec@ke99!", "08966553");
        String expected = "Cell phone number incorrectly formatted or does not contain international code; "
                + "please correct the number and try again.";
        assertEquals(expected, login.registerUser());
    }

    @Test
    @DisplayName("registerUser() succeeds when all details are valid")
    void testRegisterUser_Success() {
        login = new Login("Kyle", "Smith", "kyl_1", "Ch&&sec@ke99!", "+27838968976");
        String expected = "Username successfully captured.\nPassword successfully captured.\n"
                + "Cell phone number successfully added.\nUser registered successfully.";
        assertEquals(expected, login.registerUser());
        assertTrue(login.isRegistered());
    }

    // -----------------------------------------------------------
    // loginUser() / returnLoginStatus() - assertTrue/False and assertEquals
    // -----------------------------------------------------------

    @Test
    @DisplayName("loginUser() returns true and welcomes the user back on success")
    void testLoginUser_Success() {
        login = new Login("Kyle", "Smith", "kyl_1", "Ch&&sec@ke99!", "+27838968976");
        login.registerUser();

        assertTrue(login.loginUser("kyl_1", "Ch&&sec@ke99!"));
        assertEquals("Welcome Kyle, Smith it is great to see you again.", login.returnLoginStatus());
    }

    @Test
    @DisplayName("loginUser() returns false and rejects an incorrect password")
    void testLoginUser_Failure() {
        login = new Login("Kyle", "Smith", "kyl_1", "Ch&&sec@ke99!", "+27838968976");
        login.registerUser();

        assertFalse(login.loginUser("kyl_1", "WrongPassword1!"));
        assertEquals("Username or password incorrect, please try again.", login.returnLoginStatus());
    }
}
