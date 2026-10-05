package com.mcts.game.reversi;

import com.mcts.game.GameState;

import java.util.ArrayList;
import java.util.List;

/**
 * Standard 8×8 Reversi (Othello).
 *
 * <p>Cells are indexed row-major: move = row * 8 + col.  A pass is
 * represented as move code <b>-1</b>.  The game ends when neither
 * player has a legal placement (two consecutive passes).</p>
 *
 * <p>Players: 1 = BLACK, 2 = WHITE.  BLACK moves first.
 * Initial setup has the standard four-piece centre arrangement.</p>
 */
public class ReversiState implements GameState {

    public static final int SIZE = 8;
    public static final int EMPTY = 0;
    public static final int BLACK = 1;
    public static final int WHITE = 2;
    public static final int PASS_MOVE = -1;

    private static final int[][] DIRECTIONS = {
            {-1, -1}, {-1, 0}, {-1, 1},
            { 0, -1},          { 0, 1},
            { 1, -1}, { 1, 0}, { 1, 1}
    };

    private final int[][] board;
    private final int currentPlayer;
    private final int consecutivePasses;

    /** Creates the standard starting position. */
    public ReversiState() {
        this.board = new int[SIZE][SIZE];
        // Classic Othello opening: white on d4/e5, black on d5/e4
        board[3][3] = WHITE;
        board[3][4] = BLACK;
        board[4][3] = BLACK;
        board[4][4] = WHITE;
        this.currentPlayer = BLACK;
        this.consecutivePasses = 0;
    }

    private ReversiState(int[][] board, int currentPlayer, int consecutivePasses) {
        this.board = board;
        this.currentPlayer = currentPlayer;
        this.consecutivePasses = consecutivePasses;
    }

    /* ------------------------------------------------------------------ */
    /*  GameState implementation                                           */
    /* ------------------------------------------------------------------ */

    @Override
    public List<Integer> getLegalMoves() {
        if (isTerminal()) return List.of();

        List<Integer> moves = new ArrayList<>();
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (board[r][c] == EMPTY && wouldFlip(r, c, currentPlayer)) {
                    moves.add(r * SIZE + c);
                }
            }
        }
        if (moves.isEmpty()) {
            moves.add(PASS_MOVE);  // must pass
        }
        return moves;
    }

    @Override
    public GameState applyMove(int move) {
        int[][] newBoard = copyBoard();
        int nextPlayer = opponent(currentPlayer);
        int newPasses;

        if (move == PASS_MOVE) {
            newPasses = consecutivePasses + 1;
        } else {
            int r = move / SIZE, c = move % SIZE;
            newBoard[r][c] = currentPlayer;
            flipDiscs(newBoard, r, c, currentPlayer);
            newPasses = 0;
        }
        return new ReversiState(newBoard, nextPlayer, newPasses);
    }

    @Override
    public boolean isTerminal() {
        return consecutivePasses >= 2;
    }

    @Override
    public int getCurrentPlayer() {
        return currentPlayer;
    }

    @Override
    public int getWinner() {
        if (!isTerminal()) return -1;
        int black = 0, white = 0;
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (board[r][c] == BLACK) black++;
                else if (board[r][c] == WHITE) white++;
            }
        }
        if (black > white) return BLACK;
        if (white > black) return WHITE;
        return 0; // draw
    }

    @Override
    public GameState copy() {
        return new ReversiState(copyBoard(), currentPlayer, consecutivePasses);
    }

    @Override
    public String moveToString(int move) {
        if (move == PASS_MOVE) return "PASS";
        char col = (char) ('a' + (move % SIZE));
        int row = (move / SIZE) + 1;
        return "" + col + row;
    }

    /* ------------------------------------------------------------------ */
    /*  Flip logic                                                         */
    /* ------------------------------------------------------------------ */

    /**
     * Checks whether placing {@code player}'s disc at (r, c) would flip
     * at least one opponent disc in any direction.
     */
    private boolean wouldFlip(int r, int c, int player) {
        int opp = opponent(player);
        for (int[] d : DIRECTIONS) {
            int dr = d[0], dc = d[1];
            int nr = r + dr, nc = c + dc;
            boolean foundOpponent = false;
            while (inBounds(nr, nc) && board[nr][nc] == opp) {
                foundOpponent = true;
                nr += dr;
                nc += dc;
            }
            if (foundOpponent && inBounds(nr, nc) && board[nr][nc] == player) {
                return true;
            }
        }
        return false;
    }

    /**
     * Flips all opponent discs sandwiched by the newly placed disc at (r, c).
     */
    private void flipDiscs(int[][] b, int r, int c, int player) {
        int opp = opponent(player);
        for (int[] d : DIRECTIONS) {
            int dr = d[0], dc = d[1];
            int nr = r + dr, nc = c + dc;
            List<int[]> toFlip = new ArrayList<>();
            while (inBounds(nr, nc) && b[nr][nc] == opp) {
                toFlip.add(new int[]{nr, nc});
                nr += dr;
                nc += dc;
            }
            if (!toFlip.isEmpty() && inBounds(nr, nc) && b[nr][nc] == player) {
                for (int[] cell : toFlip) {
                    b[cell[0]][cell[1]] = player;
                }
            }
        }
    }

    /* ------------------------------------------------------------------ */
    /*  Board queries (used by heuristic evaluator)                        */
    /* ------------------------------------------------------------------ */

    /**
     * Returns a deep copy of the board array, safe for external inspection.
     */
    public int[][] getBoard() {
        return copyBoard();
    }

    /** @return true if (r, c) is one of the four corners. */
    public static boolean isCorner(int r, int c) {
        return (r == 0 || r == SIZE - 1) && (c == 0 || c == SIZE - 1);
    }

    /** @return true if (r, c) is on any edge but not a corner. */
    public static boolean isEdge(int r, int c) {
        if (isCorner(r, c)) return false;
        return r == 0 || r == SIZE - 1 || c == 0 || c == SIZE - 1;
    }

    /**
     * Counts the number of discs belonging to the given player.
     */
    public int countDiscs(int player) {
        int count = 0;
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++)
                if (board[r][c] == player) count++;
        return count;
    }

    /* ------------------------------------------------------------------ */
    /*  Helpers                                                            */
    /* ------------------------------------------------------------------ */

    private static int opponent(int player) {
        return player == BLACK ? WHITE : BLACK;
    }

    private static boolean inBounds(int r, int c) {
        return r >= 0 && r < SIZE && c >= 0 && c < SIZE;
    }

    private int[][] copyBoard() {
        int[][] copy = new int[SIZE][SIZE];
        for (int r = 0; r < SIZE; r++) {
            System.arraycopy(board[r], 0, copy[r], 0, SIZE);
        }
        return copy;
    }

    /* ------------------------------------------------------------------ */
    /*  Display                                                            */
    /* ------------------------------------------------------------------ */

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("  a b c d e f g h\n");
        for (int r = 0; r < SIZE; r++) {
            sb.append(r + 1).append(' ');
            for (int c = 0; c < SIZE; c++) {
                int v = board[r][c];
                sb.append(v == EMPTY ? '.' : (v == BLACK ? 'B' : 'W'));
                if (c < SIZE - 1) sb.append(' ');
            }
            sb.append('\n');
        }
        sb.append("Black: ").append(countDiscs(BLACK))
          .append("  White: ").append(countDiscs(WHITE));
        return sb.toString();
    }
}
