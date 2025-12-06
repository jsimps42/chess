package ui;

import chess.*;

import java.util.ArrayList;
import java.util.Collection;
import static ui.EscapeSequences.*;

public class ChessBoardUI {

    private static final int BOARD_SIZE = 8;
    private static final String[] COL_LABELS = {"a", "b", "c", "d", "e", "f", "g", "h"};

    private static final String LIGHT_SQUARE = SET_BG_COLOR_WHITE;
    private static final String DARK_SQUARE = SET_BG_COLOR_BLACK;
    private static final String RESET_BG = RESET_BG_COLOR;
    private static final String DARK_HIGHLIGHT = SET_BG_COLOR_DARK_GREEN;
    private static final String LIGHT_HIGHLIGHT = SET_BG_COLOR_GREEN;

    private static final String WHITE_COLOR = SET_TEXT_COLOR_BLUE;
    private static final String BLACK_COLOR = SET_TEXT_COLOR_RED;
    private static final String WHITE_PIECE_HIGHLIGHT = SET_TEXT_COLOR_LIGHT_GREY;
    private static final String BLACK_PIECE_HIGHLIGHT = SET_TEXT_COLOR_MAGENTA;
    private static final String RESET_TEXT = SET_TEXT_COLOR_WHITE;

    private static final String LABEL = SET_TEXT_COLOR_YELLOW + SET_TEXT_BOLD;
    private static final String RESET_LABEL = RESET_TEXT_BOLD_FAINT + RESET_TEXT_COLOR;

    public static void drawBoard(
          ChessGame game, 
          ChessGame.TeamColor playerColor, 
          Collection<ChessMove> legalMoves) {
        ChessBoard board = game.getBoard();
        if (board == null) {
            board = new ChessBoard();
            board.resetBoard();
        }

        System.out.print(RESET_TEXT + RESET_BG);
        if (playerColor == ChessGame.TeamColor.BLACK) {
            drawBoardBlackPerspective(board, playerColor, legalMoves);
        } else {
            drawBoardWhitePerspective(board, playerColor, legalMoves);
        }
        System.out.println(RESET_TEXT + RESET_BG);
    }

    private static void drawBoardWhitePerspective(
          ChessBoard board, 
          ChessGame.TeamColor playerColor,
          Collection<ChessMove> legalMoves) {
        Collection<ChessPosition> squaresToHighlight = highlightSquares(legalMoves);
        printHeader(true);
        
        for (int row = 7; row >= 0; row--) {
            printRowLabel(row + 1);
            for (int col = 0; col < BOARD_SIZE; col++) {
                ChessPosition pos = new ChessPosition(row + 1, col + 1);
                ChessPiece piece = board.getPiece(pos);
                boolean isLight = isLightInWhiteView(row, col);
                boolean highlight = squaresToHighlight.contains(pos);
                boolean isCheckedPiece = pos == legalMoves.iterator().next().getStartPosition();
                printSquare(piece, isLight, playerColor, highlight, isCheckedPiece);
            }
            printRowLabel(row + 1);
            System.out.println(RESET_BG);
        }
        printHeader(true);
    }

    private static void drawBoardBlackPerspective(
          ChessBoard board, 
          ChessGame.TeamColor playerColor, 
          Collection<ChessMove> legalMoves) {
        Collection<ChessPosition> squaresToHighlight = highlightSquares(legalMoves);
        printHeader(false);
        
        for (int row = 0; row < BOARD_SIZE; row++) {
            printRowLabel(row + 1);
            for (int col = 0; col < BOARD_SIZE; col++) {
                ChessPosition pos = new ChessPosition(8 - row, 8 - col);
                ChessPiece piece = board.getPiece(pos);
                boolean isLight = isLightInBlackView(row, col);
                boolean highlight = squaresToHighlight.contains(pos);
                boolean isCheckedPiece = pos == legalMoves.iterator().next().getStartPosition();
                printSquare(piece, isLight, playerColor, highlight, isCheckedPiece);
            }
            printRowLabel(row + 1);
            System.out.println(RESET_BG);
        }
        printHeader(false);
    }

    private static boolean isLightInWhiteView(int row, int col) {
        return ((row + col) % 2) == 1;
    }

    private static boolean isLightInBlackView(int row, int col) {
        return ((row + col) % 2) == 0;
    }

    private static void printHeader(boolean whitePerspective) {
        System.out.print("  ");
        for (int i = 0; i < BOARD_SIZE; i++) {
            String label = whitePerspective ? COL_LABELS[i] : COL_LABELS[7 - i];
            System.out.print(LABEL + " " + label + " " + RESET_LABEL);
        }
        System.out.println("  ");
    }

    private static void printRowLabel(int row) {
        System.out.print(LABEL + " " + row + " " + RESET_LABEL);
    }

    private static void printSquare(
          ChessPiece piece, 
          boolean isLight, 
          ChessGame.TeamColor playerColor,
          boolean highlight,
          boolean isCheckedPiece) {
        String bg;
        if (highlight) {
            bg = isLight ? LIGHT_HIGHLIGHT : DARK_HIGHLIGHT;
        }
        else {
            bg = isLight ? LIGHT_SQUARE : DARK_SQUARE;
        }
        System.out.print(bg);

        if (piece == null) {
            System.out.print(EMPTY);
        } else {
            String textColor = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? WHITE_COLOR : BLACK_COLOR;
            if (isCheckedPiece) {
                textColor = textColor == WHITE_COLOR ? WHITE_PIECE_HIGHLIGHT : BLACK_PIECE_HIGHLIGHT;
            }
            System.out.print(textColor + getPieceSymbol(piece) + RESET_TEXT);
        }
        System.out.print(RESET_BG);
    }

    private static String getPieceSymbol(ChessPiece piece) {
        return switch (piece.getPieceType()) {
            case KING -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_KING : BLACK_KING;
            case QUEEN -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_QUEEN : BLACK_QUEEN;
            case ROOK -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_ROOK : BLACK_ROOK;
            case BISHOP -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_BISHOP : BLACK_BISHOP;
            case KNIGHT -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_KNIGHT : BLACK_KNIGHT;
            case PAWN -> piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_PAWN : BLACK_PAWN;
        };
    }

    private static Collection<ChessPosition> highlightSquares(Collection<ChessMove> legalMoves) {
        Collection<ChessPosition> highlightedSquares = new ArrayList<>();
        for (ChessMove move : legalMoves) {
            highlightedSquares.add(move.getEndPosition());
        }
        return highlightedSquares;
    }
}