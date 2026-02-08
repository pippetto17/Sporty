package model.converter;

import model.bean.UserBean;
import model.domain.User;

/**
 * Converter utility for transforming between User domain entities and UserBean
 * data transfer objects.
 * Implements the BCE (Boundary-Control-Entity) pattern for proper layer
 * separation.
 */
public class UserConverter {
    private UserConverter() {
    }

    /**
     * Converts a UserBean to a User domain entity.
     *
     * @param userBean the user bean to convert
     * @return the User entity, or null if userBean is null
     */
    public static User toEntity(UserBean userBean) {
        if (userBean == null) {
            return null;
        }
        User user = new User();
        user.setId(userBean.getId());
        user.setUsername(userBean.getUsername());
        user.setPassword(userBean.getPassword());
        user.setName(userBean.getName());
        user.setSurname(userBean.getSurname());
        return user;
    }

    /**
     * Converts a User domain entity to a UserBean.
     *
     * @param user the user entity to convert
     * @return the UserBean, or null if user is null
     */
    public static UserBean toBean(User user) {
        if (user == null) {
            return null;
        }
        UserBean userBean = new UserBean();
        userBean.setId(user.getId());
        userBean.setUsername(user.getUsername());
        userBean.setPassword(user.getPassword());
        userBean.setName(user.getName());
        userBean.setSurname(user.getSurname());
        if (user.getRole() != null) {
            userBean.setRole(user.getRole().getCode());
        }
        return userBean;
    }
}