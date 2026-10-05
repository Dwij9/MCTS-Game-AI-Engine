package com.mcts.game.reversi;

import com.mcts.game.GameState;
import com.mcts.policy.HeuristicEvaluator;

/**
 * Position-weighted heuristic evaluator for Reversi.
 *
 * <p>Assigns positional weights to each disc on the board:</p>
 * <ul>
 *   <li><b>Corner</b> — base 1.0 + bonus 2.0 = <b>3.0</b></li>
 *   <li><b>Edge (non-corner)</b> — base 1.0 + bonus 0.5 = <b>1.5</b></li>
 *   <li><b>Interior</b> — base <b>1.0</b></li>
 * </ul>
 *
 * <p>The weighted piece counts are normalised to [0, 1] so the returned
 * value represents the evaluated player's share of total positional strength.</p>
 */
public class ReversiHeuristic implements HeuristicEvaluator {

    /** Bonus weight added for corner occupancy. */
    public static final double CORNER_BONUS = 2.0;
    /** Bonus weight added for edge (non-corner) occupancy. */
    public static final double EDGE_BONUS   = 0.5;
    /** Base weight for every disc regardless of position. */
    public static final double BASE_WEIGHT  = 1.0;

    @Override
    public double evaluate(GameState state, int player) {
        if (!(state instanceof ReversiState)) {
            throw new IllegalArgumentException("ReversiHeuristic requires a ReversiState");
        }
        ReversiState rs = (ReversiState) state;

        // Terminal states get exact scores
        if (rs.isTerminal()) {
            int winner = rs.getWinner();
            if (winner == 0) return 0.5;
            return winner == player ? 1.0 : 0.0;
        }

        int opponent = (player == 1) ? 2 : 1;
        double playerScore = 0.0;
        double opponentScore = 0.0;

        int[][] board = rs.getBoard();
        for (int r = 0; r < ReversiState.SIZE; r++) {
            for (int c = 0; c < ReversiState.SIZE; c++) {
                if (board[r][c] == ReversiState.EMPTY) continue;

                double weight = BASE_WEIGHT;
                if (ReversiState.isCorner(r, c)) {
                    weight += CORNER_BONUS;
                } else if (ReversiState.isEdge(r, c)) {
                    weight += EDGE_BONUS;
                }

                if (board[r][c] == player) {
                    playerScore += weight;
                } else {
                    opponentScore += weight;
                }
            }
        }

        double total = playerScore + opponentScore;
        if (total == 0.0) return 0.5;
        return playerScore / total;
    }
}
