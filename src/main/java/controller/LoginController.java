package controller;

import exception.ValidationException;
import model.bean.UserBean;
import model.converter.UserConverter;
import model.dao.DAOFactory;
import model.dao.UserDAO;
import model.domain.Role;
import model.domain.User;
import model.utils.Constants;

public class LoginController {
    private final UserDAO userDAO;

    public LoginController(DAOFactory daoFactory) {
        this.userDAO = daoFactory.getUserDAO();
    }

    public UserBean login(UserBean userBean) throws ValidationException {
        validateNotEmpty(userBean.getUsername(), Constants.ERROR_USERNAME_EMPTY);
        validateNotEmpty(userBean.getPassword(), Constants.ERROR_PASSWORD_EMPTY);
        User user = userDAO.authenticate(userBean.getUsername(), userBean.getPassword());
        if (user == null) {
            return null;
        }
        return UserConverter.toBean(user);
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
        var newUser = new User(0, userBean.getUsername(), userBean.getPassword(), name, surname,
                Role.fromCode(role));
        userDAO.save(newUser);
    }

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

    private void validateNotEmpty(String value, String errorMessage) throws ValidationException {
        if (value == null || value.isEmpty()) {
            throw new ValidationException(errorMessage);
        }
    }

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