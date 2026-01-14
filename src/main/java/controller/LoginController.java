package controller;

import model.bean.UserBean;
import model.dao.DAOFactory;
import model.dao.UserDAO;
import model.domain.User;

import java.sql.SQLException;

public class LoginController {
    private final UserDAO userDAO;

    public LoginController(DAOFactory.PersistenceType persistenceType) {
        try {
            this.userDAO = DAOFactory.getUserDAO(persistenceType);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize DAO: " + e.getMessage(), e);
        }
    }

    public User login(UserBean userBean) {
        String username = userBean.getUsername();
        String password = userBean.getPassword();

        // Validazione input
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        // Autenticazione - usa UserDAO.authenticate()
        return userDAO.authenticate(username, password);
    }

    public void register(UserBean userBean, String name, String surname, int role) {
        String username = userBean.getUsername();
        String password = userBean.getPassword();

        // Validazione
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (surname == null || surname.isEmpty()) {
            throw new IllegalArgumentException("Surname cannot be empty");
        }

        // Verifica se l'utente esiste già - usa UserDAO per operazioni CRUD
        User existingUser = userDAO.findByUsername(username);
        if (existingUser != null) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Creazione nuovo utente - usa UserDAO per salvare
        User newUser = new User(username, password, name, surname, role);
        userDAO.save(newUser);
    }
}
