package model.converter;

import model.bean.UserBean;
import model.domain.User;

public class UserConverter {
    private UserConverter() {
    }

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