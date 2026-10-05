package com.mcts.game.tictactoe;

import com.mcts.game.GameState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Standard 3×3 Tic-Tac-Toe.
 *
 * <p>Board cells are indexed 0–8 (row-major): move = row * 3 + col.
 * Player 1 is X, player 2 is O. Empty cells are 0.</p>
 */
public class TicTacToeState implements GameState {

    private static final int SIZE = 3;
    private static final int[][] WIN_LINES = {
            {0, 1, 2}, {3, 4, 5}, {6, 7, 8},   // rows
            {0, 3, 6}, {1, 4, 7}, {2, 5, 8},   // columns
            {0, 4, 8}, {2, 4, 6}                // diagonals
    };

    private final int[] board;
    private final int currentPlayer;

    /** Creates the empty starting position. */
    public TicTacToeState() {
        this.board = new int[SIZE * SIZE];
        this.currentPlayer = 1;
    }

    private TicTacToeState(int[] board, int currentPlayer) {
        this.board = board;
        this.currentPlayer = currentPlayer;
    }

    /* ------------------------------------------------------------------ */
    /*  GameState implementation                                           */
    /* ------------------------------------------------------------------ */

    @Override
    public List<Integer> getLegalMoves() {
        List<Integer> moves = new ArrayList<>();
        if (checkWinner() != -1) return moves;
        for (int i = 0; i < board.length; i++) {
            if (board[i] == 0) moves.add(i);
        }
        return moves;
    }

    @Override
    public GameState applyMove(int move) {
        int[] newBoard = Arrays.copyOf(board, board.length);
        newBoard[move] = currentPlayer;
        int nextPlayer = (currentPlayer == 1) ? 2 : 1;
        return new TicTacToeState(newBoard, nextPlayer);
    }

    @Override
    public boolean isTerminal() {
        return checkWinner() != -1 || getLegalMoves().isEmpty();
    }

    @Override
    public int getCurrentPlayer() {
        return currentPlayer;
    }

    @Override
    public int getWinner() {
        int w = checkWinner();
        if (w != -1) return w;                         // 1 or 2 won
        if (getLegalMoves().isEmpty()) return 0;        // draw
        return -1;                                     // ongoing
    }

    @Override
    public GameState copy() {
        return new TicTacToeState(Arrays.copyOf(board, board.length), currentPlayer);
    }

    @Override
    public String moveToString(int move) {
        return "(" + (move / SIZE) + "," + (move % SIZE) + ")";
    }

    /* ------------------------------------------------------------------ */
    /*  Internals                                                          */
    /* ------------------------------------------------------------------ */

    /**
     * @return the winning player (1 or 2), or -1 if no one has won yet
     */
    private int checkWinner() {
        for (int[] line : WIN_LINES) {
            int a = board[line[0]], b = board[line[1]], c = board[line[2]];
            if (a != 0 && a == b && b == c) return a;
        }
        return -1;
    }

    /* ------------------------------------------------------------------ */
    /*  Display                                                            */
    /* ------------------------------------------------------------------ */

    public int[] getBoard() {
        return Arrays.copyOf(board, board.length);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                int v = board[r * SIZE + c];
                sb.append(v == 0 ? "." : (v == 1 ? "X" : "O"));
                if (c < SIZE - 1) sb.append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
