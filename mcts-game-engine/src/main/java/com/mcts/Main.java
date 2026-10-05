package com.mcts;

import com.mcts.engine.MCTSEngine;
import com.mcts.game.GameState;
import com.mcts.game.reversi.ReversiHeuristic;
import com.mcts.game.reversi.ReversiState;
import com.mcts.game.tictactoe.TicTacToeState;
import com.mcts.policy.DepthLimitedPolicy;
import com.mcts.policy.RandomRolloutPolicy;

/**
 * Demonstration driver: MCTS vs. MCTS on both Tic-Tac-Toe and Reversi.
 *
 * <p>Player 1 uses a depth-limited simulation policy (where applicable),
 * player 2 uses full random rollouts. Each move is printed with the
 * resulting board state and visit statistics.</p>
 */
public class Main {

    private static final int ITERATIONS = 2000;

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║      MCTS Game AI Engine — Demo         ║");
        System.out.println("╚══════════════════════════════════════════╝\n");

        playDemo("Tic-Tac-Toe",
                new TicTacToeState(),
                new MCTSEngine(new RandomRolloutPolicy(), ITERATIONS),
                new MCTSEngine(new RandomRolloutPolicy(), ITERATIONS));

        System.out.println("\n" + "=".repeat(50) + "\n");

        ReversiHeuristic heuristic = new ReversiHeuristic();
        playDemo("Reversi (8×8)",
                new ReversiState(),
                new MCTSEngine(new DepthLimitedPolicy(12, heuristic), ITERATIONS),
                new MCTSEngine(new RandomRolloutPolicy(), ITERATIONS / 2));
    }

    private static void playDemo(String gameName, GameState state,
                                  MCTSEngine engine1, MCTSEngine engine2) {
        System.out.println("--- " + gameName + " ---");
        System.out.println("Player 1: " + engine1.getPolicy().getClass().getSimpleName()
                + " (" + engine1.getIterations() + " iterations)");
        System.out.println("Player 2: " + engine2.getPolicy().getClass().getSimpleName()
                + " (" + engine2.getIterations() + " iterations)");
        System.out.println();
        System.out.println(state);
        System.out.println();

        int turnNumber = 1;
        while (!state.isTerminal()) {
            MCTSEngine active = (state.getCurrentPlayer() == 1) ? engine1 : engine2;
            MCTSEngine other  = (state.getCurrentPlayer() == 1) ? engine2 : engine1;

            long start = System.nanoTime();
            int move = active.bestMove(state);
            long elapsed = (System.nanoTime() - start) / 1_000_000;

            // Report
            int rootVisits = active.getRoot() != null ? active.getRoot().getVisits() : 0;
            System.out.printf("Turn %2d | Player %d plays %-6s | %4d ms | %d tree visits%n",
                    turnNumber, state.getCurrentPlayer(),
                    state.moveToString(move), elapsed, rootVisits);

            // Advance both trees (subtree reuse)
            active.advanceTree(move);
            other.advanceTree(move);

            state = state.applyMove(move);
            turnNumber++;
        }

        System.out.println();
        System.out.println(state);
        int winner = state.getWinner();
        System.out.println(winner == 0 ? "\nResult: Draw"
                                       : "\nResult: Player " + winner + " wins!");
    }
}
