package dataaccess;

import model.UserData;

import java.util.HashSet;

public class MemoryUserAccess implements UserAccess {
    private HashSet<UserData> db;

    public MemoryUserAccess() {
        db = HashSet.newHashSet(16);
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        for (UserData user : db) {
            if (user.username().equals(username)) {
                return user;
            }
        }
        throw new DataAccessException("User does not exist: " + username);
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
    public boolean authenticateUser(String username, String password) throws DataAccessException {
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
        } else {
            throw new DataAccessException("User not found: " + username);
        }
    }

    @Override
    public void clear() {
        db = HashSet.newHashSet(16);
    }
}