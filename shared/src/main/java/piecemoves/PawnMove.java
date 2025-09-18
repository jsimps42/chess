package piecemoves;

import java.util.Collection;
import chess.*;
import java.util.ArrayList;

public class PawnMove {
    public static Collection<ChessMove> getPawnMoves(ChessBoard board, ChessPosition start) {
        Collection<ChessMove> moves = new ArrayList<>();
        ChessPiece pawn = board.getPiece(start);
        boolean promote = false;
        ChessPiece.PieceType[] promotionPieces = {ChessPiece.PieceType.QUEEN, 
            ChessPiece.PieceType.KNIGHT, ChessPiece.PieceType.BISHOP, 
            ChessPiece.PieceType.ROOK};
        int row = start.getRow();
        int extraMove = 0;

        
        if (pawn.getTeamColor() == ChessGame.TeamColor.WHITE) {
            row +=  1; //white moves up
            if (row == 8) { //promotion?
                promote = true;
            }
            if (start.getRow() == 2) {
                extraMove = 1;
            }
        }
        else {
            row -= 1; //black moves down
            if (row == 1) { //promotion?
                promote = true;
            }
            if (start.getRow() == 7) {
                extraMove = -1;
            }
        }

        for (int i = -1; i < 2; i++) {
                int col = start.getColumn() + i;

                if (row >= 1 && row <= 8 && col >= 1 && col <= 8) { //must be in bounds
                    ChessPosition potentialPos = new ChessPosition(row, col); //examine potential space
                    ChessPiece potentialPosPiece = board.getPiece(potentialPos); //and if a piece is present

                    if (potentialPosPiece == null) {
                        if(i == 0) { //empty space is valid on forward move
                            if (promote) { //promote?
                                for (ChessPiece.PieceType piece : promotionPieces) {
                                    moves.add(new ChessMove(start, potentialPos, piece));
                                }
                            }
                            else { 
                                moves.add(new ChessMove(start, potentialPos, null));
                            }

                            if (extraMove != 0) { //perform extra move if applicable
                                potentialPos = new ChessPosition(row + extraMove, col); //examine potential space
                                potentialPosPiece = board.getPiece(potentialPos); //and if a piece is present
                                if (potentialPosPiece == null) {
                                    moves.add(new ChessMove(start, potentialPos, null));
                                }
                            }
                        }
                    }    
                    else if (potentialPosPiece.getTeamColor() != pawn.getTeamColor() && i != 0) { //enemy space is valid on diagonal
                        if (promote) { //promote?
                                for (ChessPiece.PieceType piece : promotionPieces) {
                                    moves.add(new ChessMove(start, potentialPos, piece));
                                }
                            }
                        else {
                            moves.add(new ChessMove(start, potentialPos, null));
                        }
                    } 
                }    
            }

        return moves;
    }    
}
