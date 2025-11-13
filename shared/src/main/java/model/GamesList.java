package model;

import com.google.gson.Gson;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

public class GamesList extends HashSet<GameData> {
    public GamesList() {
        super();
    }

    public GamesList(Collection<GameData> games) { 
        super(games); 
    }

    public Collection<GameData> games() { 
        return this; 
    }

    public String toString() { 
        return new Gson().toJson(this.toArray()); 
    }

    public String toJson() {
        return new Gson().toJson(Map.of("games", this));
    }

    public record GamesListResponse(Collection<GameData> games) {
    }
}