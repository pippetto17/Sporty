package controller;

import exception.ValidationException;
import model.bean.UserBean;
import model.converter.UserConverter;
import model.dao.DAOFactory;
import model.dao.UserDAO;
import model.domain.Role;
import model.domain.User;
import model.utils.Constants;

/**
 * Controller responsible for authentication and user registration operations.
 * Handles login validation, new user registration, and role management.
 */
public class LoginController {
    private final UserDAO userDAO;

    /**
     * Constructs a new LoginController with the specified DAO factory.
     *
     * @param daoFactory the factory to create DAO instances
     */
    public LoginController(DAOFactory daoFactory) {
        this.userDAO = daoFactory.getUserDAO();
    }

    /**
     * Authenticates a user with the provided credentials.
     * Validates username and password, then checks against the database.
     *
     * @param userBean the user credentials containing username and password
     * @return a UserBean with complete user information if authentication succeeds,
     *         null otherwise
     * @throws ValidationException if username or password is empty
     */
    public UserBean login(UserBean userBean) throws ValidationException {
        validateNotEmpty(userBean.getUsername(), Constants.ERROR_USERNAME_EMPTY);
        validateNotEmpty(userBean.getPassword(), Constants.ERROR_PASSWORD_EMPTY);
        User user = userDAO.authenticate(userBean.getUsername(), userBean.getPassword());
        if (user == null) {
            return null;
        }
        return UserConverter.toBean(user);
    }

    /**
     * Registers a new user in the system.
     * Validates all input fields and ensures username uniqueness.
     *
     * @param userBean the user credentials containing username and password
     * @param name     the user's first name
     * @param surname  the user's last name
     * @param role     the role code (player, organizer, or field manager)
     * @throws ValidationException if validation fails or username already exists
     */
    public void register(UserBean userBean, String name, String surname, int role) throws ValidationException {
        validateNotEmpty(userBean.getUsername(), Constants.ERROR_USERNAME_EMPTY);
        validateNotEmpty(userBean.getPassword(), Constants.ERROR_PASSWORD_EMPTY);
        validateNotEmpty(name, Constants.ERROR_NAME_EMPTY);
        validateNotEmpty(surname, Constants.ERROR_SURNAME_EMPTY);
        var existingUser = userDAO.findByUsername(userBean.getUsername());
        if (existingUser != null) {
            throw new ValidationException(Constants.ERROR_USERNAME_EXISTS);
        }
        var newUser = new User(0, userBean.getUsername(), userBean.getPassword(), name, surname,
                Role.fromCode(role));
        userDAO.save(newUser);
    }

    /**
     * Converts a role string identifier to its numeric code.
     *
     * @param roleString the role string (e.g., "Player", "Organizer", "Field
     *                   Manager")
     * @return the numeric role code
     * @throws ValidationException if the role string is null or unknown
     */
    public int getRoleCodeFromString(String roleString) throws ValidationException {
        if (roleString == null) {
            throw new ValidationException("Role cannot be null");
        }
        return switch (roleString) {
            case Constants.ROLE_PLAYER -> Role.PLAYER.getCode();
            case Constants.ROLE_ORGANIZER -> Role.ORGANIZER.getCode();
            case Constants.ROLE_FIELD_MANAGER -> Role.FIELD_MANAGER.getCode();
            default -> throw new ValidationException("Unknown role: " + roleString);
        };
    }

    /**
     * Validates that a string value is not null or empty.
     *
     * @param value        the value to validate
     * @param errorMessage the error message to throw if validation fails
     * @throws ValidationException if the value is null or empty
     */
    private void validateNotEmpty(String value, String errorMessage) throws ValidationException {
        if (value == null || value.isEmpty()) {
            throw new ValidationException(errorMessage);
        }
    }

    /**
     * Validates all registration input fields and returns the first error found.
     *
     * @param username the username to validate
     * @param password the password to validate
     * @param name     the first name to validate
     * @param surname  the last name to validate
     * @param role     the role to validate
     * @return null if all validations pass, otherwise an error message
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
        return null;
    }
}