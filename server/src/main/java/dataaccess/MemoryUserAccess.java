package dataaccess;

import model.UserData;
import java.util.HashSet;

public class MemoryUserAccess implements UserAccess{
    private HashSet<UserData> db;
    public MemoryUserAccess() {
        db = new HashSet<>(16);
    }

    @Override
    public void addUser(UserData user) throws DataAccessException {
        try {
            getUser(user.username());
        }
        catch (DataAccessException e) {
            db.add(user);
            return;
        }

        throw new DataAccessException("User already exists: " + user.username());
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        for (UserData user : db) {
            if (user.username().equals(username)) {
                return user;
            }
        }
        throw new DataAccessException("User not found: " + username);
    }

    @Override
    public void authenticateUser(String username, String password) throws DataAccessException {
        for (UserData user : db) {
            if (user.username().equals(username) &&
                user.password().equals(password)) {
                return;
            }
        }
        throw new DataAccessException("Could not access: " + username);
    }

    @Override
    public void clear() {
        db = new HashSet<>(16);
    }
}