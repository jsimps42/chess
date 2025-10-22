package dataaccess;

import model.AuthData;
import java.util.HashMap;

public interface AuthDAO {
    void addAuth(AuthData auth);
    AuthData getAuth(String authToken);
    void removeAuth(String authToken);
    void clear();
    HashMap<String, AuthData> listAuthTokens();
}