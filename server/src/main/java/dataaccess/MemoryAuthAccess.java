package dataaccess;

import model.AuthData;

import java.util.HashSet;

public class MemoryAuthAccess implements AuthAccess {
    private HashSet<AuthData> db;

    public MemoryAuthAccess() {
        db = HashSet.newHashSet(16);
    }

    @Override
    public void addAuth(AuthData authData) throws Exception {
    public void addAuth(AuthData authData) throws Exception {
        db.add(authData);
    }

    @Override
    public void deleteAuth(String authToken) {
        for (AuthData authData : db) {
            if (authData.authToken().equals(authToken)) {
                db.remove(authData);
                break;
            }
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        for (AuthData authData : db) {
            if (authData.authToken().equals(authToken)) {
                return authData;
            }
        }
        throw new DataAccessException("Auth token does not exist: " + authToken);
    }

    @Override
    public void clear() throws Exception {
        db = new HashSet<>(16);
    }
}