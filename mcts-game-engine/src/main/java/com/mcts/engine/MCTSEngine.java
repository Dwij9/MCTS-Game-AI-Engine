package com.mcts.engine;

import com.mcts.game.GameState;
import com.mcts.policy.SimulationPolicy;

/**
 * Monte Carlo Tree Search engine.
 *
 * <p>Runs the four-phase MCTS loop — selection, expansion, simulation,
 * backpropagation — for a configurable number of iterations and returns
 * the most-visited (most robust) child move.</p>
 *
 * <p><b>Subtree advancement.</b> After a move is played on the board,
 * call {@link #advanceTree(int)} to re-root the search tree at the
 * corresponding child node. All visit and win statistics accumulated
 * during prior searches are preserved, eliminating the cost of
 * rebuilding the tree from scratch each turn.</p>
 */
public class MCTSEngine {

    private static final double DEFAULT_EXPLORATION = Math.sqrt(2);

    private double explorationConstant;
    private SimulationPolicy policy;
    private Node root;
    private int iterations;

    /**
     * @param policy              simulation (rollout) strategy
     * @param iterations          number of MCTS iterations per {@link #bestMove}
     * @param explorationConstant C in the UCT formula
     */
    public MCTSEngine(SimulationPolicy policy, int iterations, double explorationConstant) {
        this.policy = policy;
        this.iterations = iterations;
        this.explorationConstant = explorationConstant;
        this.root = null;
    }

    /** Uses √2 as the default exploration constant. */
    public MCTSEngine(SimulationPolicy policy, int iterations) {
        this(policy, iterations, DEFAULT_EXPLORATION);
    }

    /* ------------------------------------------------------------------ */
    /*  Core search                                                        */
    /* ------------------------------------------------------------------ */

    /**
     * Runs MCTS from the given state and returns the best move.
     *
     * <p>If a search tree already exists (from a prior call + {@link #advanceTree}),
     * the engine continues building on it. Otherwise a fresh root is created.</p>
     *
     * @param state the current game state
     * @return the move code of the best action, or -1 if no moves are available
     */
    public int bestMove(GameState state) {
        // Initialise or reuse the root
        if (root == null) {
            root = new Node(state, -1, null, 0);
        }

        for (int i = 0; i < iterations; i++) {
            // 1. Selection — descend via UCT to a promising leaf
            Node selected = root.select(explorationConstant);

            // 2. Expansion — add one child if the leaf is non-terminal
            Node expanded = selected;
            if (!selected.getState().isTerminal() && !selected.isFullyExpanded()) {
                expanded = selected.expand();
            }

            // 3. Simulation — play out from the expanded node
            int perspectivePlayer = expanded.getPlayerJustMoved();
            if (perspectivePlayer == 0) {
                // Root node: perspective is the current player's opponent
                // (since root has no "player just moved", we pick the opponent
                //  so that children — which DO have a playerJustMoved — align)
                perspectivePlayer = state.getCurrentPlayer();
            }
            double score = policy.simulate(expanded.getState(), perspectivePlayer);

            // 4. Backpropagation — update statistics up the tree
            expanded.backpropagate(score, perspectivePlayer);
        }

        // Choose the most-visited child (robust child selection)
        return mostVisitedChildMove();
    }

    /**
     * Returns the move of the child with the highest visit count.
     * Ties are broken arbitrarily (first encountered).
     */
    private int mostVisitedChildMove() {
        Node bestChild = null;
        int bestVisits = -1;
        for (Node child : root.getChildren()) {
            if (child.getVisits() > bestVisits) {
                bestVisits = child.getVisits();
                bestChild = child;
            }
        }
        return bestChild != null ? bestChild.getMoveFromParent() : -1;
    }

    /* ------------------------------------------------------------------ */
    /*  Subtree advancement                                                */
    /* ------------------------------------------------------------------ */

    /**
     * Re-roots the search tree at the child corresponding to {@code move}.
     *
     * <p>If the move was previously expanded, the subtree beneath it —
     * including all accumulated visit and win statistics — is preserved.
     * The old root and its other branches become eligible for GC.
     * If the move was never expanded, the tree is discarded.</p>
     *
     * @param move the move that was actually played
     */
    public void advanceTree(int move) {
        if (root == null) return;

        Node child = root.getChildForMove(move);
        if (child != null) {
            child.detachFromParent();
            root = child;
        } else {
            root = null;
        }
    }

    /** Discards the entire search tree. */
    public void reset() {
        root = null;
    }

    /* ------------------------------------------------------------------ */
    /*  Accessors / mutators                                               */
    /* ------------------------------------------------------------------ */

    public double getExplorationConstant() {
        return explorationConstant;
    }

    public void setExplorationConstant(double c) {
        this.explorationConstant = c;
    }

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int n) {
        this.iterations = n;
    }

    public SimulationPolicy getPolicy() {
        return policy;
    }

    public void setPolicy(SimulationPolicy policy) {
        this.policy = policy;
    }

    public Node getRoot() {
        return root;
    }
}
