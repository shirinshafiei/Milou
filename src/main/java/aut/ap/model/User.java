package aut.ap.model;

import aut.ap.framwork.MilouEntity;
import jakarta.persistence.Basic;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table (name = "users")
public class User extends MilouEntity {
    @Basic (optional = false)
    private String name;

    @Basic (optional = false)
    private String email;

    @Basic (optional = false)
    private String password;

    public User() {
    }

    public User (String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "User{" +
                "name ='" + name + '\'' +
                "email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", id='" + getId() + '\'' +
                '}';
    }

}
