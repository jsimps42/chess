package dataaccess;

import model.UserData;
import java.util.HashSet;

public class MemoryUserAccess implements UserAccess{
    private HashSet<UserData> db;
    public MemoryUserAccess() {
        db = new HashSet<>(16);
    }

    @Override
    public void addUser(UserData user) {
        if (getUser(user.username()) == null) {
            db.add(user);
            return;
        }
    }

    @Override
    public UserData getUser(String username) {
        for (UserData user : db) {
            if (user.username().equals(username)) {
                return user;
            }
        }
        return null;
    }

    @Override
    public boolean authenticateUser(String username, String password) throws Exception {
        boolean userExists = false;
        for (UserData user : db) {
            if (user.username().equals(username)) {
                userExists = true;
            }
            if (user.username().equals(username) &&
                user.password().equals(password)) {
                return true;
            }
        }
        if (userExists) {
            return false;
        }
        else {
            throw new DataAccessException("Error: User does not exist: " + username);
        }
    }

    @Override
    public void clear() {
        db = new HashSet<>(16);
    }
}