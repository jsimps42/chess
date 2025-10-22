package dataaccess;

import model.UserData;
import java.util.HashMap;

public class MemoryUserDAO implements UserDAO {

    private final HashMap<String, UserData> users = new HashMap<>();

    @Override
    public void addUser(UserData user) throws DataAccessException {
        if (users.containsKey(user.username())) {
            throw new DataAccessException("User already exists");
        }
        users.put(user.username(), user);
    }

    @Override
    public UserData getUser(String username) {
        return users.get(username);
    }

    @Override
    public void clear() {
        users.clear();
    }

    @Override
    public HashMap<String, UserData> getUserMap() {
        return users;
    }
}