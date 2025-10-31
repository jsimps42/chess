package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;

public class GameSerializer {
    private static final Gson gson = new Gson();

    public static String serialize(ChessGame game) {
        return gson.toJson(game);
    }

    public static ChessGame deserialize(String json) {
        return gson.fromJson(json, ChessGame.class);
    }
}