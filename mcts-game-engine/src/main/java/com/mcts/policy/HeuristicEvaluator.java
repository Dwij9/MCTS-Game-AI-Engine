package com.mcts.policy;

import com.mcts.game.GameState;

/**
 * Evaluates a non-terminal game state and returns a score in [0, 1]
 * from the perspective of a given player. Used by {@link DepthLimitedPolicy}
 * when a simulation reaches the depth ceiling without a terminal outcome.
 */
public interface HeuristicEvaluator {

    /**
     * @param state  the game state to evaluate (may be non-terminal)
     * @param player the player whose perspective to score from (1 or 2)
     * @return a value in [0.0, 1.0]: 1.0 = best for {@code player},
     *         0.0 = worst, 0.5 = neutral
     */
    double evaluate(GameState state, int player);
}
