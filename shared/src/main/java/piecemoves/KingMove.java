package piecemoves;

import java.util.Collection;
import chess.*;
import java.util.ArrayList;

public class KingMove {
    public static Collection<ChessMove> getKingMoves(ChessBoard board, ChessPosition start) {
        ChessPiece king = board.getPiece(start);

        if (king == null) {
            return null;
        }
        
        Collection<ChessMove> moves = new ArrayList<>();
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                if (i == 0 && j == 0) { //skipping checking starting position
                    j++;
                }

                int row = start.getRow() + i;
                int col = start.getColumn() + j;

                if (row >= 1 && row <= 8 && col >= 1 && col <= 8) { //must be in bounds
                    ChessPosition potentialPos = new ChessPosition(row, col); //examine potential space
                    ChessPiece potentialPosPiece = board.getPiece(potentialPos); //and if a piece is present

                    if (potentialPosPiece == null || //empty space is valid
                        potentialPosPiece.getTeamColor() != king.getTeamColor()) { //enemy space is valid
                            moves.add(new ChessMove(start, potentialPos, null));
                    }
                }
            }
        }
        return moves;
    }    
}
