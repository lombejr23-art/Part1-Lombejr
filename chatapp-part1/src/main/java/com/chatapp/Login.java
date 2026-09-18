package com.chatapp;

import java.util.regex.Pattern;

/**
 * Login.java
 *
 * Handles user registration and login for the ChatApp console application.
 *
 * Design notes / assumptions made while implementing the PoE brief:
 * - The username rule ("contains an underscore and is no more than five
 *   characters long") is interpreted literally: the underscore counts
 *   towards the five-character limit.
 * - The cell phone number rule is interpreted as: the number must start
 *   with the South African international dialling code "+27", followed
 *   by digits only, with the digit portion after "+27" being no more
 *   than ten characters long. This matches the worked example in the
 *   brief ("+27838968976" is accepted).
 * - registerUser() validates the username, password and cell phone number
 *   (in that order) and returns the first relevant failure message, or a
 *   success message once every check passes.
 *
 * Reference used to build the regular-expression based cell phone check:
 * Oracle Java Documentation, "Pattern (Java SE 17 & JDK 17)".
 * Available at: https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/regex/Pattern.html
 */
public class Login {

    // Registration fields
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private String cellPhoneNumber;

    // Tracks whether registerUser() has completed successfully
    private boolean registered;

    // Tracks whether the last loginUser() call succeeded
    private boolean loggedIn;

    // Regex: "+27" followed by 1 to 10 digits (international code + number)
    private static final Pattern SA_CELLPHONE_PATTERN = Pattern.compile("^\\+27[0-9]{1,10}$");

    // Regex: at least one uppercase letter, one digit and one special character
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*[0-9].*");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile(".*[^a-zA-Z0-9].*");

    public Login() {
    }

    public Login(String firstName, String lastName, String username, String password, String cellPhoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.cellPhoneNumber = cellPhoneNumber;
    }

    // ---------------------------------------------------------------
    // Validation methods
    // ---------------------------------------------------------------

    /**
     * Checks that the given username contains an underscore and is no
     * more than five characters long.
     */
    public boolean checkUserName(String username) {
        if (username == null) {
            return false;
        }
        return username.contains("_") && username.length() <= 5;
    }

    /** Overload that checks the username currently stored on this instance. */
    public boolean checkUserName() {
        return checkUserName(this.username);
    }

    /**
     * Checks that the given password is at least eight characters long
     * and contains a capital letter, a number and a special character.
     */
    public boolean checkPasswordComplexity(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        return UPPERCASE_PATTERN.matcher(password).matches()
                && DIGIT_PATTERN.matcher(password).matches()
                && SPECIAL_CHAR_PATTERN.matcher(password).matches();
    }

    /** Overload that checks the password currently stored on this instance. */
    public boolean checkPasswordComplexity() {
        return checkPasswordComplexity(this.password);
    }

    /**
     * Checks that the given cell phone number contains the South African
     * international dialling code (+27) followed by the number, with the
     * digits after the country code being no more than ten characters long.
     */
    public boolean checkCellPhoneNumber(String cellPhoneNumber) {
        if (cellPhoneNumber == null) {
            return false;
        }
        return SA_CELLPHONE_PATTERN.matcher(cellPhoneNumber).matches();
    }

    /** Overload that checks the cell phone number currently stored on this instance. */
    public boolean checkCellPhoneNumber() {
        return checkCellPhoneNumber(this.cellPhoneNumber);
    }

    // ---------------------------------------------------------------
    // Registration
    // ---------------------------------------------------------------

    /**
     * Validates the username, password and cell phone number currently
     * stored on this instance, and returns the appropriate message.
     */
    public String registerUser() {
        if (!checkUserName()) {
            registered = false;
            return "Username is not correctly formatted; please ensure that your username contains an "
                    + "underscore and is no more than five characters in length.";
        }

        if (!checkPasswordComplexity()) {
            registered = false;
            return "Password is not correctly formatted; please ensure that the password contains at least "
                    + "eight characters, a capital letter, a number, and a special character.";
        }

        if (!checkCellPhoneNumber()) {
            registered = false;
            return "Cell phone number incorrectly formatted or does not contain international code; "
                    + "please correct the number and try again.";
        }

        registered = true;
        return "Username successfully captured.\nPassword successfully captured.\n"
                + "Cell phone number successfully added.\nUser registered successfully.";
    }

    // ---------------------------------------------------------------
    // Login
    // ---------------------------------------------------------------

    /**
     * Verifies that the entered username and password match the details
     * stored when the user registered.
     */
    public boolean loginUser(String enteredUsername, String enteredPassword) {
        loggedIn = registered
                && this.username != null && this.username.equals(enteredUsername)
                && this.password != null && this.password.equals(enteredPassword);
        return loggedIn;
    }

    /**
     * Returns the appropriate welcome / failure message based on the
     * outcome of the most recent loginUser() call.
     */
    public String returnLoginStatus() {
        if (loggedIn) {
            return "Welcome " + firstName + ", " + lastName + " it is great to see you again.";
        }
        return "Username or password incorrect, please try again.";
    }

    // ---------------------------------------------------------------
    // Getters / setters
    // ---------------------------------------------------------------

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCellPhoneNumber() {
        return cellPhoneNumber;
    }

    public void setCellPhoneNumber(String cellPhoneNumber) {
        this.cellPhoneNumber = cellPhoneNumber;
    }

    public boolean isRegistered() {
        return registered;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }
}
