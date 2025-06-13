package aut.ap.service;

import aut.ap.framwork.SingletonSessionFactory;
import aut.ap.model.Email;
import aut.ap.model.EmailRecipient;
import aut.ap.model.User;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class EmailService {

    private static final String CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int CODE_LENGTH = 6;


    public static String sendEmail(User sender, String subject, String body, String recipients) {
        if (sender == null) {
            throw new RuntimeException("Sender cannot be null");
        }
        if (subject == null || subject.trim().isEmpty()) {
            throw new RuntimeException("Email subject cannot be empty");
        }
        if (subject.length() > 50) {
            throw new RuntimeException("Email subject is too long (max 50 characters)");
        }
        if (body == null || body.trim().isEmpty()) {
            throw new RuntimeException("Email body cannot be empty");
        }
        if (recipients == null || recipients.trim().isEmpty()) {
            throw new RuntimeException("Recipients list cannot be empty");
        }

        Email email = persist(sender, subject, body);
        String[] recipientArray = recipients.split(",");
        for (String recipient : recipientArray) {
            recipient = recipient.trim();
            if (!recipient.isEmpty()) {
                String finalRecipient = recipient.contains("@") ? recipient : recipient + "@milou.com";
                User recipientUser = SingletonSessionFactory.get()
                        .fromTransaction(session ->
                                session.createNativeQuery("select * from users where email = :email", User.class)
                                        .setParameter("email", finalRecipient)
                                        .getSingleResult()

                        );
                if (!(recipientUser == null)) {
                    persist(email, recipientUser);
                }
            }
        }
        return email.getCode();
    }

    public static Email readEmailByCode(String code, int userId) {
        Email email = SingletonSessionFactory.get()
                .fromTransaction(session ->
                        session.createNativeQuery("select * from email where code = :code", Email.class)
                                .setParameter("code", code)
                                .getSingleResult()
                );

        if (email == null) {
            throw new IllegalArgumentException("Email not found for code: " + code);
        }

        boolean found = !SingletonSessionFactory.get()
                .fromTransaction(session ->
                        session.createNativeQuery("select 1 from email_recipient where email_id = :emailId and recipient_id = :userId")
                                .setParameter("emailId", email.getId())
                                .setParameter("userId", userId)
                                .getResultList()
                ).isEmpty();

        if (email.getSender().getId() == userId || found) {
            SingletonSessionFactory.get().inTransaction(session -> {
                session.createNativeQuery("update email_recipient set status = 'read' where email_id = :emailId and recipient_id = :userId")
                        .setParameter("emailId", email.getId())
                        .setParameter("userId", userId)
                        .executeUpdate();
            });
            return email;
        }

        throw new RuntimeException("You cannot read this email.");

    }

    public static List<Email> allEmail(int userId) {
        return SingletonSessionFactory.get()
                .fromTransaction(session ->
                        session.createNativeQuery(
                                        "select e.* from email e " +
                                                "left join email_recipient er on er.email_id = e.id " +
                                                "where er.recipient_id = :userId", Email.class
                                )
                                .setParameter("userId", userId)
                                .getResultList()
                );
    }

    public static List<Email> unreadEmail(int userId) {
        return SingletonSessionFactory.get()
                .fromTransaction(session ->
                        session.createNativeQuery(
                                        "select e.* from email e " +
                                                "left join email_recipient er on er.email_id = e.id " +
                                                "where er.recipient_id = :userId and er.status = 'unread'", Email.class
                                )
                                .setParameter("userId", userId)
                                .getResultList()
                );
    }

    public static List<Email> sentEmail(int userId){
        return SingletonSessionFactory.get()
                .fromTransaction(session ->
                        session.createNativeQuery(
                                        "select e.* from email e " +
                                                "where e.sender_id = :userId", Email.class
                                )
                                .setParameter("userId", userId)
                                .getResultList()
                );
    }

    public static String replyEmail(User sender, String code, String body) {
        Email email = SingletonSessionFactory.get()
                .fromTransaction(session ->
                        session.createNativeQuery("select * from email where code = :code", Email.class)
                                .setParameter("code", code)
                                .getSingleResult()
                );

        if (email == null) {
            throw new IllegalArgumentException("Email not found for code: " + code);
        }

        String newSubject = "[Re] " + email.getSubject();

        List<EmailRecipient> recipients = SingletonSessionFactory.get()
                .fromTransaction(session ->
                        session.createNativeQuery("select * from email_recipient where email_id = :emailId", EmailRecipient.class)
                                .setParameter("emailId", email.getId())
                                .getResultList()
                );

        if (recipients.isEmpty()) {
            throw new IllegalArgumentException("No recipients found for email: " + email.getId());
        }

        boolean isRecipient = false;
        for (EmailRecipient recipient : recipients) {
            if (recipient.getRecipient().getId() == sender.getId()) {
                isRecipient = true;
                break;
            }
        }

        if (isRecipient) {
            User newRecipient = email.getSender();

            Email newEmail = persist(sender, newSubject, body);

             for (EmailRecipient recipient : recipients) {
                 if (!(recipient.getRecipient().getId() == sender.getId())) {
                     persist(newEmail, recipient.getRecipient());
                 }
             }
             persist(newEmail, newRecipient);

            return newEmail.getCode();
        }

        throw new RuntimeException("You cannot reply to this email");
    }

    public static String forwardEmail(User sender, String code, String recipients) {
        Email email = SingletonSessionFactory.get()
                .fromTransaction(session ->
                        session.createNativeQuery("select * from email where code = :code", Email.class)
                                .setParameter("code", code)
                                .getSingleResult()
                );
        List<EmailRecipient> recipientsList = SingletonSessionFactory.get()
                .fromTransaction(session ->
                        session.createNativeQuery("select * from email_recipient where email_id = :emailId", EmailRecipient.class)
                                .setParameter("emailId", email.getId())
                                .getResultList()
                );

        boolean found = false;
        for (EmailRecipient recipient : recipientsList) {
            if (recipient.getRecipient().getId() == sender.getId()) {
                found = true;
            }
        }
        if (found) {
            String newSubject = "[Fw] " + email.getSubject();

            Email forwardedEmail = persist(sender, newSubject, email.getBody());

            String[] recipientArray = recipients.split(",");
            for (String recipient : recipientArray) {
                recipient = recipient.trim();
                if (!recipient.isEmpty()) {
                    String finalRecipient = recipient.contains("@") ? recipient : recipient + "@milou.com";
                    User recipientUser = SingletonSessionFactory.get()
                            .fromTransaction(session ->
                                    session.createNativeQuery("select * from users where email = :email", User.class)
                                            .setParameter("email", finalRecipient)
                                            .getSingleResult()

                            );
                    if (!(recipientUser == null)) {
                        persist(forwardedEmail, recipientUser);
                    }
                }

            }
            return forwardedEmail.getCode();
        }
        throw new RuntimeException("you can not forward email");
    }

    public static List<String> findRecipients(Email email) {
        List<EmailRecipient> recipientsList = SingletonSessionFactory.get()
                .fromTransaction(session ->
                        session.createNativeQuery("select * from email_recipient where email_id = :emailId", EmailRecipient.class)
                                .setParameter("emailId", email.getId())
                                .getResultList()
                );
        List<String> emailAddress = new ArrayList<>();
        for (EmailRecipient recipient : recipientsList) {
            emailAddress.add(recipient.getRecipient().getEmail());
        }

        return emailAddress;
    }


    public static Email persist(User sender, String subject, String body) {
        String code = generateEmailCode();
        Email email = new Email(sender, subject, body, code);
        SingletonSessionFactory.get()
                .inTransaction(session -> session.persist(email));
        return email;
    }

    public static EmailRecipient persist(Email email, User recipient) {
        EmailRecipient recipient1 = new EmailRecipient(email, recipient);
        SingletonSessionFactory.get()
                .inTransaction(session -> session.persist(recipient1));
        return recipient1;
    }

    public static String generateEmailCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder(CODE_LENGTH);

        for (int i = 0; i < EmailService.CODE_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            code.append(CHARACTERS.charAt(index));
        }

        return code.toString();
    }
}



