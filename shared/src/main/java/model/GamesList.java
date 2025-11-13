package model;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class GamesList extends HashSet<GameData> {
    public GamesList() {

    }
    public GamesList(HashSet<GameData> games) { 
        super(games); 
    }

    public String toString() { 
        return new Gson().toJson(this.toArray()); 
    }

    public String toJson() {
        return new Gson().toJson(Map.of("games", this));
    }
}