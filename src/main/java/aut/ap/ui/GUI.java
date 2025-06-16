package aut.ap.ui;

import aut.ap.model.Email;
import aut.ap.model.User;
import aut.ap.service.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Comparator;
import java.util.List;

import static aut.ap.service.EmailService.*;
import static aut.ap.service.UserService.*;

public class GUI extends JFrame {
    private User currentUser;
    private JPanel cards;
    private CardLayout cardLayout;
    private Email currentEmail;

    public GUI() {
        setTitle("Milou Mail");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);

        // Create and add panels
        cards.add(createWelcomePanel(), "WELCOME");
        cards.add(createLoginPanel(), "LOGIN");
        cards.add(createSignupPanel(), "SIGNUP");
        cards.add(new JPanel(), "INBOX"); // Placeholder, will be created after login
        cards.add(createComposePanel(), "COMPOSE");
        cards.add(createViewEmailPanel(), "VIEW_EMAIL");

        add(cards);
    }

    public void start() {
        SwingUtilities.invokeLater(() -> {
            setVisible(true);
            showWelcomePanel();
        });
    }

    private JPanel createWelcomePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Welcome to Milou Mail", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        buttonPanel.setBorder(new EmptyBorder(50, 150, 50, 150));

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> showLoginPanel());
        loginButton.setFont(new Font("Arial", Font.PLAIN, 18));

        JButton signupButton = new JButton("Sign Up");
        signupButton.addActionListener(e -> showSignupPanel());
        signupButton.setFont(new Font("Arial", Font.PLAIN, 18));

        buttonPanel.add(loginButton);
        buttonPanel.add(signupButton);

        panel.add(buttonPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Login", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.setBorder(new EmptyBorder(50, 150, 20, 150));

        JLabel emailLabel = new JLabel("Email:");
        JTextField emailField = new JTextField();
        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField();

        formPanel.add(emailLabel);
        formPanel.add(emailField);
        formPanel.add(passwordLabel);
        formPanel.add(passwordField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton loginButton = new JButton("Login");
        JButton backButton = new JButton("Back");

        loginButton.addActionListener(e -> {
            String email = emailField.getText();
            if (!email.contains("@")) {
                email += "@milou.com";
            }
            String password = new String(passwordField.getPassword());

            try {
                currentUser = login(email, password);
                showInboxPanel();
                JOptionPane.showMessageDialog(this, "Welcome back, " + currentUser.getName() + "!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Login failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        backButton.addActionListener(e -> showWelcomePanel());

        buttonPanel.add(loginButton);
        buttonPanel.add(backButton);

        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createSignupPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Sign Up", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(new EmptyBorder(50, 150, 20, 150));

        JLabel nameLabel = new JLabel("Name:");
        JTextField nameField = new JTextField();
        JLabel emailLabel = new JLabel("Email:");
        JTextField emailField = new JTextField();
        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField();
        JLabel confirmLabel = new JLabel("Confirm Password:");
        JPasswordField confirmField = new JPasswordField();

        formPanel.add(nameLabel);
        formPanel.add(nameField);
        formPanel.add(emailLabel);
        formPanel.add(emailField);
        formPanel.add(passwordLabel);
        formPanel.add(passwordField);
        formPanel.add(confirmLabel);
        formPanel.add(confirmField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton signupButton = new JButton("Sign Up");
        JButton backButton = new JButton("Back");

        signupButton.addActionListener(e -> {
            String name = nameField.getText();
            String email = emailField.getText();
            if (!email.contains("@")) {
                email += "@milou.com";
            }
            String password = new String(passwordField.getPassword());
            String confirm = new String(confirmField.getPassword());

            if (!password.equals(confirm)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (password.length() < 8) {
                JOptionPane.showMessageDialog(this, "Password must be at least 8 characters", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                signUp(name, email, password);
                JOptionPane.showMessageDialog(this, "Account created successfully! Please login.");
                showLoginPanel();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Signup failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        backButton.addActionListener(e -> showWelcomePanel());

        buttonPanel.add(signupButton);
        buttonPanel.add(backButton);

        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createInboxPanel() {
        if (currentUser == null) {
            return new JPanel(new BorderLayout());
        }

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));


        JPanel headerPanel = new JPanel(new BorderLayout());

        JLabel welcomeLabel = new JLabel("Welcome, " + currentUser.getName());
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        headerPanel.add(welcomeLabel, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton composeButton = new JButton("Compose");
        JButton logoutButton = new JButton("Logout");

        composeButton.addActionListener(e -> showComposePanel());
        logoutButton.addActionListener(e -> {
            currentUser = null;
            showWelcomePanel();
        });

        buttonPanel.add(composeButton);
        buttonPanel.add(logoutButton);
        headerPanel.add(buttonPanel, BorderLayout.EAST);

        panel.add(headerPanel, BorderLayout.NORTH);

        // Tabbed pane for different email views
        JTabbedPane tabbedPane = new JTabbedPane();

        // All Emails Tab
        JPanel allEmailsPanel = new JPanel(new BorderLayout());
        DefaultListModel<Email> allEmailsModel = new DefaultListModel<>();
        List<Email> allEmails = allEmail(currentUser.getId());
        allEmails.sort(Comparator.comparing(Email::getDate).reversed());
        allEmails.forEach(allEmailsModel::addElement);

        JList<Email> allEmailsList = new JList<>(allEmailsModel);
        allEmailsList.setCellRenderer(new EmailListCellRenderer());
        allEmailsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Email selected = allEmailsList.getSelectedValue();
                if (selected != null) {
                    showEmailViewPanel(selected);
                }
            }
        });

        allEmailsPanel.add(new JScrollPane(allEmailsList), BorderLayout.CENTER);
        tabbedPane.addTab("All Emails", allEmailsPanel);

        // Unread Emails Tab
        JPanel unreadEmailsPanel = new JPanel(new BorderLayout());
        DefaultListModel<Email> unreadEmailsModel = new DefaultListModel<>();
        List<Email> unreadEmails = EmailService.unreadEmail(currentUser.getId());
        unreadEmails.sort(Comparator.comparing(Email::getDate).reversed());
        unreadEmails.forEach(unreadEmailsModel::addElement);

        JList<Email> unreadEmailsList = new JList<>(unreadEmailsModel);
        unreadEmailsList.setCellRenderer(new EmailListCellRenderer());
        unreadEmailsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Email selected = unreadEmailsList.getSelectedValue();
                if (selected != null) {
                    showEmailViewPanel(selected);
                }
            }
        });

        JLabel unreadCountLabel = new JLabel(unreadEmails.size() + " unread emails");
        unreadEmailsPanel.add(unreadCountLabel, BorderLayout.NORTH);
        unreadEmailsPanel.add(new JScrollPane(unreadEmailsList), BorderLayout.CENTER);
        tabbedPane.addTab("Unread Emails", unreadEmailsPanel);

        // Sent Emails Tab
        JPanel sentEmailsPanel = new JPanel(new BorderLayout());
        DefaultListModel<Email> sentEmailsModel = new DefaultListModel<>();
        List<Email> sentEmails = EmailService.sentEmail(currentUser.getId());
        sentEmails.sort(Comparator.comparing(Email::getDate).reversed());
        sentEmails.forEach(sentEmailsModel::addElement);

        JList<Email> sentEmailsList = new JList<>(sentEmailsModel);
        sentEmailsList.setCellRenderer(new EmailListCellRenderer());
        sentEmailsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Email selected = sentEmailsList.getSelectedValue();
                if (selected != null) {
                    showEmailViewPanel(selected);
                }
            }
        });

        sentEmailsPanel.add(new JScrollPane(sentEmailsList), BorderLayout.CENTER);
        tabbedPane.addTab("Sent Emails", sentEmailsPanel);

        panel.add(tabbedPane, BorderLayout.CENTER);

        return panel;
    }

    private void showInboxPanel() {
        for (Component comp : cards.getComponents()) {
            if (comp.getName() != null && comp.getName().equals("INBOX")) {
                cards.remove(comp);
                break;
            }
        }

        JPanel inboxPanel = createInboxPanel();
        inboxPanel.setName("INBOX");
        cards.add(inboxPanel, "INBOX");

        cardLayout.show(cards, "INBOX");
    }


    private JPanel createComposePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Header with buttons
        JPanel headerPanel = new JPanel(new BorderLayout());
        JButton backButton = new JButton("Back to Inbox");
        backButton.addActionListener(e -> showInboxPanel());
        headerPanel.add(backButton, BorderLayout.WEST);

        JLabel titleLabel = new JLabel("Compose Email", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        panel.add(headerPanel, BorderLayout.NORTH);

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Recipients
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("To (comma separated):"), gbc);

        gbc.gridy = 1;
        JTextField recipientsField = new JTextField(30);
        formPanel.add(recipientsField, gbc);

        // Subject
        gbc.gridy = 2;
        formPanel.add(new JLabel("Subject:"), gbc);

        gbc.gridy = 3;
        JTextField subjectField = new JTextField(30);
        formPanel.add(subjectField, gbc);

        // Body
        gbc.gridy = 4;
        formPanel.add(new JLabel("Message:"), gbc);

        gbc.gridy = 5;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        JTextArea bodyArea = new JTextArea(10, 30);
        JScrollPane bodyScroll = new JScrollPane(bodyArea);
        formPanel.add(bodyScroll, gbc);

        // Buttons
        gbc.gridy = 6;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton sendButton = new JButton("Send");
        JButton cancelButton = new JButton("Cancel");

        sendButton.addActionListener(e -> {
            String recipients = recipientsField.getText();
            String subject = subjectField.getText();
            String body = bodyArea.getText();

            try {
                String code = EmailService.sendEmail(currentUser, subject, body, recipients);
                JOptionPane.showMessageDialog(this, "Email sent successfully!\nCode: " + code);
                showInboxPanel();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to send email: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> showInboxPanel());

        buttonPanel.add(cancelButton);
        buttonPanel.add(sendButton);
        formPanel.add(buttonPanel, gbc);

        panel.add(formPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createViewEmailPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Header with buttons
        JPanel headerPanel = new JPanel(new BorderLayout());
        JButton backButton = new JButton("Back to Inbox");
        backButton.addActionListener(e -> showInboxPanel());
        headerPanel.add(backButton, BorderLayout.WEST);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton replyButton = new JButton("Reply");
        JButton forwardButton = new JButton("Forward");

        replyButton.addActionListener(e -> {
            if (currentEmail != null) {
                replyToEmail(currentEmail);
            }
        });

        forwardButton.addActionListener(e -> {
            if (currentEmail != null) {
                forwardEmail(currentEmail);
            }
        });

        actionPanel.add(replyButton);
        actionPanel.add(forwardButton);
        headerPanel.add(actionPanel, BorderLayout.EAST);

        panel.add(headerPanel, BorderLayout.NORTH);

        // Email content
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel infoPanel = new JPanel(new GridLayout(0, 1, 5, 5));

        JLabel fromLabel = new JLabel();
        JLabel toLabel = new JLabel();
        JLabel subjectLabel = new JLabel();
        JLabel dateLabel = new JLabel();

        infoPanel.add(fromLabel);
        infoPanel.add(toLabel);
        infoPanel.add(subjectLabel);
        infoPanel.add(dateLabel);

        contentPanel.add(infoPanel, BorderLayout.NORTH);

        JTextArea bodyArea = new JTextArea();
        bodyArea.setEditable(false);
        bodyArea.setLineWrap(true);
        bodyArea.setWrapStyleWord(true);
        contentPanel.add(new JScrollPane(bodyArea), BorderLayout.CENTER);

        panel.add(contentPanel, BorderLayout.CENTER);

        // Store references to update later
        panel.putClientProperty("fromLabel", fromLabel);
        panel.putClientProperty("toLabel", toLabel);
        panel.putClientProperty("subjectLabel", subjectLabel);
        panel.putClientProperty("dateLabel", dateLabel);
        panel.putClientProperty("bodyArea", bodyArea);

        return panel;
    }


    private void showWelcomePanel() {
        cardLayout.show(cards, "WELCOME");
    }

    private void showLoginPanel() {
        cardLayout.show(cards, "LOGIN");
    }

    private void showSignupPanel() {
        cardLayout.show(cards, "SIGNUP");
    }


    private void showComposePanel() {
        cardLayout.show(cards, "COMPOSE");
    }

    private void showEmailViewPanel(Email email) {
        JPanel viewPanel = (JPanel) cards.getComponent(5); // VIEW_EMAIL panel

        JLabel fromLabel = (JLabel) viewPanel.getClientProperty("fromLabel");
        JLabel toLabel = (JLabel) viewPanel.getClientProperty("toLabel");
        JLabel subjectLabel = (JLabel) viewPanel.getClientProperty("subjectLabel");
        JLabel dateLabel = (JLabel) viewPanel.getClientProperty("dateLabel");
        JTextArea bodyArea = (JTextArea) viewPanel.getClientProperty("bodyArea");

        fromLabel.setText("From: " + email.getSender().getEmail());

        List<String> recipients = EmailService.findRecipients(email);
        toLabel.setText("To: " + String.join(", ", recipients));

        subjectLabel.setText("Subject: " + email.getSubject());
        dateLabel.setText("Date: " + email.getDate());
        bodyArea.setText(email.getBody());


        // Store the current email for reply/forward
        viewPanel.putClientProperty("currentEmail", email);

        cardLayout.show(cards, "VIEW_EMAIL");
    }

    private void replyToEmail(Email original) {
        currentEmail = original;
        JPanel composePanel = (JPanel) cards.getComponent(4); // COMPOSE panel

        // Find components in the compose panel
        JTextField recipientsField = findComponentInPanel(composePanel, JTextField.class, 0);
        JTextField subjectField = findComponentInPanel(composePanel, JTextField.class, 1);
        JTextArea bodyArea = findComponentInPanel(composePanel, JTextArea.class, 0);

        if (recipientsField != null && subjectField != null && bodyArea != null) {
            // Set recipient to original sender
            recipientsField.setText(original.getSender().getEmail());

            // Set subject with "Re: " prefix
            subjectField.setText(original.getSubject().startsWith("Re: ") ?
                    original.getSubject() : "Re: " + original.getSubject());

            // Quote original message
            String originalBody = "\n\n---------- Original Message ----------\n" +
                    "From: " + original.getSender().getEmail() + "\n" +
                    "Date: " + original.getDate() + "\n" +
                    "Subject: " + original.getSubject() + "\n\n" +
                    original.getBody();

            bodyArea.setText(originalBody);

            // Show compose panel and position cursor at top
            cardLayout.show(cards, "COMPOSE");
            bodyArea.requestFocus();
            bodyArea.setCaretPosition(0);
        }
    }

    private void forwardEmail(Email original) {
        currentEmail = original;
        JPanel composePanel = (JPanel) cards.getComponent(4); // COMPOSE panel

        // Find components in the compose panel
        JTextField recipientsField = findComponentInPanel(composePanel, JTextField.class, 0);
        JTextField subjectField = findComponentInPanel(composePanel, JTextField.class, 1);
        JTextArea bodyArea = findComponentInPanel(composePanel, JTextArea.class, 0);

        if (recipientsField != null && subjectField != null && bodyArea != null) {
            // Clear recipients for user to fill in
            recipientsField.setText("");

            // Set subject with "Fwd: " prefix
            subjectField.setText(original.getSubject().startsWith("Fwd: ") ?
                    original.getSubject() : "Fwd: " + original.getSubject());

            // Quote original message with full headers
            List<String> originalRecipients = EmailService.findRecipients(original);
            String originalBody = "\n\n---------- Forwarded Message ----------\n" +
                    "From: " + original.getSender().getEmail() + "\n" +
                    "Date: " + original.getDate() + "\n" +
                    "Subject: " + original.getSubject() + "\n" +
                    "To: " + String.join(", ", originalRecipients) + "\n\n" +
                    original.getBody();

            bodyArea.setText(originalBody);

            // Show compose panel and focus on recipients field
            cardLayout.show(cards, "COMPOSE");
            recipientsField.requestFocus();
        }
    }


    @SuppressWarnings("unchecked")
    private <T extends Component> T findComponentInPanel(Container container, Class<T> type, int index) {
        int count = 0;
        for (Component comp : container.getComponents()) {
            if (type.isInstance(comp)) {
                if (count == index) {
                    return (T) comp;
                }
                count++;
            } else if (comp instanceof Container) {
                T found = findComponentInPanel((Container) comp, type, index - count);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static class EmailListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Email) {
                Email email = (Email) value;
                setText("<html><b>" + email.getSender().getEmail() + "</b> - " +
                        email.getSubject() + " (" + email.getCode() + ")<br>" +
                        "<small>" + email.getDate() + "</small></html>");
            }
            return this;
        }
    }
}