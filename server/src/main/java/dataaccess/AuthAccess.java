package dataaccess;

import model.AuthData;

public interface AuthAccess {
    void addAuth(AuthData authData) throws Exception;
    AuthData getAuth(String authToken) throws Exception;
    void deleteAuth(String authToken) throws Exception;
    void clear() throws Exception;
}