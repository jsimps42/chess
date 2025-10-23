package dataaccess;

import model.AuthData;

public interface AuthAccess {
    void addAuth(AuthData authData);

    AuthData getAuth(String authToken) throws DataAccessException;

    void deleteAuth(String authToken);

    void clear();
}