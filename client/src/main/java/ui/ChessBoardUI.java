package ui;

import chess.*;
import static ui.EscapeSequences.*;

public class ChessBoardUI {

    private static final int BOARD_SIZE = 8;
    private static final String[] COL_LABELS = {"a", "b", "c", "d", "e", "f", "g", "h"};

    // Backgrounds
    private static final String LIGHT_SQUARE = SET_BG_COLOR_WHITE;
    private static final String DARK_SQUARE = SET_BG_COLOR_BLACK;
    private static final String RESET_BG = RESET_BG_COLOR;

    // Piece colors
    private static final String WHITE_PIECE = SET_TEXT_COLOR_BLUE;
    private static final String BLACK_PIECE = SET_TEXT_COLOR_RED;
    private static final String RESET_TEXT = SET_TEXT_COLOR_WHITE;

    // Labels
    private static final String LABEL = SET_TEXT_COLOR_YELLOW + SET_TEXT_BOLD;
    private static final String RESET_LABEL = RESET_TEXT_BOLD_FAINT + RESET_TEXT_COLOR;

    public static void drawBoard(ChessGame game, ChessGame.TeamColor playerColor) {
        ChessBoard board = game.getBoard();

        System.out.print(RESET_TEXT + RESET_BG);
        if (playerColor == ChessGame.TeamColor.BLACK) {
            drawBoardBlackPerspective(board);
        } else {
            drawBoardWhitePerspective(board);
        }
        System.out.println(RESET_TEXT + RESET_BG);
    }

    private static void drawBoardWhitePerspective(ChessBoard board) {
        printHeader(true);

        for (int row = 7; row >= 0; row--) {
            printRowLabel(row + 1);
            for (int col = 0; col < BOARD_SIZE; col++) {
                ChessPosition pos = new ChessPosition(row + 1, col + 1);
                boolean isLight = (row + col) % 2 == 1;
                printSquare(board.getPiece(pos), isLight);
            }
            printRowLabel(row + 1);
            System.out.println(RESET_BG);
        }

        printHeader(true);
    }

    private static void drawBoardBlackPerspective(ChessBoard board) {
        printHeader(false);

        for (int row = 0; row < BOARD_SIZE; row++) {
            printRowLabel(8 - row);
            for (int col = 7; col >= 0; col--) {
                ChessPosition pos = new ChessPosition(row + 1, col + 1);
                boolean isLight = (row + col) % 2 == 0;
                printSquare(board.getPiece(pos), isLight);
            }
            printRowLabel(8 - row);
            System.out.println(RESET_BG);
        }

        printHeader(false);
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

    private static void printSquare(ChessPiece piece, boolean isLight) {
        String bg = isLight ? LIGHT_SQUARE : DARK_SQUARE;
        System.out.print(bg);

        if (piece == null) {
            System.out.print(EMPTY);
        } else {
            String color = piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_PIECE : BLACK_PIECE;
            System.out.print(color + getPieceSymbol(piece) + RESET_TEXT);
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
}