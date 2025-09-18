package piecemoves;

import java.util.Collection;
import chess.*;
import java.util.ArrayList;

public class QueenMove {
    public static Collection<ChessMove> getQueenMoves(ChessBoard board, ChessPosition start) {
        Collection<ChessMove> moves = new ArrayList<>();

        //borrow from existing functions
        Collection<ChessMove> diagonal = BishopMove.getBishopMoves(board, start);
        Collection<ChessMove> straight = RookMove.getRookMoves(board, start);
        
        //copy it over
        for (ChessMove move : diagonal) {
            moves.add(move);
        }
        for (ChessMove move : straight) {
            moves.add(move);
        }

        return moves;
    }
}
