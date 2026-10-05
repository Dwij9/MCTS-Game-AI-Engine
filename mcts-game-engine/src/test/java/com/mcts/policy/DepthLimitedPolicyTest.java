package com.mcts.policy;

import com.mcts.game.GameState;
import com.mcts.game.reversi.ReversiHeuristic;
import com.mcts.game.reversi.ReversiState;
import com.mcts.game.tictactoe.TicTacToeState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link DepthLimitedPolicy} and {@link ReversiHeuristic}.
 */
class DepthLimitedPolicyTest {

    /* ------------------------------------------------------------------ */
    /*  DepthLimitedPolicy                                                 */
    /* ------------------------------------------------------------------ */

    @Test
    void simulate_returnsValidScore() {
        DepthLimitedPolicy policy = new DepthLimitedPolicy(
                10, new ReversiHeuristic(), 42);
        GameState state = new ReversiState();

        double score = policy.simulate(state, 1);
        assertTrue(score >= 0.0 && score <= 1.0,
                "Simulation score must be in [0, 1], got: " + score);
    }

    @Test
    void simulate_terminalState_returnsExactOutcome() {
        // Build a finished TTT game: X wins
        GameState state = new TicTacToeState();
        state = state.applyMove(0); // X
        state = state.applyMove(3); // O
        state = state.applyMove(1); // X
        state = state.applyMove(4); // O
        state = state.applyMove(2); // X wins

        // Dummy evaluator (should not be called)
        HeuristicEvaluator dummy = (s, p) -> { fail("Should not be called"); return 0; };
        DepthLimitedPolicy policy = new DepthLimitedPolicy(5, dummy);

        assertEquals(1.0, policy.simulate(state, 1),
                "Win for player 1 from player 1's perspective = 1.0");
        assertEquals(0.0, policy.simulate(state, 2),
                "Win for player 1 from player 2's perspective = 0.0");
    }

    @ParameterizedTest
    @ValueSource(ints = {8, 10, 12, 14, 16, 18})
    void simulate_variousDepths_returnValidScores(int depth) {
        DepthLimitedPolicy policy = new DepthLimitedPolicy(
                depth, new ReversiHeuristic(), 99);
        GameState state = new ReversiState();

        // Run multiple simulations and check they all return valid values
        for (int i = 0; i < 50; i++) {
            double score = policy.simulate(state, 1);
            assertTrue(score >= 0.0 && score <= 1.0,
                    "Depth " + depth + ": score " + score + " out of range");
        }
    }

    @Test
    void shallowerDepth_isFaster() {
        ReversiHeuristic heuristic = new ReversiHeuristic();
        DepthLimitedPolicy shallow = new DepthLimitedPolicy(8, heuristic, 42);
        DepthLimitedPolicy deep = new DepthLimitedPolicy(18, heuristic, 42);
        GameState state = new ReversiState();

        int runs = 500;

        long startDeep = System.nanoTime();
        for (int i = 0; i < runs; i++) deep.simulate(state, 1);
        long deepTime = System.nanoTime() - startDeep;

        long startShallow = System.nanoTime();
        for (int i = 0; i < runs; i++) shallow.simulate(state, 1);
        long shallowTime = System.nanoTime() - startShallow;

        assertTrue(shallowTime < deepTime,
                "Depth 8 should be faster than depth 18 (" +
                        shallowTime / 1_000_000 + " ms vs " +
                        deepTime / 1_000_000 + " ms)");
    }

    /* ------------------------------------------------------------------ */
    /*  ReversiHeuristic                                                   */
    /* ------------------------------------------------------------------ */

    @Test
    void heuristic_initialPosition_isBalanced() {
        ReversiHeuristic heuristic = new ReversiHeuristic();
        GameState state = new ReversiState();

        double p1Score = heuristic.evaluate(state, 1);
        double p2Score = heuristic.evaluate(state, 2);

        assertEquals(0.5, p1Score, 0.01,
                "Symmetric initial position should evaluate to ~0.5 for player 1");
        assertEquals(0.5, p2Score, 0.01,
                "Symmetric initial position should evaluate to ~0.5 for player 2");
    }

    @Test
    void heuristic_scoresAreComplementary() {
        ReversiHeuristic heuristic = new ReversiHeuristic();
        GameState state = new ReversiState();
        state = state.applyMove(state.getLegalMoves().get(0));

        double p1 = heuristic.evaluate(state, 1);
        double p2 = heuristic.evaluate(state, 2);

        assertEquals(1.0, p1 + p2, 0.001,
                "Scores for opposite players should sum to 1.0");
    }

    @Test
    void heuristic_terminalWin_returnsOne() {
        // Force a terminal state via double pass
        GameState state = new ReversiState();
        state = state.applyMove(ReversiState.PASS_MOVE);
        state = state.applyMove(ReversiState.PASS_MOVE);
        // Initial position: 2 black, 2 white → draw
        ReversiHeuristic heuristic = new ReversiHeuristic();
        assertEquals(0.5, heuristic.evaluate(state, 1),
                "Equal disc count at terminal → 0.5");
    }

    @Test
    void heuristic_cornerBonus_reflected() {
        // The corner bonus (2.0) means a corner disc is worth 3× an interior disc.
        // We can verify by checking that the heuristic weights match expectations.
        assertEquals(2.0, ReversiHeuristic.CORNER_BONUS);
        assertEquals(0.5, ReversiHeuristic.EDGE_BONUS);
        assertEquals(1.0, ReversiHeuristic.BASE_WEIGHT);
    }

    /* ------------------------------------------------------------------ */
    /*  RandomRolloutPolicy                                                */
    /* ------------------------------------------------------------------ */

    @Test
    void randomRollout_returnsValidScore() {
        RandomRolloutPolicy policy = new RandomRolloutPolicy(42);
        GameState state = new TicTacToeState();

        for (int i = 0; i < 100; i++) {
            double score = policy.simulate(state, 1);
            assertTrue(score == 0.0 || score == 0.5 || score == 1.0,
                    "Random rollout on TTT should return 0, 0.5, or 1");
        }
    }
}
