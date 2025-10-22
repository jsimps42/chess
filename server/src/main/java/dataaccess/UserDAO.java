package dataaccess;

import model.UserData;
import java.util.HashMap;

public interface UserDAO {
    void addUser(UserData user) throws DataAccessException;
    UserData getUser(String username);
    void clear();
    HashMap<String, UserData> getUserMap();
}