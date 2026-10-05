package com.mcts.engine;

import com.mcts.game.GameState;
import com.mcts.game.tictactoe.TicTacToeState;
import com.mcts.policy.RandomRolloutPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link MCTSEngine}: search correctness, subtree reuse, and
 * the quality of selected moves in recognisable board positions.
 */
class MCTSEngineTest {

    private MCTSEngine engine;

    @BeforeEach
    void setUp() {
        engine = new MCTSEngine(new RandomRolloutPolicy(123), 5000);
    }

    @Test
    void bestMove_returnsLegalMove() {
        GameState state = new TicTacToeState();
        int move = engine.bestMove(state);
        assertTrue(state.getLegalMoves().contains(move),
                "MCTS should return a legal move");
    }

    @Test
    void bestMove_blocksOpponentWin() {
        // Board:  X X .
        //         O . .
        //         . . .
        // Player 2 (O) to move — must block at position 2
        GameState state = new TicTacToeState();
        state = state.applyMove(0); // X at (0,0)
        state = state.applyMove(3); // O at (1,0)
        state = state.applyMove(1); // X at (0,1)
        // Now O to move; X threatens (0,2) for the win
        int move = engine.bestMove(state);
        assertEquals(2, move,
                "O must block X's winning threat at (0,2)");
    }

    @Test
    void bestMove_takesWinningMove() {
        // Board:  X X .
        //         O O .
        //         . . .
        // Player 1 (X) to move — should win at position 2
        GameState state = new TicTacToeState();
        state = state.applyMove(0); // X
        state = state.applyMove(3); // O
        state = state.applyMove(1); // X
        state = state.applyMove(4); // O
        // X to move, X has 0,1 — winning move is 2
        int move = engine.bestMove(state);
        assertEquals(2, move,
                "X should take the immediate win at (0,2)");
    }

    @Test
    void subtreeAdvancement_preservesStatistics() {
        GameState state = new TicTacToeState();
        engine.bestMove(state); // build tree

        Node rootBefore = engine.getRoot();
        assertNotNull(rootBefore);
        assertTrue(rootBefore.getVisits() > 0);

        // Pick the most-visited child
        Node bestChild = rootBefore.getChildren().stream()
                .max((a, b) -> Integer.compare(a.getVisits(), b.getVisits()))
                .orElseThrow();
        int move = bestChild.getMoveFromParent();
        int childVisitsBefore = bestChild.getVisits();

        engine.advanceTree(move);

        Node newRoot = engine.getRoot();
        assertNotNull(newRoot, "Root should exist after advancement");
        assertEquals(childVisitsBefore, newRoot.getVisits(),
                "Visit count should be preserved after subtree advancement");
        assertNull(newRoot.getParent(),
                "New root should be detached from old parent");
    }

    @Test
    void subtreeAdvancement_unknownMoveResetsTree() {
        GameState state = new TicTacToeState();
        engine.bestMove(state);

        // Advance with a move that may not have been expanded
        engine.advanceTree(99); // invalid move
        assertNull(engine.getRoot(),
                "Tree should be discarded when advancing to an unexpanded move");
    }

    @Test
    void reset_clearsTree() {
        GameState state = new TicTacToeState();
        engine.bestMove(state);
        assertNotNull(engine.getRoot());

        engine.reset();
        assertNull(engine.getRoot(), "reset() should clear the tree");
    }

    @Test
    void explorationConstant_affectsSearch() {
        // With very low exploration, MCTS should exploit early findings
        MCTSEngine lowC = new MCTSEngine(new RandomRolloutPolicy(42), 500, 0.1);
        MCTSEngine highC = new MCTSEngine(new RandomRolloutPolicy(42), 500, 5.0);

        GameState state = new TicTacToeState();
        lowC.bestMove(state);
        highC.bestMove(state);

        // With high C, children should have more even visit distribution
        Node lowRoot = lowC.getRoot();
        Node highRoot = highC.getRoot();

        int lowMaxVisits = lowRoot.getChildren().stream()
                .mapToInt(Node::getVisits).max().orElse(0);
        int highMaxVisits = highRoot.getChildren().stream()
                .mapToInt(Node::getVisits).max().orElse(0);
        int lowMinVisits = lowRoot.getChildren().stream()
                .mapToInt(Node::getVisits).min().orElse(0);
        int highMinVisits = highRoot.getChildren().stream()
                .mapToInt(Node::getVisits).min().orElse(0);

        double lowSpread = (double) lowMaxVisits / Math.max(lowMinVisits, 1);
        double highSpread = (double) highMaxVisits / Math.max(highMinVisits, 1);

        assertTrue(lowSpread > highSpread,
                "Low exploration constant should produce more uneven visit spread");
    }
}
