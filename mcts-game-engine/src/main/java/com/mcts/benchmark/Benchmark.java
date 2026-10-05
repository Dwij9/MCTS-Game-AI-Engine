package com.mcts.benchmark;

import com.mcts.engine.MCTSEngine;
import com.mcts.game.GameState;
import com.mcts.game.reversi.ReversiHeuristic;
import com.mcts.game.reversi.ReversiState;
import com.mcts.game.tictactoe.TicTacToeState;
import com.mcts.policy.DepthLimitedPolicy;
import com.mcts.policy.RandomRolloutPolicy;
import com.mcts.policy.SimulationPolicy;

import java.util.List;
import java.util.Random;

/**
 * Benchmarking harness for the MCTS engine.
 *
 * <p>Runs controlled experiments that compare:</p>
 * <ol>
 *   <li>Full random rollout vs. depth-limited heuristic simulation at
 *       depths 8, 10, 12, 14, 16, 18</li>
 *   <li>Exploration constants 0.5, 1.0, √2, 2.0</li>
 * </ol>
 *
 * <p>Each configuration plays a set of games against a random opponent.
 * Per-game runtime and win rate are reported.</p>
 */
public class Benchmark {

    private static final int MCTS_ITERATIONS = 1000;
    private static final int GAMES_PER_CONFIG = 20;
    private static final Random RNG = new Random(42);

    /* ------------------------------------------------------------------ */
    /*  Entry point                                                        */
    /* ------------------------------------------------------------------ */

    public static void main(String[] args) {
        System.out.println("=== MCTS Game AI Engine — Benchmark Suite ===\n");

        benchmarkTicTacToe();
        System.out.println();
        benchmarkReversiDepths();
        System.out.println();
        benchmarkExplorationConstants();
    }

    /* ------------------------------------------------------------------ */
    /*  Tic-Tac-Toe: MCTS vs random                                        */
    /* ------------------------------------------------------------------ */

    private static void benchmarkTicTacToe() {
        System.out.println("--- Tic-Tac-Toe: MCTS (random rollout) vs. Random Player ---");
        SimulationPolicy policy = new RandomRolloutPolicy();
        MCTSEngine engine = new MCTSEngine(policy, MCTS_ITERATIONS);

        int wins = 0, draws = 0, losses = 0;
        long totalMs = 0;

        for (int g = 0; g < GAMES_PER_CONFIG; g++) {
            long start = System.nanoTime();
            int result = playGame(new TicTacToeState(), engine, 1);
            totalMs += (System.nanoTime() - start) / 1_000_000;

            if (result == 1) wins++;
            else if (result == 0) draws++;
            else losses++;
            engine.reset();
        }

        System.out.printf("  Games: %d | Wins: %d | Draws: %d | Losses: %d%n",
                GAMES_PER_CONFIG, wins, draws, losses);
        System.out.printf("  Avg time/game: %d ms%n", totalMs / GAMES_PER_CONFIG);
    }

    /* ------------------------------------------------------------------ */
    /*  Reversi: depth sweep                                               */
    /* ------------------------------------------------------------------ */

    private static void benchmarkReversiDepths() {
        System.out.println("--- Reversi: Depth-Limited Simulation Sweep ---");
        System.out.println("  (MCTS vs. Random, " + GAMES_PER_CONFIG + " games each)\n");
        System.out.printf("  %-12s  %-6s  %-6s  %-6s  %-10s%n",
                "Policy", "Wins", "Draws", "Losses", "Avg ms/game");
        System.out.println("  " + "-".repeat(50));

        ReversiHeuristic heuristic = new ReversiHeuristic();

        // Full rollout baseline
        runReversiConfig("Full rollout", new RandomRolloutPolicy());

        // Depth-limited at various depths
        for (int depth : new int[]{18, 16, 14, 12, 10, 8}) {
            runReversiConfig("Depth " + depth,
                    new DepthLimitedPolicy(depth, heuristic));
        }
    }

    private static void runReversiConfig(String label, SimulationPolicy policy) {
        MCTSEngine engine = new MCTSEngine(policy, MCTS_ITERATIONS);
        int wins = 0, draws = 0, losses = 0;
        long totalMs = 0;

        for (int g = 0; g < GAMES_PER_CONFIG; g++) {
            long start = System.nanoTime();
            int result = playGame(new ReversiState(), engine, 1);
            totalMs += (System.nanoTime() - start) / 1_000_000;

            if (result == 1) wins++;
            else if (result == 0) draws++;
            else losses++;
            engine.reset();
        }

        System.out.printf("  %-12s  %-6d  %-6d  %-6d  %-10d%n",
                label, wins, draws, losses, totalMs / GAMES_PER_CONFIG);
    }

    /* ------------------------------------------------------------------ */
    /*  Reversi: exploration constant sweep                                */
    /* ------------------------------------------------------------------ */

    private static void benchmarkExplorationConstants() {
        System.out.println("--- Reversi: Exploration Constant Tuning ---");
        System.out.println("  (MCTS vs. Random, " + GAMES_PER_CONFIG + " games each)\n");
        System.out.printf("  %-8s  %-6s  %-6s  %-6s  %-10s%n",
                "C", "Wins", "Draws", "Losses", "Avg ms/game");
        System.out.println("  " + "-".repeat(46));

        ReversiHeuristic heuristic = new ReversiHeuristic();
        DepthLimitedPolicy policy = new DepthLimitedPolicy(12, heuristic);

        for (double c : new double[]{0.5, 1.0, Math.sqrt(2), 2.0, 3.0}) {
            MCTSEngine engine = new MCTSEngine(policy, MCTS_ITERATIONS, c);
            int wins = 0, draws = 0, losses = 0;
            long totalMs = 0;

            for (int g = 0; g < GAMES_PER_CONFIG; g++) {
                long start = System.nanoTime();
                int result = playGame(new ReversiState(), engine, 1);
                totalMs += (System.nanoTime() - start) / 1_000_000;

                if (result == 1) wins++;
                else if (result == 0) draws++;
                else losses++;
                engine.reset();
            }

            System.out.printf("  %-8.3f  %-6d  %-6d  %-6d  %-10d%n",
                    c, wins, draws, losses, totalMs / GAMES_PER_CONFIG);
        }
    }

    /* ------------------------------------------------------------------ */
    /*  Game driver                                                        */
    /* ------------------------------------------------------------------ */

    /**
     * Plays a complete game: the MCTS engine controls {@code mctsPlayer},
     * the opponent plays uniformly at random.
     *
     * @return the winner (1 or 2), or 0 for a draw
     */
    private static int playGame(GameState state, MCTSEngine engine, int mctsPlayer) {
        while (!state.isTerminal()) {
            List<Integer> moves = state.getLegalMoves();
            int move;

            if (state.getCurrentPlayer() == mctsPlayer) {
                move = engine.bestMove(state);
                engine.advanceTree(move);
            } else {
                move = moves.get(RNG.nextInt(moves.size()));
                engine.advanceTree(move); // keep subtree in sync
            }
            state = state.applyMove(move);
        }
        return state.getWinner();
    }
}
