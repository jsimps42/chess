package model;

import chess.*;

public record GameData(ChessGame game, int gameID, String gameName, 
String whiteUsername, String blackUsername) {
    public GameData withGameName(String newGameName) {
        return new GameData(this.game, this.gameID, newGameName, this.whiteUsername, this.blackUsername);
    }
}
