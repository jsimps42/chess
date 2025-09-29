package piecemoves;

import java.util.Collection;
import chess.*;
import java.util.ArrayList;

public class RookMove {
    public static Collection<ChessMove> getRookMoves(ChessBoard board, ChessPosition start) {
        ChessPiece rook = board.getPiece(start);

        if (rook == null) {
            return null;
        }
        
        Collection<ChessMove> moves = new ArrayList<>();
        int[] posMovRow = {0, 0, -1, 1}; //The possible directions rook can move 
        int[] posMovCol = {-1, 1, 0, 0}; //by row and column

        for (int i = 0; i < 4; i++) { //running possible moves in each direction (i)
            int row = start.getRow();
            int col = start.getColumn();

            while (true) {
                row += posMovRow[i];
                col += posMovCol[i];

                if (row < 1 || row > 8 || col < 1 || col > 8) { //stop if out of bounds
                    break;
                }

                ChessPosition potentialPos = new ChessPosition(row, col); //examine potential space
                ChessPiece potentialPosPiece = board.getPiece(potentialPos); //and if a piece is present

                if (potentialPosPiece == null) { //empty space so valid move
                    moves.add(new ChessMove(start, potentialPos, null));
                }

                else if (potentialPosPiece.getTeamColor() != rook.getTeamColor()) { //occupied by enemy so still valid
                    moves.add(new ChessMove(start, potentialPos, null));
                    break;
                }

                else { //ally on space makes for invalid move
                    break;
                }
            }
        }
        return moves;
    }    
}
