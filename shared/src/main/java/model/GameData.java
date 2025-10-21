package model;

import chess.*;

public record GameData(ChessGame game, int gameID, String gameName, 
String whiteUsername, String blackUsername) {

}
