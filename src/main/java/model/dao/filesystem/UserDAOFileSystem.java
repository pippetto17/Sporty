package model.dao.filesystem;

import model.dao.UserDAO;
import model.domain.User;
import java.util.logging.Logger;

public class UserDAOFileSystem implements UserDAO {
    private static final Logger logger = Logger.getLogger(UserDAOFileSystem.class.getName());

    @Override
    public User authenticate(String username, String password) {
        logger.warning(
                "FileSystem persistence for User not fully implemented. Authenticating strictly for testing if needed.");
        return null;
    }

    @Override
    public User findById(int id) {
        return null;
    }

    @Override
    public User findByUsername(String username) {
        return null;
    }

    @Override
    public void save(User user) {
        logger.warning("Save User to FileSystem not implemented.");
    }
}