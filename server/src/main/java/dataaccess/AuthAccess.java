package dataaccess;

import model.AuthData;
//delete me
public interface AuthAccess {
    void addAuth(AuthData authData) throws DataAccessException;

    AuthData getAuth(String authToken) throws DataAccessException;

    void deleteAuth(String authToken) throws DataAccessException;

    void clear() throws DataAccessException;
}