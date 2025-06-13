package aut.ap.model;

import aut.ap.framwork.MilouEntity;
import jakarta.persistence.*;

@Entity
@Table (name = "email_recipient")
public class EmailRecipient extends MilouEntity {
    public enum Status {
        unread,
        read
    }
    @ManyToOne(optional = false)
    @JoinColumn(name = "email_id")
    private Email email;
    @ManyToOne(optional = false)
    @JoinColumn (name = "recipient_id")
    private User recipient;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.unread;

    public EmailRecipient() {
    }

    public EmailRecipient(Email email, User recipientId) {
        this.email = email;
        this.recipient = recipientId;
    }

    public Email getEmail() {
        return email;
    }

    public void setEmail(Email email) {
        this.email = email;
    }

    public User getRecipient() {
        return recipient;
    }

    public void setRecipient(User recipient) {
        this.recipient = recipient;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
    @Override
    public String toString() {
        return "EmailRecipient{" +
                "email=" + email +
                ", recipient=" + recipient +
                ", status=" + status +
                ", id=" + getId() +
                '}';
    }

}
