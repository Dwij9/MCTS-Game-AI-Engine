MCTS GAME AI ENGINE
====================

A Monte Carlo Tree Search engine implemented in Java, featuring UCT-based
selection, subtree reuse, and a depth-limited heuristic simulation policy.
Applied to Tic-Tac-Toe and 8x8 Reversi (Othello).


BUILD & RUN
-----------

Requires Java 17+ and Maven 3.8+.

  mvn clean compile                                             # compile
  mvn test                                                      # run tests
  mvn exec:java -Dexec.mainClass="com.mcts.Main"               # demo
  mvn exec:java -Dexec.mainClass="com.mcts.benchmark.Benchmark" # benchmarks
  mvn package                                                   # package jar
  java -jar target/mcts-game-engine-1.0.0.jar                  # run jar


ARCHITECTURE
------------

  com.mcts
  |-- engine
  |   |-- MCTSEngine.java           Core search loop + subtree advancement
  |   +-- Node.java                 Tree node with UCT selection & backpropagation
  |
  |-- game
  |   |-- GameState.java            Abstract game-state interface
  |   |-- tictactoe
  |   |   +-- TicTacToeState.java
  |   +-- reversi
  |       |-- ReversiState.java     8x8 board with full flip logic
  |       +-- ReversiHeuristic.java
  |
  |-- policy
  |   |-- SimulationPolicy.java     Rollout strategy interface
  |   |-- HeuristicEvaluator.java
  |   |-- RandomRolloutPolicy.java
  |   +-- DepthLimitedPolicy.java
  |
  |-- benchmark
  |   +-- Benchmark.java            Depth sweep + exploration constant tuning
  |
  +-- Main.java                     Interactive demo


KEY FEATURES
------------

MCTS Core Loop

  Each iteration performs four steps:

  1. Selection        Descend the tree via the UCT formula, balancing
                      exploitation (win rate) against exploration (visit count):

                        UCT(i) = w_i / n_i  +  C * sqrt( ln(N) / n_i )

  2. Expansion        Add one unexplored child node.

  3. Simulation       Play out from the expanded node using a pluggable
                      SimulationPolicy.

  4. Backpropagation  Walk back to the root, updating visit and win counts
                      at every node.

  The best move is chosen by robust child selection (highest visit count).


Subtree Reuse

  After each move, advanceTree(move) re-roots the search tree at the child
  corresponding to the played move. All accumulated visit and win statistics
  persist across turns, eliminating the overhead of rebuilding the tree from
  scratch.


Depth-Limited Simulation with Heuristic Evaluator

  DepthLimitedPolicy caps simulation depth and falls back to a
  HeuristicEvaluator for early position scoring when the game hasn't
  terminated. The Reversi heuristic weights positional strength:

    Position     Base    Bonus    Total Weight
    --------     ----    -----    ------------
    Corner       1.0     +2.0     3.0
    Edge         1.0     +0.5     1.5
    Interior     1.0       --     1.0

  Benchmarking depths 8-18 shows depth 8 cuts per-game runtime by ~21%
  while preserving near-optimal move selection against a random opponent.


TESTS
-----

The test suite (mvn test) covers:

  MCTSEngineTest          Verifies that MCTS picks winning moves, blocks
                          opponent threats, and that subtree advancement
                          preserves visit statistics.

  TicTacToeStateTest      Move legality, row/column/diagonal win detection,
                          draw detection, state immutability.

  ReversiStateTest        Initial setup, legal-move generation, disc-flipping
                          mechanics, pass handling, terminal detection.

  DepthLimitedPolicyTest  Score validity across depths 8-18, faster execution
                          at shallower depths, heuristic symmetry and
                          complementarity.


LICENSE
-------

MIT
