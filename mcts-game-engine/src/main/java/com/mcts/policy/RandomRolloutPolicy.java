package com.mcts.policy;

import com.mcts.game.GameState;

import java.util.List;
import java.util.Random;

/**
 * Full random rollout: plays uniformly random moves until a terminal
 * state is reached, then returns the actual outcome.
 */
public class RandomRolloutPolicy implements SimulationPolicy {

    private final Random rng;

    public RandomRolloutPolicy() {
        this.rng = new Random();
    }

    public RandomRolloutPolicy(long seed) {
        this.rng = new Random(seed);
    }

    @Override
    public double simulate(GameState state, int perspectivePlayer) {
        GameState sim = state.copy();
        while (!sim.isTerminal()) {
            List<Integer> moves = sim.getLegalMoves();
            sim = sim.applyMove(moves.get(rng.nextInt(moves.size())));
        }
        int winner = sim.getWinner();
        if (winner == 0) return 0.5;
        return winner == perspectivePlayer ? 1.0 : 0.0;
    }
}
