package dataaccess;

import model.UserData;

public interface UserAccess {
    void addUser(UserData user) throws DataAccessException;
    UserData getUser(String username) throws DataAccessException;
    boolean authenticateUser(String username, String password) throws DataAccessException;
    void clear() throws DataAccessException;
}