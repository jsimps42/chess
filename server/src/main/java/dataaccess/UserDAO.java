package dataaccess;

import java.util.HashMap;
import model.UserData;

public interface UserDAO {
    void addUser(UserData user) throws DataAccessException;
    UserData getUser(String username) throws DataAccessException;
    void clear();
    public HashMap<String, UserData> getUserMap();
}