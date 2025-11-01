package model;

import chess.ChessGame;

public record GameData(
        ChessGame game,
        int gameID,
        String gameName,
        String whiteUsername,
        String blackUsername) {

    public GameData withGameName(String newGameName) {
        return new GameData(game, gameID, newGameName, whiteUsername, blackUsername);
    }
}