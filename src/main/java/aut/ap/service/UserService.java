package aut.ap.service;

import aut.ap.framwork.SingletonSessionFactory;
import aut.ap.model.User;

public class UserService {
    public static void signUp(String name, String email, String password) {
        boolean emailExists = SingletonSessionFactory.get()
                .fromTransaction(session ->
                        session.createQuery(
                                        "select count(u) > 0 from users p where u.email = :email",
                                        Boolean.class
                                )
                                .setParameter("email", email)
                                .getSingleResult()
                );
        if (emailExists) {
            throw new IllegalArgumentException("An account with this email already exists");
        }
        User newPerson = new User(name, email, password);
        SingletonSessionFactory.get()
                .inTransaction(session -> session.persist(newPerson));
    }

    public static User login(String email, String password) {
        User user = SingletonSessionFactory.get()
                .fromTransaction(session ->
                        session.createNativeQuery(
                                        "select * from users where email = :email", User.class)
                                .setParameter("email", email)
                                .getSingleResult()
                );
        if (user == null) {
            throw new RuntimeException("email not found");
        } else if (!user.getPassword().equals(password)) {
            throw new RuntimeException("password is incorrect");
        } else {
            return user;
        }

    }
}
