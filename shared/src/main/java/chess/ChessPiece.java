package chess;

import java.util.Collection;
import piecemoves.*;
import java.util.Objects;
import com.google.gson.annotations.Expose;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    @Expose private ChessGame.TeamColor pieceColor;
    @Expose private ChessPiece.PieceType type;
    private Collection<ChessMove> moves;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    //for copying chess pieces
     public ChessPiece(ChessPiece other) {
        this.pieceColor = other.pieceColor;
        this.type = other.type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    public void setTeamColor(ChessGame.TeamColor pieceColor) {
        this.pieceColor = pieceColor;
    }

    public void setPieceType(ChessPiece.PieceType type) {
        this.type = type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        if (type == PieceType.BISHOP) {
            return BishopMove.getBishopMoves(board, myPosition);
        }
        else if (type == PieceType.KING) {
            return KingMove.getKingMoves(board, myPosition);
        }
        else if (type == PieceType.KNIGHT) {
            return KnightMove.getKnightMoves(board, myPosition);
        }
        else if (type == PieceType.QUEEN) {
            return QueenMove.getQueenMoves(board, myPosition);
        }
        else if (type == PieceType.ROOK) {
            return RookMove.getRookMoves(board, myPosition);
        }
        else if (type == PieceType.PAWN) {
            return PawnMove.getPawnMoves(board, myPosition);
        }
        else {
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChessPiece that)) {
            return false;
        }
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }

    @Override
    public String toString() {
        return "ChessPiece{" +
                "pieceColor=" + pieceColor +
                ", type=" + type +
                ", moves=" + moves +
                '}';
    }
}

