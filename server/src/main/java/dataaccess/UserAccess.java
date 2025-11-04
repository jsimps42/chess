package dataaccess;

import model.UserData;

public interface UserAccess {
    void addUser(UserData username) throws Exception;
    UserData getUser(String username) throws Exception;
    boolean authenticateUser(String username, String password) throws Exception;
    void clear() throws Exception;
}