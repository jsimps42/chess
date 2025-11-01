package model;

import chess.ChessGame;

public record GameData(ChessGame game, int gameID, String gameName, 
                        String whiteUsername, String blackUsername) {
                            
}