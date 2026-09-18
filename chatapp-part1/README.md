# ChatApp — Part 1: Registration and Login

**Student Number:** <ST10539183>
**PROG5121/p/w**

A console-only Java application implementing the registration and login
feature described for Part 1 of the PoE brief. Built with Maven, tested
with JUnit 5.

## What's here

```
chatapp-part1/
├── pom.xml
├── README.md
├── .gitignore
└── src
    ├── main/java/com/chatapp/
    │   ├── Login.java     # Registration/login logic (the class the brief asks for)
    │   └── Main.java      # Console menu (register / login / exit)
    └── test/java/com/chatapp/
        └── LoginTest.java # JUnit 5 tests using the brief's test data
```

## Requirements

- Java 17+ (JDK, not just JRE)
- Maven 3.8+
- A regular internet connection (Maven needs to download JUnit from Maven
  Central the first time you run it)

## Running the app

```bash
mvn compile exec:java
```

You'll get a simple menu:

```
1. Register a new account
2. Login
3. Exit
```

Register first (with a valid username, password and cell number), then
log in with the same details to see the welcome message.

##  test run

```bash
mvn test
```

This runs `LoginTest`, which checks every scenario in the brief's test
data table (username, password and cell-phone-number validation, plus
registration and login outcomes).

## the  runnable jar

```bash
mvn package
java -jar target/chatapp-part1.jar
```

## Design decisions / assumptions

The brief has a couple of ambiguous points, so here's how they were
resolved (also documented as comments in `Login.java`):

- **Username rule** — "contains an underscore and is no more than five
  characters long" is treated literally: the underscore itself counts
  towards the five-character limit (e.g. `kyl_1` is valid, `kyle_1` is not).
- **Cell phone number rule** — interpreted as: the number must start with
  the South African international code `+27`, followed by digits only,
  with no more than ten digits after the code. This matches the worked
  example in the brief (`+27838968976`).
- `registerUser()` validates username → password → cell number in that
  order and returns the first relevant failure message, or a success
  message once all three pass.





