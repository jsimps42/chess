package piecemoves;

import java.util.Collection;
import chess.*;
import java.util.ArrayList;

public class BishopMove {
    public static Collection<ChessMove> getBishopMoves(ChessBoard board, ChessPosition start) {
        ChessPiece bishop = board.getPiece(start);

        if (bishop == null) {
            return null;
        }

        Collection<ChessMove> moves = new ArrayList<>();
        int[] posMovRow = {1, 1, -1, -1}; //The possible directions bishop can move 
        int[] posMovCol = {-1, 1, -1, 1}; //by row and column

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

                else if (potentialPosPiece.getTeamColor() != bishop.getTeamColor()) { //occupied by enemy so still valid
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
