package dataaccess;

import model.UserData;

public interface UserAccess {
    UserData getUser(String username) throws DataAccessException;

    void addUser(UserData user) throws DataAccessException;

    boolean authenticateUser(String username, String password) throws DataAccessException;

    void clear() throws DataAccessException;
}