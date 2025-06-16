package aut.ap.ui;

import aut.ap.model.Email;
import aut.ap.model.User;
import org.hibernate.service.NullServiceException;

import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

import static aut.ap.model.EmailRecipient.Status.unread;
import static aut.ap.service.EmailService.*;
import static aut.ap.service.UserService.login;
import static aut.ap.service.UserService.signUp;

public class CLI {
    private Scanner scanner = new Scanner(System.in);

    public void start() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("command ([L]ogin, [S]ignup): ");
            String command = scanner.nextLine();
            switch (command) {
                case "L" :
                case "Login" :
                    loginCommand();
                    break;
                case "S" :
                case "Singup" :
                    signUpCommand();
                    break;
                default:
                    System.err.println("invalid command.");
            }
        }
    }

    private void loginCommand() {
        System.out.print("Email: ");
        String emailInput = scanner.nextLine();

        String email = emailInput.contains("@") ? emailInput : emailInput + "@milou.com";

        System.out.print("Password: ");
        String password = scanner.nextLine();

        try {
            User user = login(email, password);
            String username = email.substring(0, email.indexOf("@"));
            System.out.println("Welcome back, " + capitalize(username) + "!");

            unreadEmailCommand(user);
            while (true) {
                System.out.println("[S]end, [V]iew, [R]eply, [F]orward, Exit: ");
                String command = scanner.nextLine();
                    switch (command) {
                        case "S":
                        case "Send":
                            sendCommand(user);
                            break;
                        case "V":
                        case "View":
                            viewCommand(user);
                            break;
                        case "R":
                        case "Reply":
                            replyCommand(user);
                            break;
                        case "F":
                        case "Forward":
                            forwardCommand(user);
                        case "Exit":
                            return;
                        default:
                            System.err.println("invalid command.");
                    }
            }
        } catch (RuntimeException e) {
            System.err.println("Login failed: " + e.getMessage());
        }
    }

    private void signUpCommand() {
        System.out.println("Name: ");
        String name = scanner.nextLine();
        System.out.print("Email: ");
        String emailInput = scanner.nextLine();
        String email = emailInput.contains("@") ? emailInput : emailInput + "@milou.com";
        System.out.print("Password: ");
        String password = scanner.nextLine();
        while (password.length() < 8) {
            System.out.println("Weak password");
            password = scanner.nextLine();
        }
        try {
            signUp(name, email, password);
            System.out.println("Your new account is created.");
            System.out.println("Go ahead and login!");

        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
        }
    }

    private void sendCommand(User sender) {
        System.out.println("Recipient(s): ");
        String recipients = scanner.nextLine();
        System.out.println("Subject: ");
        String subject = scanner.nextLine();
        System.out.println("Body: ");
        String body = scanner.nextLine();
        try {
            String code = sendEmail(sender, subject, body, recipients);
            System.out.println("Successfully sent your email.");
            System.out.println("code: " + code);
        }catch (RuntimeException e) {
            System.err.println(e.getMessage());
        }
    }

    private void viewCommand(User sender) {
        while (true) {
            System.out.println("[A]ll emails, [U]nread emails, [S]ent emails, Read by [C]ode, Exit: ");
            String command = scanner.nextLine();
            switch (command) {
                case "A":
                case "All emails":
                    allEmailsCommand(sender);
                    break;
                case "U":
                case "Unread emails":
                   unreadEmailCommand(sender);
                   break;
                case "S":
                case "Sent emails":
                    sentEmailsCommand(sender);
                    break;
                case "C":
                case "Read by Code":
                    readByCodeCommand(sender);
                    break;
                case "Exit":
                    return;
                default:
                    System.err.println("invalid command.");
            }
        }
    }

    private void allEmailsCommand(User user) {
        List<Email> allEmails = allEmail(user.getId());
        allEmails.sort(Comparator.comparing(Email::getDate).reversed());

        if (allEmails.isEmpty()) {
            System.out.println("You have no emails.");
        } else {
            System.out.println("All Emails:");
            for (Email e : allEmails) {
                System.out.println("- " + e.getSender().getEmail() + " - " + e.getSubject() + " (" + e.getCode() + ")");
            }
        }
    }

    private void unreadEmailCommand(User user) {
        List<Email> unreadEmails = unreadEmail(user.getId());
        unreadEmails.sort(Comparator.comparing(Email::getDate).reversed());
        if (unreadEmails.isEmpty()) {
            System.out.println("You have no unread emails.");
        } else {
            System.out.println("Unread Emails:");
            System.out.println(unreadEmails.size() + " unread emails:");
            for (Email e : unreadEmails) {
                System.out.println("- " + e.getSender().getEmail() + " - " + e.getSubject() + " (" + e.getCode() + ")");
            }
        }
    }

    private void sentEmailsCommand(User user) {
        List<Email> sentEmails = sentEmail(user.getId());
        sentEmails.sort(Comparator.comparing(Email::getDate).reversed());
        System.out.println("sent Email: ");

        for (Email e : sentEmails) {
            List<String> emailRecipients = findRecipients(e);
            System.out.println("- " + emailRecipients + " - " + e.getSubject() + " (" + e.getCode() + ")");
        }
    }

    private void readByCodeCommand(User user) {
        System.out.println("Code: ");
        String code = scanner.nextLine();

        try {
            Email email = readEmailByCode(code, user.getId());
            List<String> emailAddresses = findRecipients(email);

            System.out.println("Code: " + code);
            System.out.println("Recipient(s): " + String.join(", ", emailAddresses));
            System.out.println("Subject: " + email.getSubject());
            System.out.println("Date: " + email.getDate());
            System.out.println();
            System.out.println(email.getBody());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void replyCommand(User user) {
        System.out.println("Code: ");
        String code = scanner.nextLine();
        System.out.println("Body: ");
        String body = scanner.nextLine();
        try {
           String newCode =  replyEmail(user, code, body);
           System.out.println("Successfully sent your reply to email " + code);
           System.out.println("Code: " + newCode);
        }catch (RuntimeException e) {
            System.err.println(e.getMessage());
        }
    }

    private void forwardCommand(User user) {
        System.out.println("Code: ");
        String code = scanner.nextLine();
        System.out.println("Recipient(s): ");
        String recipients = scanner.nextLine();
        try {
            String newCode = forwardEmail(user, code, recipients);
            System.out.println("Successfully forwarded your email.");
            System.out.println("Code: " + newCode);

        }catch (RuntimeException e) {
            System.err.println(e.getMessage());
        }
    }


    private String capitalize(String input) {
        if (input == null || input.isEmpty()) return input;
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }
}
