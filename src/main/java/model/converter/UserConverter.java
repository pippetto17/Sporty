package model.converter;

import model.bean.UserBean;
import model.domain.User;

public class UserConverter {
    private UserConverter() {
    }

    public static User toUser(UserBean userBean) {
        if (userBean == null) {
            return null;
        }
        User user = new User();
        user.setUsername(userBean.getUsername());
        user.setPassword(userBean.getPassword());
        return user;
    }

    public static UserBean toUserBean(User user) {
        if (user == null) {
            return null;
        }
        UserBean userBean = new UserBean();
        userBean.setUsername(user.getUsername());
        userBean.setPassword(user.getPassword());
        return userBean;
    }
}