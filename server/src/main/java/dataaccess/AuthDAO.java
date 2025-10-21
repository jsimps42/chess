package dataaccess;

import model.AuthData;
import java.util.HashMap;

public interface AuthDAO {
    void addAuth(AuthData auth);
    AuthData getAuth(String authToken);
    void deleteAuth(String authToken);
    void clear();
    public HashMap<String, AuthData> listAuthTokens();
}