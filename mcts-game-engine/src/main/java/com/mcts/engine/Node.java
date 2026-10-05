package com.mcts.engine;

import com.mcts.game.GameState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A single node in the MCTS search tree.
 *
 * <p>Each node stores a game state, the move that produced it, visit and win
 * statistics, and links to its parent and children. The win counter tracks
 * results from the perspective of {@code playerJustMoved} — the player who
 * made the move leading into this node — so that the parent's UCT selection
 * directly maximises the win rate of its own moves.</p>
 */
public class Node {

    private final GameState state;
    private final int moveFromParent;
    private Node parent;
    private final int playerJustMoved;

    private final List<Integer> untriedMoves;
    private final List<Node> children;

    private int visits;
    private double wins;

    /**
     * @param state           game state at this node
     * @param moveFromParent  the move the parent played to reach here (-1 for root)
     * @param parent          parent node (null for root)
     * @param playerJustMoved the player who played {@code moveFromParent} (0 for root)
     */
    public Node(GameState state, int moveFromParent, Node parent, int playerJustMoved) {
        this.state = state;
        this.moveFromParent = moveFromParent;
        this.parent = parent;
        this.playerJustMoved = playerJustMoved;
        this.children = new ArrayList<>();
        this.untriedMoves = new ArrayList<>(state.isTerminal()
                ? Collections.emptyList()
                : state.getLegalMoves());
        this.visits = 0;
        this.wins = 0.0;
    }

    /* ------------------------------------------------------------------ */
    /*  MCTS phases                                                        */
    /* ------------------------------------------------------------------ */

    /**
     * <b>Selection.</b> Descend the tree using UCT until reaching a node that
     * is either terminal or has untried moves remaining.
     *
     * @param explorationConstant the C parameter in the UCT formula
     * @return the selected leaf node
     */
    public Node select(double explorationConstant) {
        Node current = this;
        while (!current.state.isTerminal() && current.isFullyExpanded()) {
            current = current.bestChild(explorationConstant);
        }
        return current;
    }

    /**
     * <b>Expansion.</b> Picks one untried move, creates a child node for it,
     * and returns that child. If no untried moves remain, returns {@code this}.
     *
     * @return the newly created child, or this node if already fully expanded
     */
    public Node expand() {
        if (untriedMoves.isEmpty()) {
            return this;
        }
        int move = untriedMoves.remove(untriedMoves.size() - 1);
        GameState childState = state.applyMove(move);
        Node child = new Node(childState, move, this, state.getCurrentPlayer());
        children.add(child);
        return child;
    }

    /**
     * <b>Backpropagation.</b> Walks from this node to the root, updating
     * visit counts and win tallies. The {@code score} is interpreted from
     * the perspective of {@code perspectivePlayer}: nodes whose
     * {@code playerJustMoved} matches the perspective player add the score;
     * others add the complement (1 − score).
     *
     * @param score             result of the simulation in [0, 1]
     * @param perspectivePlayer the player for whom score == 1.0 means a win
     */
    public void backpropagate(double score, int perspectivePlayer) {
        Node current = this;
        while (current != null) {
            current.visits++;
            if (current.playerJustMoved == perspectivePlayer) {
                current.wins += score;
            } else if (current.playerJustMoved != 0) {
                current.wins += 1.0 - score;
            }
            current = current.parent;
        }
    }

    /* ------------------------------------------------------------------ */
    /*  UCT child selection                                                */
    /* ------------------------------------------------------------------ */

    /**
     * Returns the child that maximises the UCT value:
     * <pre>
     *   UCT = w_i / n_i  +  C · √( ln(N) / n_i )
     * </pre>
     * where w_i and n_i are the child's wins and visits, N is this node's
     * visits, and C is the exploration constant.
     *
     * @param explorationConstant C in the UCT formula
     * @return the best child according to UCT
     */
    public Node bestChild(double explorationConstant) {
        Node best = null;
        double bestValue = Double.NEGATIVE_INFINITY;
        double logParentVisits = Math.log(this.visits);

        for (Node child : children) {
            double exploitation = child.wins / child.visits;
            double exploration = explorationConstant
                    * Math.sqrt(logParentVisits / child.visits);
            double uct = exploitation + exploration;
            if (uct > bestValue) {
                bestValue = uct;
                best = child;
            }
        }
        return best;
    }

    /* ------------------------------------------------------------------ */
    /*  Subtree reuse helpers                                              */
    /* ------------------------------------------------------------------ */

    /**
     * Finds the child node corresponding to a specific move.
     *
     * @param move the move code to look up
     * @return the matching child, or null if the move was never expanded
     */
    public Node getChildForMove(int move) {
        for (Node child : children) {
            if (child.moveFromParent == move) {
                return child;
            }
        }
        return null;
    }

    /**
     * Detaches this node from its parent so the rest of the old tree
     * can be garbage-collected after subtree advancement.
     */
    public void detachFromParent() {
        this.parent = null;
    }

    /* ------------------------------------------------------------------ */
    /*  Accessors                                                          */
    /* ------------------------------------------------------------------ */

    public boolean isFullyExpanded() {
        return untriedMoves.isEmpty();
    }

    public GameState getState() {
        return state;
    }

    public int getMoveFromParent() {
        return moveFromParent;
    }

    public Node getParent() {
        return parent;
    }

    public List<Node> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public int getVisits() {
        return visits;
    }

    public double getWins() {
        return wins;
    }

    public int getPlayerJustMoved() {
        return playerJustMoved;
    }

    public double getWinRate() {
        return visits == 0 ? 0.0 : wins / visits;
    }
}
