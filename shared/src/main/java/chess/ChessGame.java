package chess;

import java.util.Collection;
import piecemoves.*;
import java.util.ArrayList;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private TeamColor teamTurn;
    private ChessBoard currentBoard = new ChessBoard();

    public ChessGame() {
        teamTurn = TeamColor.WHITE;
        currentBoard.resetBoard();
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
        if (movingPiece == null) {
            return null;
        }

        TeamColor teamColor = movingPiece.getTeamColor();
        Collection<ChessMove> possibleMoves = movingPiece.pieceMoves(currentBoard, startPosition);
        Collection<ChessMove> validMoves = new ArrayList<>();

        for (ChessMove move : possibleMoves) {
            //Copy the current board to a simulation board
            ChessBoard simulatedBoard = new ChessBoard(currentBoard);
            ChessGame simulatedGame = new ChessGame();

            simulatedGame.setBoard(simulatedBoard);
            simulatedGame.setTeamTurn(this.teamTurn);  //probably unecessary, but set team

            //Simulate the move
            simulatedBoard.addPiece(move.getEndPosition(), movingPiece);
            simulatedBoard.addPiece(startPosition, null);

            // No check = valid
            if (!simulatedGame.isInCheck(teamColor)) {
                validMoves.add(move);
            }
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
        ChessPiece.PieceType promotionPiece = move.getPromotionPiece();

        //check if move is valid
        Collection<ChessMove> moves = validMoves(start);
        if (moves == null || !moves.contains(move)) {
            throw new InvalidMoveException("Invalid move: No valid moves from this position.");
        }

        //check for piece
        ChessPiece movingPiece = currentBoard.getPiece(start);
        if (movingPiece == null) {
            throw new InvalidMoveException("Invalid move: No piece at the starting position.");
        }

        //correct team moving?
        TeamColor teamColor = movingPiece.getTeamColor();
        if (teamColor != teamTurn) {
            throw new InvalidMoveException("Invalid move: It is not your turn.");
        }
        
        //update board state
        if (promotionPiece != null) {
            currentBoard.addPiece(move.getEndPosition(), new ChessPiece (teamColor, promotionPiece));
        }
        else {
            currentBoard.addPiece(move.getEndPosition(), movingPiece);
        }
        currentBoard.addPiece(start, null);

        //change team turn
        if (teamTurn == TeamColor.WHITE) {
            teamTurn = TeamColor.BLACK;
        }
        else {
            teamTurn = TeamColor.WHITE;
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingSpace = findKing(teamColor);
        int row = kingSpace.getRow() + ((teamColor == TeamColor.WHITE) ? 1 : -1);
        int col;
        ChessPosition attackerPos;
        ChessPiece attacker;
        
        for (int i = -1; i <= 1; i +=2) {
            col = kingSpace.getColumn() + i;
            if (row < 1 || row > 8 || col < 1 || col > 8) {                    
                continue;
            }
            else {
                attackerPos = new ChessPosition (row, col);
                attacker = currentBoard.getPiece(attackerPos);

                if (attacker == null) {
                    continue;
                }

                if (attacker.getTeamColor() != teamColor && attacker.getPieceType() == ChessPiece.PieceType.PAWN) {
                    return true;
                }
            }
        }
        
        Collection<ChessMove> diagonals = BishopMove.getBishopMoves(currentBoard, kingSpace);

        if (diagonals != null) {
            for (ChessMove move : diagonals) {
                attackerPos = move.getEndPosition();
                attacker = currentBoard.getPiece(attackerPos);

                if (attacker == null) {
                    continue;
                }

                if (attacker.getTeamColor() != teamColor && (attacker.getPieceType() == ChessPiece.PieceType.BISHOP || 
                    attacker.getPieceType() == ChessPiece.PieceType.QUEEN)) {
                    return true;
                }
            }
        }

        Collection<ChessMove> straights = RookMove.getRookMoves(currentBoard, kingSpace);
        if (straights != null) {
            for (ChessMove move : straights) {
                attackerPos = move.getEndPosition();
                attacker = currentBoard.getPiece(attackerPos);

                if (attacker == null) {
                    continue;
                }

                if (attacker.getTeamColor() != teamColor && (attacker.getPieceType() == ChessPiece.PieceType.ROOK || 
                    attacker.getPieceType() == ChessPiece.PieceType.QUEEN)) {
                    return true;
                }
            }
        }
        
        Collection<ChessMove> knightAttacks = KnightMove.getKnightMoves(currentBoard, kingSpace);

        if (knightAttacks != null) {
            for (ChessMove move : knightAttacks) {
                attackerPos = move.getEndPosition();
                attacker = currentBoard.getPiece(attackerPos);

                if (attacker == null) {
                    continue;
                }  

                if (attacker.getTeamColor() != teamColor && attacker.getPieceType() == ChessPiece.PieceType.KNIGHT) {
                    return true;
                }
            }
        }

        Collection<ChessMove> kingAttacks = KingMove.getKingMoves(currentBoard, kingSpace);

        if (kingAttacks != null) {
            for (ChessMove move : kingAttacks) {
                attackerPos = move.getEndPosition();
                attacker = currentBoard.getPiece(attackerPos);

                if (attacker == null) {
                    continue;
                }  

                if (attacker.getTeamColor() != teamColor && attacker.getPieceType() == ChessPiece.PieceType.KING) {
                    return true;
                }
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

        return noValidMoves(teamColor);
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
        return noValidMoves(teamColor);
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

    private ChessPosition findKing(TeamColor color) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = currentBoard.getPiece(pos);
                if (piece != null && piece.getPieceType() == ChessPiece.PieceType.KING && piece.getTeamColor() == color) {
                    return pos;
                }
            }
        }
    return null;
}

    private boolean noValidMoves(TeamColor teamColor) {
        //run through each space
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPosition position = new ChessPosition(i, j);
                ChessPiece piece = currentBoard.getPiece(position);

                //Is piece valid?
                if (piece == null) {
                    continue;
                }

                //Is this piece the same team? Can it make a valid move?
                if (piece.getTeamColor() == teamColor && !(validMoves(position).isEmpty())) {
                    return false; //Yes, then valid moves remain
                }
            }
        }
        return true; //None of the pieces could move
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((teamTurn == null) ? 0 : teamTurn.hashCode());
        result = prime * result + ((currentBoard == null) ? 0 : currentBoard.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ChessGame other = (ChessGame) obj;
        if (teamTurn != other.teamTurn) {
            return false;
        }
        if (currentBoard == null) {
            if (other.currentBoard != null) {
                return false;
            }
        } else if (!currentBoard.equals(other.currentBoard)) {
            return false;
        }
        return true;
    }
}
