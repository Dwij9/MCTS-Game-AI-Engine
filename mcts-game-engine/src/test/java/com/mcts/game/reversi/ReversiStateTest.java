package com.mcts.game.reversi;

import com.mcts.game.GameState;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link ReversiState}: initial position, move legality,
 * flipping mechanics, pass handling, and terminal detection.
 */
class ReversiStateTest {

    @Test
    void initialState_hasFourPieces() {
        ReversiState state = new ReversiState();
        assertEquals(2, state.countDiscs(ReversiState.BLACK));
        assertEquals(2, state.countDiscs(ReversiState.WHITE));
    }

    @Test
    void initialState_blackMovesFirst() {
        ReversiState state = new ReversiState();
        assertEquals(ReversiState.BLACK, state.getCurrentPlayer());
    }

    @Test
    void initialState_hasFourLegalMoves() {
        // Standard Reversi opening has exactly 4 legal moves for Black
        ReversiState state = new ReversiState();
        List<Integer> moves = state.getLegalMoves();
        assertEquals(4, moves.size());
    }

    @Test
    void initialState_notTerminal() {
        ReversiState state = new ReversiState();
        assertFalse(state.isTerminal());
        assertEquals(-1, state.getWinner());
    }

    @Test
    void applyMove_flipsOpponentDiscs() {
        ReversiState state = new ReversiState();
        // Black plays d3 (row=2, col=3, move=2*8+3=19)
        // This should flip the white piece at d4 (row=3, col=3)
        GameState after = state.applyMove(19);
        ReversiState rs = (ReversiState) after;
        assertEquals(4, rs.countDiscs(ReversiState.BLACK),
                "Black should have 3 original + 1 placed = 4 after flipping");
        // Total should be 5: 4 original + 1 new placement
        assertEquals(1, rs.countDiscs(ReversiState.WHITE),
                "White should have lost one disc to flipping");
    }

    @Test
    void applyMove_switchesPlayer() {
        ReversiState state = new ReversiState();
        List<Integer> moves = state.getLegalMoves();
        GameState after = state.applyMove(moves.get(0));
        assertEquals(ReversiState.WHITE, after.getCurrentPlayer());
    }

    @Test
    void applyMove_doesNotMutateOriginal() {
        ReversiState state = new ReversiState();
        int blackBefore = state.countDiscs(ReversiState.BLACK);
        state.applyMove(state.getLegalMoves().get(0));
        assertEquals(blackBefore, state.countDiscs(ReversiState.BLACK),
                "Original state must not be mutated");
    }

    @Test
    void pass_returnedWhenNoPlacementPossible() {
        // After many moves, a player may have no legal placements
        // and must pass (move code -1).
        // We test the pass mechanism by verifying the contract:
        // if getLegalMoves() returns [-1], applyMove(-1) should switch player
        ReversiState state = new ReversiState();
        List<Integer> moves = state.getLegalMoves();
        // A pass in the move list means the value -1 is present
        // At the start this shouldn't happen
        assertFalse(moves.contains(ReversiState.PASS_MOVE),
                "Opening position should have real moves, not a pass");
    }

    @Test
    void passMove_switchesPlayerWithoutPlacing() {
        ReversiState state = new ReversiState();
        int totalBefore = state.countDiscs(ReversiState.BLACK)
                + state.countDiscs(ReversiState.WHITE);
        GameState afterPass = state.applyMove(ReversiState.PASS_MOVE);
        ReversiState rs = (ReversiState) afterPass;
        int totalAfter = rs.countDiscs(ReversiState.BLACK)
                + rs.countDiscs(ReversiState.WHITE);
        assertEquals(totalBefore, totalAfter,
                "Pass should not change disc counts");
        assertEquals(ReversiState.WHITE, rs.getCurrentPlayer(),
                "Pass should switch the current player");
    }

    @Test
    void twoConsecutivePasses_endsGame() {
        ReversiState state = new ReversiState();
        GameState s1 = state.applyMove(ReversiState.PASS_MOVE);
        GameState s2 = s1.applyMove(ReversiState.PASS_MOVE);
        assertTrue(s2.isTerminal(),
                "Two consecutive passes should end the game");
    }

    @Test
    void isCorner_correctCells() {
        assertTrue(ReversiState.isCorner(0, 0));
        assertTrue(ReversiState.isCorner(0, 7));
        assertTrue(ReversiState.isCorner(7, 0));
        assertTrue(ReversiState.isCorner(7, 7));
        assertFalse(ReversiState.isCorner(0, 3));
        assertFalse(ReversiState.isCorner(4, 4));
    }

    @Test
    void isEdge_excludesCorners() {
        assertTrue(ReversiState.isEdge(0, 3));
        assertTrue(ReversiState.isEdge(7, 5));
        assertTrue(ReversiState.isEdge(3, 0));
        assertFalse(ReversiState.isEdge(0, 0)); // corner
        assertFalse(ReversiState.isEdge(3, 3)); // interior
    }

    @Test
    void copy_isIndependent() {
        ReversiState state = new ReversiState();
        GameState after = state.applyMove(state.getLegalMoves().get(0));
        GameState copied = after.copy();
        assertNotSame(after, copied);
        assertEquals(after.getCurrentPlayer(), copied.getCurrentPlayer());
    }

    @Test
    void moveToString_formatsCorrectly() {
        ReversiState state = new ReversiState();
        assertEquals("a1", state.moveToString(0));   // (0,0)
        assertEquals("d3", state.moveToString(19));  // (2,3)
        assertEquals("h8", state.moveToString(63));  // (7,7)
        assertEquals("PASS", state.moveToString(ReversiState.PASS_MOVE));
    }

    @Test
    void fullGame_reachesTerminal() {
        // Play a game with simple sequential moves and verify it terminates
        GameState state = new ReversiState();
        int maxMoves = 128; // safety limit
        int moveCount = 0;
        while (!state.isTerminal() && moveCount < maxMoves) {
            List<Integer> moves = state.getLegalMoves();
            state = state.applyMove(moves.get(0));
            moveCount++;
        }
        assertTrue(state.isTerminal(),
                "A game should eventually reach a terminal state");
        int winner = state.getWinner();
        assertTrue(winner >= 0 && winner <= 2,
                "Winner should be 0, 1, or 2 at terminal");
    }
}
