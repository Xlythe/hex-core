package com.hex.core;

/** Standalone regression suite. Compare the live win check with exhaustive simple paths. */
public final class WinningChainTest {
    private static final int NO_PATH = 10;

    private static boolean adjacent(int a, int b) {
        int dx = a % 3 - b % 3, dy = a / 3 - b / 3;
        return Math.max(Math.max(Math.abs(dx), Math.abs(dy)), Math.abs(dx + dy)) == 1;
    }

    private static int distance(int mask, int cell, int visited, int team) {
        int axis = team == 1 ? cell % 3 : cell / 3;
        if (axis == 2) return 1;
        int shortest = NO_PATH;
        visited |= 1 << cell;
        for (int next = 0; next < 9; next++) {
            if ((mask & ~visited & (1 << next)) != 0 && adjacent(cell, next)) {
                shortest = Math.min(shortest, 1 + distance(mask, next, visited, team));
            }
        }
        return shortest;
    }

    private static int shortest(int mask, int team) {
        int best = NO_PATH;
        for (int i = 0; i < 3; i++) {
            int cell = team == 1 ? 3 * i : i;
            if ((mask & (1 << cell)) != 0) best = Math.min(best, distance(mask, cell, 0, team));
        }
        return best;
    }

    private static Game game(int size) {
        Game.GameOptions options = new Game.GameOptions();
        options.gridSize = size;
        options.swap = true;
        options.timer = new Timer(0, 0, Timer.NO_TIMER);
        return new Game(options, new PlayerObject(1), new PlayerObject(2));
    }

    public static void main(String[] args) {
        int[][] lengths = new int[3][512];
        for (int mask = 0; mask < 512; mask++) {
            for (int side = 1; side <= 2; side++) lengths[side][mask] = shortest(mask, side);
        }
        Game game = game(3);
        for (int state = 0; state < 19683; state++) {
            int code = state;
            int[] masks = new int[3];
            for (int i = 0; i < 9; i++, code /= 3) {
                int side = code % 3;
                masks[side] |= 1 << i;
                game.gamePieces[i % 3][i / 3].setTeam((byte) side, game);
            }
            int winners = 0;
            for (int side = 1; side <= 2; side++) {
                GameAction.winFlagReset(game);
                GameAction.checkedFlagReset(game);
                boolean won = GameAction.checkWinPlayer(side, game);
                int expected = lengths[side][masks[side]];
                require(won == (expected < NO_PATH), "wrong winner", state);
                if (won) winners++;
                int marked = 0;
                for (int i = 0; i < 9; i++)
                    if (game.gamePieces[i % 3][i / 3].isWinningPath()) marked |= 1 << i;
                require((marked & ~masks[side]) == 0, "highlight includes opponent", state);
                require(Integer.bitCount(marked) == (won ? expected : 0), "wrong highlight length", state);
                require(lengths[side][marked] == expected, "disconnected highlight", state);
            }
            require(winners <= 1 && (masks[0] != 0 || winners == 1), "full-board/no-draw rule", state);
        }
        Game turns = game(3);
        require(!GameAction.makeMove(turns.getPlayer2(), new Point(0, 0), turns), "second moved first", 0);
        require(GameAction.makeMove(turns.getPlayer1(), new Point(1, 0), turns), "opening rejected", 0);
        require(!GameAction.makeMove(turns.getPlayer1(), new Point(1, 0), turns), "same side swapped", 0);
        require(GameAction.makeMove(turns.getPlayer2(), new Point(1, 0), turns), "swap rejected", 0);
        require(turns.gamePieces[0][1].getTeam() == 2 && turns.gamePieces[1][0].getTeam() == 0, "wrong transpose", 0);
        Game finish = game(3);
        int[][] moves = {{0,2}, {0,0}, {2,0}, {1,0}, {2,1}, {0,1}, {1,2}};
        for (int i = 0; i < moves.length; i++) {
            PlayingEntity player = i % 2 == 0 ? finish.getPlayer1() : finish.getPlayer2();
            require(GameAction.makeMove(player, new Point(moves[i][0], moves[i][1]), finish), "legal move rejected", i);
        }
        require(!GameAction.makeMove(finish.getPlayer2(), new Point(1,1), finish), "move after win", 0);
        finish.clearBoard();
        new Replay(0, finish).run();
        require(finish.gamePieces[0][2].isWinningPath() && finish.gamePieces[1][2].isWinningPath()
                && finish.gamePieces[2][1].isWinningPath() && !finish.gamePieces[2][0].isWinningPath(),
                "replay did not highlight the shortest finish", 0);
        for (int size : new int[]{4, 11, 19, 30}) {
            Game dense = game(size);
            for (GamePiece[] column : dense.gamePieces) {
                for (GamePiece cell : column) cell.setTeam((byte) 2, dense);
            }
            require(GameAction.checkWinPlayer(2, dense), "dense board winner", size);
            int marked = 0;
            for (GamePiece[] column : dense.gamePieces) {
                for (GamePiece cell : column) if (cell.isWinningPath()) marked++;
            }
            require(marked == size, "dense board length", size);
        }
        System.out.println("Verified winners and minimum connected highlights on all 19683 three-by-three boards.");
    }

    private static void require(boolean condition, String error, int state) {
        if (!condition) throw new AssertionError(error + " (state " + state + ")");
    }
}
