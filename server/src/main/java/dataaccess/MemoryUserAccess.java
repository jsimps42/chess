package dataaccess;

import model.UserData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.mindrot.jbcrypt.BCrypt;

public class MemoryUserAccess implements UserAccess {
    private final Map<String, UserData> users = new HashMap<>();

    @Override
    public void addUser(UserData user) throws DataAccessException {
        if (users.containsKey(user.username())) {
            throw new DataAccessException("User already exists");
        }
        users.put(user.username(), user);
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        return users.get(username);
    }

    @Override
    public void authenticateUser(String username, String password) throws DataAccessException {
        UserData user = users.get(username);
        if (user == null || !BCrypt.checkpw(password, user.password())) {
            throw new DataAccessException("bad credentials"); // ← Same as MySQL
        }
    }

    @Override
    public void clear() throws DataAccessException {
        users.clear();
    }
}