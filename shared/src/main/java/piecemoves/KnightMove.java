package piecemoves;

import java.util.Collection;
import chess.*;
import java.util.ArrayList;
import java.lang.Math;

public class KnightMove {
    public static Collection<ChessMove> getKnightMoves(ChessBoard board, ChessPosition start) {
        Collection<ChessMove> moves = new ArrayList<>();
        ChessPiece knight = board.getPiece(start);

        for (int i = -2; i < 3; i++) { 
            if (i == 0) { //knight doesn't have possible moves in same row
                i++;
            }

            for (int j = 2; j > -3; j--) { 
                if (j == 0) { //knight doesn't have possible moves in same col
                    j--;
                }
                int row = start.getRow() + i;
                int col = start.getColumn() + j;

                if (Math.abs(i) != Math.abs(j) && //must be L shaped move
                    row >= 1 && row <= 8 && col >= 1 && col <= 8) { //and must be in bounds
                        
                    ChessPosition potentialPos = new ChessPosition(row, col); //examine potential space
                    ChessPiece potentialPosPiece = board.getPiece(potentialPos); //and if a piece is present

                    if (potentialPosPiece == null || //empty space is valid
                    potentialPosPiece.getTeamColor() != knight.getTeamColor()) { //enemy space is valid
                        moves.add(new ChessMove(start, potentialPos, null));
                    }
                }    
            }    
        }
        return moves;
    }
}
