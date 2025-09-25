package chess;

import java.util.Collection;
import java.util.ArrayList;
import piecemoves.*;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private TeamColor teamTurn;
    private ChessBoard currentBoard;
    private ChessPosition whiteKingSpace;
    private ChessPosition blackKingSpace;

    public ChessGame() {
        teamTurn = TeamColor.WHITE;
        currentBoard.resetBoard();
        whiteKingSpace = new ChessPosition (1, 5);
        blackKingSpace = new ChessPosition (8, 5);
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece movingPiece = currentBoard.getPiece(startPosition);
        TeamColor teamColor = movingPiece.getTeamColor();
        ChessPosition kingSpace;
        ChessPiece king;
        Collection<ChessMove> validMoves = movingPiece.pieceMoves(currentBoard, startPosition);
        ChessBoard backupBoard = currentBoard;

        if (teamColor == TeamColor.WHITE) {
                kingSpace = whiteKingSpace;
                king = currentBoard.getPiece(kingSpace);
            }
            else {
                kingSpace = blackKingSpace;
                king = currentBoard.getPiece(kingSpace);
            }

        for (ChessMove move : validMoves) {
            //change board and check for check
            if (teamColor == TeamColor.WHITE && movingPiece == king) {
                whiteKingSpace = move.getEndPosition();
            }
            else if(teamColor == TeamColor.BLACK && movingPiece == king) {
                blackKingSpace = move.getEndPosition();
            }

            currentBoard.addPiece(move.getEndPosition(), movingPiece); 
            currentBoard.addPiece(move.getEndPosition(), null); 

            if (isInCheck(teamColor)) {
                validMoves.remove(move); //remove invalid moves
            }

            //Reset board
            if (teamColor == TeamColor.WHITE && movingPiece == king) {
                whiteKingSpace = kingSpace;
            }
            else if(teamColor == TeamColor.BLACK && movingPiece == king) {
                blackKingSpace = kingSpace;
            }
            currentBoard = backupBoard;
        }
        return validMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition start = move.getStartPosition();
        //check validity
        if (!validMoves(start).contains(move)) {
            return;
        }

        ChessPiece movingPiece = currentBoard.getPiece(start);
        TeamColor teamColor = movingPiece.getTeamColor();
        
        //update board state
        if (teamColor == TeamColor.WHITE && movingPiece == currentBoard.getPiece(whiteKingSpace)) {
            whiteKingSpace = move.getEndPosition();
        }
        else if(teamColor == TeamColor.BLACK && movingPiece == currentBoard.getPiece(blackKingSpace)) {
            blackKingSpace = move.getEndPosition();
        }
        currentBoard.addPiece(move.getEndPosition(), movingPiece);
        currentBoard.addPiece(start, null);
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingSpace;
        int row;
        int col;
        ChessPosition attackerPos;
        ChessPiece attacker;
        
        if (teamColor == TeamColor.WHITE) {
            kingSpace = whiteKingSpace;
            row = whiteKingSpace.getRow() + 1;

            for (int i = -1; i <= 1; i +=2) {
                col = whiteKingSpace.getColumn() + i;
                if (row < 1 || row > 8 || col < 1 || col > 8) {
                    continue;
                }
                else {
                    attackerPos = new ChessPosition (row, col);
                    attacker = currentBoard.getPiece(attackerPos);

                    if (attacker.getTeamColor() != teamColor && attacker.getPieceType() == ChessPiece.PieceType.PAWN) {
                        return true;
                    }
                }
            }
        }
        else {
            kingSpace = blackKingSpace;
            row = blackKingSpace.getRow() - 1;

            for (int i = -1; i <= 1; i +=2) {
                col = blackKingSpace.getColumn() + i;
                if (row < 1 || row > 8 || col < 1 || col > 8) {
                    continue;
                }
                else {
                    attackerPos = new ChessPosition (row, col);
                    attacker = currentBoard.getPiece(attackerPos);

                    if (attacker.getTeamColor() != teamColor && attacker.getPieceType() == ChessPiece.PieceType.PAWN) {
                        return true;
                    }
                }
            }
        }
        

        for (ChessMove move : BishopMove.getBishopMoves(currentBoard, kingSpace)) {
            attackerPos = move.getEndPosition();
            attacker = currentBoard.getPiece(attackerPos);
            if (attacker.getTeamColor() != teamColor && (attacker.getPieceType() == ChessPiece.PieceType.BISHOP || 
                attacker.getPieceType() == ChessPiece.PieceType.QUEEN)) {
                return true;
            }
        }
        for (ChessMove move : RookMove.getRookMoves(currentBoard, kingSpace)) {
            attackerPos = move.getEndPosition();
            attacker = currentBoard.getPiece(attackerPos);
            if (attacker.getTeamColor() != teamColor && (attacker.getPieceType() == ChessPiece.PieceType.ROOK || 
                attacker.getPieceType() == ChessPiece.PieceType.QUEEN)) {
                return true;
            }
        }
        for (ChessMove move : KnightMove.getKnightMoves(currentBoard, kingSpace)) {
            attackerPos = move.getEndPosition();
            attacker = currentBoard.getPiece(attackerPos);
            if (attacker.getTeamColor() != teamColor && attacker.getPieceType() == ChessPiece.PieceType.KNIGHT) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) { 
            return false; //No check, no mate
        }

        //run through each space
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPosition position = new ChessPosition(i, j);
                ChessPiece piece = currentBoard.getPiece(position);
                //Is this piece the same team? Can it make a move to get out of check?
                if (piece.getTeamColor() == teamColor && !(validMoves(position).isEmpty())) {
                    return false; //Can get out of check, then not checkmate
                }
            }
        }
        return true; //None of the pieces could stop the check? Checkmated
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) { //if in check, not stalemate
            return false;
        }

        //run through each space
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPosition position = new ChessPosition(i, j);
                ChessPiece piece = currentBoard.getPiece(position);
                //Is this piece the same team? Can it make a valid move?
                if (piece.getTeamColor() == teamColor && !(validMoves(position).isEmpty())) {
                    return false; //Yes, then not stalemate
                }
            }
        }
        return true; //None of the pieces could move? Stalemate
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        currentBoard = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return currentBoard;
    }
}
