package piecemoves;

import java.util.Collection;
import chess.*;
import java.util.ArrayList;

public class BishopMove {
    public static Collection<ChessMove> getBishopMoves(ChessBoard board, ChessPosition start) {
        Collection<ChessMove> moves = new ArrayList<>();
        ChessPiece bishop = board.getPiece(start);
        int[] posMovRow = {1, 1, -1, -1}; //The possible directions bishop can move 
        int[] posMovCol = {-1, 1, -1, 1}; //by row and column

        for (int i = 0; i < 4; i++) { //running possible moves in each direction (i)
            int row = start.getRow();
            int col = start.getColumn();

            while (true) {
                row += posMovRow[i];
                col += posMovCol[i];

                if (row < 0 || row > 7 || col < 0 || col > 7) {
                    break;
                }

                ChessPosition potentialPos = new ChessPosition(row, col);
                ChessPiece potentialPosPiece = board.getPiece(potentialPos);

                if (potentialPosPiece == null) {
                    moves.add(new ChessMove(start, potentialPos, null));
                }

                else if (potentialPosPiece.getTeamColor() != bishop.getTeamColor()) {
                    moves.add(new ChessMove(start, potentialPos, null));
                }
                
                else {
                    break;
                }
            }
        }
        return moves;
    }
}
