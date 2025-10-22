package dataaccess;

import model.AuthData;
import java.util.HashMap;

public class MemoryAuthDAO implements AuthDAO {

    private final HashMap<String, AuthData> authTokens = new HashMap<>();

    @Override
    public void addAuth(AuthData auth) {
        authTokens.put(auth.authToken(), auth);
    }

    @Override
    public AuthData getAuth(String authToken) {
        return authTokens.get(authToken);
    }

    @Override
    public void removeAuth(String authToken) {
        authTokens.remove(authToken);
    }

    @Override
    public void clear() {
        authTokens.clear();
    }

    @Override
    public HashMap<String, AuthData> listAuthTokens() {
        return authTokens;
    }
}
