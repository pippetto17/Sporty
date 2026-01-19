package controller;

import exception.ValidationException;
import model.bean.UserBean;
import model.dao.DAOFactory;
import model.dao.UserDAO;
import model.domain.User;
import model.utils.Constants;

public class LoginController {
    private final UserDAO userDAO;

    public LoginController(DAOFactory.PersistenceType persistenceType) {
        this.userDAO = DAOFactory.getUserDAO(persistenceType);
    }

    public User login(UserBean userBean) {
        validateNotEmpty(userBean.getUsername(), Constants.ERROR_USERNAME_EMPTY);
        validateNotEmpty(userBean.getPassword(), Constants.ERROR_PASSWORD_EMPTY);

        return userDAO.authenticate(userBean.getUsername(), userBean.getPassword());
    }

    public void register(UserBean userBean, String name, String surname, int role) throws ValidationException {
        validateNotEmpty(userBean.getUsername(), Constants.ERROR_USERNAME_EMPTY);
        validateNotEmpty(userBean.getPassword(), Constants.ERROR_PASSWORD_EMPTY);
        validateNotEmpty(name, Constants.ERROR_NAME_EMPTY);
        validateNotEmpty(surname, Constants.ERROR_SURNAME_EMPTY);

        var existingUser = userDAO.findByUsername(userBean.getUsername());
        if (existingUser != null) {
            throw new ValidationException(Constants.ERROR_USERNAME_EXISTS);
        }

        var newUser = new User(userBean.getUsername(), userBean.getPassword(), name, surname, role);
        userDAO.save(newUser);
    }

    public int getRoleCodeFromString(String roleString) throws ValidationException {
        if (roleString == null) {
            throw new ValidationException("Role cannot be null");
        }

        return switch (roleString) {
            case Constants.ROLE_PLAYER -> model.domain.Role.PLAYER.getCode();
            case Constants.ROLE_ORGANIZER -> model.domain.Role.ORGANIZER.getCode();
            case Constants.ROLE_FIELD_MANAGER -> model.domain.Role.FIELD_MANAGER.getCode();
            default -> throw new ValidationException("Unknown role: " + roleString);
        };
    }

    private void validateNotEmpty(String value, String errorMessage) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    /**
     * Validates all registration input fields
     * 
     * @return null if valid, error message string if invalid
     */
    public String validateRegistrationInputs(String username, String password, String name,
            String surname, String role) {
        if (username == null || username.trim().isEmpty()) {
            return Constants.ERROR_USERNAME_EMPTY;
        }
        if (password == null || password.isEmpty()) {
            return Constants.ERROR_PASSWORD_EMPTY;
        }
        if (name == null || name.trim().isEmpty()) {
            return Constants.ERROR_NAME_EMPTY;
        }
        if (surname == null || surname.trim().isEmpty()) {
            return Constants.ERROR_SURNAME_EMPTY;
        }
        if (role == null || role.isEmpty()) {
            return Constants.ERROR_ALL_FIELDS_REQUIRED;
        }
        return null; // All valid
    }
}
