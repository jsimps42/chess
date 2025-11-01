package dataaccess;

import model.AuthData;
import java.util.HashSet;

public class MemoryAuthAccess implements AuthAccess {
    private final HashSet<AuthData> db = new HashSet<>(16);

    @Override
    public void addAuth(AuthData auth) throws DataAccessException {
        db.add(auth);
    }

    @Override
    public AuthData getAuth(String token) {
        for (AuthData a : db) {
            if (a.authToken().equals(token)) {
                return a;
            }
        }
        return null;
    }

    @Override
    public void deleteAuth(String token) throws DataAccessException {
        db.removeIf(a -> a.authToken().equals(token));
    }

    @Override
    public void clear() {
        db.clear();
    }
}