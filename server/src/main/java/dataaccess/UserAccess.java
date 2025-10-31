package dataaccess;

import model.UserData;

public interface UserAccess {
    void addUser(UserData username) throws DataAccessException;

    UserData getUser(String username) throws DataAccessException;

    void authenticateUser(String username, String password) throws DataAccessException;

    void clear() throws DataAccessException;
}