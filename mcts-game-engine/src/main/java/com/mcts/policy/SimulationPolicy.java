package com.mcts.policy;

import com.mcts.game.GameState;

/**
 * Strategy interface for the simulation (rollout) phase of MCTS.
 * Implementations decide how to play out a position and return
 * a score from a given player's perspective.
 */
public interface SimulationPolicy {

    /**
     * Simulates play from the given state and returns an outcome score.
     *
     * @param state             the state from which to begin the simulation
     * @param perspectivePlayer the player whose perspective determines the score
     * @return a value in [0.0, 1.0]: 1.0 = win for perspectivePlayer,
     *         0.0 = loss, 0.5 = draw or heuristic neutral
     */
    double simulate(GameState state, int perspectivePlayer);
}
