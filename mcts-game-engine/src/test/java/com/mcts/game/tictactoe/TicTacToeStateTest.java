package com.mcts.game.tictactoe;

import com.mcts.game.GameState;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link TicTacToeState}: move legality, win/draw detection,
 * and state immutability.
 */
class TicTacToeStateTest {

    @Test
    void initialState_hasNineLegalMoves() {
        GameState state = new TicTacToeState();
        assertEquals(9, state.getLegalMoves().size());
    }

    @Test
    void initialState_player1MovesFirst() {
        GameState state = new TicTacToeState();
        assertEquals(1, state.getCurrentPlayer());
    }

    @Test
    void applyMove_switchesPlayer() {
        GameState state = new TicTacToeState();
        state = state.applyMove(0);
        assertEquals(2, state.getCurrentPlayer());
    }

    @Test
    void applyMove_reducesLegalMoves() {
        GameState state = new TicTacToeState();
        state = state.applyMove(4); // centre
        List<Integer> moves = state.getLegalMoves();
        assertEquals(8, moves.size());
        assertFalse(moves.contains(4));
    }

    @Test
    void applyMove_doesNotMutateOriginal() {
        GameState original = new TicTacToeState();
        GameState after = original.applyMove(0);
        assertEquals(9, original.getLegalMoves().size(),
                "Original state must not be mutated");
        assertEquals(8, after.getLegalMoves().size());
    }

    @Test
    void rowWin_detected() {
        // X X X
        // O O .
        // . . .
        GameState s = new TicTacToeState();
        s = s.applyMove(0); // X
        s = s.applyMove(3); // O
        s = s.applyMove(1); // X
        s = s.applyMove(4); // O
        s = s.applyMove(2); // X wins top row
        assertTrue(s.isTerminal());
        assertEquals(1, s.getWinner());
    }

    @Test
    void columnWin_detected() {
        // X O .
        // X O .
        // X . .
        GameState s = new TicTacToeState();
        s = s.applyMove(0); // X
        s = s.applyMove(1); // O
        s = s.applyMove(3); // X
        s = s.applyMove(4); // O
        s = s.applyMove(6); // X wins left column
        assertTrue(s.isTerminal());
        assertEquals(1, s.getWinner());
    }

    @Test
    void diagonalWin_detected() {
        // X O .
        // . X O
        // . . X
        GameState s = new TicTacToeState();
        s = s.applyMove(0); // X
        s = s.applyMove(1); // O
        s = s.applyMove(4); // X
        s = s.applyMove(5); // O
        s = s.applyMove(8); // X wins main diagonal
        assertTrue(s.isTerminal());
        assertEquals(1, s.getWinner());
    }

    @Test
    void draw_detected() {
        // X O X
        // X O O
        // O X X
        GameState s = new TicTacToeState();
        s = s.applyMove(0); // X
        s = s.applyMove(1); // O
        s = s.applyMove(2); // X
        s = s.applyMove(4); // O
        s = s.applyMove(3); // X
        s = s.applyMove(5); // O
        s = s.applyMove(7); // X
        s = s.applyMove(6); // O
        s = s.applyMove(8); // X
        assertTrue(s.isTerminal());
        assertEquals(0, s.getWinner(), "Should be a draw");
    }

    @Test
    void ongoingGame_notTerminal() {
        GameState s = new TicTacToeState();
        s = s.applyMove(4);
        assertFalse(s.isTerminal());
        assertEquals(-1, s.getWinner());
    }

    @Test
    void copy_isIndependent() {
        GameState original = new TicTacToeState();
        original = original.applyMove(4);
        GameState copied = original.copy();

        GameState modified = copied.applyMove(0);
        assertEquals(7, modified.getLegalMoves().size());
        assertEquals(8, original.getLegalMoves().size(),
                "Modifying a copy's descendant must not affect the original");
    }

    @Test
    void moveToString_formats() {
        GameState s = new TicTacToeState();
        assertEquals("(0,0)", s.moveToString(0));
        assertEquals("(1,1)", s.moveToString(4));
        assertEquals("(2,2)", s.moveToString(8));
    }
}
