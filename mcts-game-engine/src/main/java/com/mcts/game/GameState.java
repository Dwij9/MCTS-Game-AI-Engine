package com.mcts.game;

import java.util.List;

/**
 * Abstract representation of a two-player, zero-sum, perfect-information game state.
 * Each game (Tic-Tac-Toe, Reversi, etc.) implements this interface so the MCTS
 * engine can operate on it without knowledge of the underlying rules.
 */
public interface GameState {

    /**
     * Returns all legal moves for the current player.
     * Moves are encoded as integers whose meaning is game-specific
     * (e.g., row * boardSize + col). A "pass" move is represented as -1.
     *
     * @return list of legal move codes; never null, empty only if terminal
     */
    List<Integer> getLegalMoves();

    /**
     * Applies a move and returns the resulting state.
     * The original state is not mutated.
     *
     * @param move a value previously returned by {@link #getLegalMoves()}
     * @return a new GameState reflecting the move
     */
    GameState applyMove(int move);

    /**
     * @return true if the game has ended (win, loss, or draw)
     */
    boolean isTerminal();

    /**
     * @return the player whose turn it is: 1 or 2
     */
    int getCurrentPlayer();

    /**
     * @return the winner: 1 or 2 if a player won, 0 for a draw,
     *         -1 if the game is still in progress
     */
    int getWinner();

    /**
     * @return a deep copy of this state
     */
    GameState copy();

    /**
     * Converts an integer move code to a human-readable string.
     *
     * @param move the move code
     * @return a display string, e.g. "(2,3)" or "PASS"
     */
    String moveToString(int move);
}
