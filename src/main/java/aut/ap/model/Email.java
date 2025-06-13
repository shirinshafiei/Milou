package aut.ap.model;

import aut.ap.framwork.MilouEntity;
import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table (name = "email")
public class Email extends MilouEntity {
     @Basic(optional = false)
     @Column(name = "date", insertable = false, updatable = false)
     @Temporal(TemporalType.TIMESTAMP)
     private Date date;
     @JoinColumn(name = "sender_id")
     @ManyToOne(optional = false)
     private User sender;
     @Basic(optional = false)
     private String subject;
     @Basic(optional = false)
     private String body;
     @Basic(optional = false)
     private String code;

     public Email() {
     }

     public Email(User sender, String subject, String body, String code) {
          this.sender = sender;
          this.subject  = subject;
          this.body = body;
          this.code = code;
     }


     public Date getDate() {
          return date;
     }

     public void setDate(Date date) {
          this.date = date;
     }


     public String getSubject() {
          return subject;
     }

     public void setSubject(String subject) {
          this.subject = subject;
     }

     public String getBody() {
          return body;
     }

     public void setBody(String body) {
          this.body = body;
     }

     public String getCode() {
          return code;
     }

     public void setCode(String code) {
          this.code = code;
     }

     public User getSender() {
          return sender;
     }

     public void setSender(User sender) {
          this.sender = sender;
     }

     @Override
     public String toString() {
          return "Email{" +
                  "date=" + date +
                  ", sender=" + sender +
                  ", subject='" + subject + '\'' +
                  ", body='" + body + '\'' +
                  ", code='" + code + '\'' +
                  ", id='" + getId() + '\'' +
                  '}';
     }

}
