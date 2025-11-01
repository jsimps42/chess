package piecemoves;

import java.util.Collection;
import chess.*;
import java.util.ArrayList;

public class PawnMove {
    public static Collection<ChessMove> getPawnMoves(ChessBoard board, ChessPosition start) {
        ChessPiece pawn = board.getPiece(start);
        
        if (pawn == null) {
            return null;
        }

        Collection<ChessMove> moves = new ArrayList<>();
        ChessGame.TeamColor color = pawn.getTeamColor();
        int row = start.getRow();
        int col = start.getColumn();
        int direction = (color == ChessGame.TeamColor.WHITE) ? 1 : -1;
        boolean opener = (color == ChessGame.TeamColor.WHITE && row == 2) ||
                               (color == ChessGame.TeamColor.BLACK && row == 7);
        boolean promotion = (color == ChessGame.TeamColor.WHITE && row + direction == 8) ||
                                 (color == ChessGame.TeamColor.BLACK && row + direction == 1);

        ChessPiece.PieceType[] promotionPieces = {
            ChessPiece.PieceType.QUEEN,
            ChessPiece.PieceType.KNIGHT,
            ChessPiece.PieceType.BISHOP,
            ChessPiece.PieceType.ROOK
        };

        // Straight move
        ChessPosition potentialStraight = new ChessPosition(row + direction, col);
        if (board.getPiece(potentialStraight) == null) {
            if (promotion) {
                for (ChessPiece.PieceType piece : promotionPieces) {
                    moves.add(new ChessMove(start, potentialStraight, piece));
                }    
            }
            else {
                moves.add(new ChessMove(start, potentialStraight, null));
            }

            // 2 space opener
            if (opener) {
                ChessPosition potentialOpener = new ChessPosition(row + direction * 2, col);
                if (board.getPiece(potentialOpener) == null) {
                    moves.add(new ChessMove(start, potentialOpener, null));
                }
            }
        }

        // Captures
        for (int i = -1; i <= 1; i += 2) {
            col = start.getColumn() + i;
            if (col < 1 || col > 8) {
                continue;
            }

            ChessPosition potentialDiagonal = new ChessPosition(row + direction, col);
            ChessPiece diagonalPiece = board.getPiece(potentialDiagonal);
            if (diagonalPiece != null && diagonalPiece.getTeamColor() != color) {
                if (promotion) {
                    for (ChessPiece.PieceType piece : promotionPieces) {
                        moves.add(new ChessMove(start, potentialDiagonal, piece));
                    }    
                }
                else {
                    moves.add(new ChessMove(start, potentialDiagonal, null));
                }
            }
        }
        return moves;
    }
}