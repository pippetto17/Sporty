package controller;

import exception.ServiceInitializationException;
import model.bean.UserBean;
import model.dao.DAOFactory;
import model.dao.UserDAO;
import model.domain.User;
import model.utils.Constants;

import java.sql.SQLException;

public class LoginController {
    private final UserDAO userDAO;

    public LoginController(DAOFactory.PersistenceType persistenceType) {
        try {
            this.userDAO = DAOFactory.getUserDAO(persistenceType);
        } catch (SQLException e) {
            throw new ServiceInitializationException(Constants.ERROR_DAO_INIT + e.getMessage(), e);
        }
    }

    public User login(UserBean userBean) {
        String username = userBean.getUsername();
        String password = userBean.getPassword();

        // Validazione input
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException(Constants.ERROR_USERNAME_EMPTY);
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException(Constants.ERROR_PASSWORD_EMPTY);
        }

        // Autenticazione - usa UserDAO.authenticate()
        return userDAO.authenticate(username, password);
    }

    public void register(UserBean userBean, String name, String surname, int role) {
        String username = userBean.getUsername();
        String password = userBean.getPassword();

        // Validazione
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException(Constants.ERROR_USERNAME_EMPTY);
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException(Constants.ERROR_PASSWORD_EMPTY);
        }
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException(Constants.ERROR_NAME_EMPTY);
        }
        if (surname == null || surname.isEmpty()) {
            throw new IllegalArgumentException(Constants.ERROR_SURNAME_EMPTY);
        }

        // Verifica se l'utente esiste già - usa UserDAO per operazioni CRUD
        User existingUser = userDAO.findByUsername(username);
        if (existingUser != null) {
            throw new IllegalArgumentException(Constants.ERROR_USERNAME_EXISTS);
        }

        // Creazione nuovo utente - usa UserDAO per salvare
        User newUser = new User(username, password, name, surname, role);
        userDAO.save(newUser);
    }

    /**
     * Convert role display string to role code
     * 
     * @param roleString Role display name (from Constants)
     * @return Role code
     */
    public static int getRoleCodeFromString(String roleString) {
        if (roleString == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        return roleString.equals(Constants.ROLE_PLAYER) ? model.domain.Role.PLAYER.getCode()
                : model.domain.Role.ORGANIZER.getCode();
    }
}
