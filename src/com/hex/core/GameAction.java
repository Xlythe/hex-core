package com.hex.core;

public class GameAction {
    public static int LOCAL_GAME = 1;
    public static int NET_GAME = 2;

    private GameAction() {}

    public static synchronized boolean checkWinPlayer(int team, Game game) {
        if (team != 1 && team != 2) return false;
        PlayingEntity opponent = team == 1 ? game.getPlayer2() : game.getPlayer1();
        if (opponent.giveUp() || (game.gameOptions.timer.type != Timer.NO_TIMER && opponent.getTime() < 0)) {
            return true;
        }
        return GamePiece.markShortestWinningChain((byte) team, game);
    }

    public static void checkedFlagReset(Game game) {
        for(int x = game.gameOptions.gridSize - 1; x >= 0; x--) {
            for(int y = game.gameOptions.gridSize - 1; y >= 0; y--) {
                game.gamePieces[x][y].setCheckedflage(false);
            }
        }
    }

    public static void winFlagReset(Game game) {
        for(int x = game.gameOptions.gridSize - 1; x >= 0; x--) {
            for(int y = game.gameOptions.gridSize - 1; y >= 0; y--) {
                game.gamePieces[x][y].setWinningPath(false);
            }
        }
    }

    public static void setPiece(Point p, Game game) {
        if(game.getCurrentPlayer() instanceof PlayerObject) ((PlayerObject) game.getCurrentPlayer()).setMove(game, p);
    }

    private static void setGamePiece(byte t, int x, int y, Game game) {
        game.getMoveList().makeMove(x, y, t, System.currentTimeMillis() - game.getMoveStart(), game.getMoveNumber());
        game.gamePieces[x][y].setTeam(t, game);

    }

    public static boolean makeMove(PlayingEntity player, Point hex, Game game) {
        if(game.isGameOver() || game.replayRunning || player == null || hex == null
                || player.getTeam() != (game.getMoveList().size() % 2) + 1 || hex.x < 0 || hex.y < 0
                || hex.x >= game.gameOptions.gridSize || hex.y >= game.gameOptions.gridSize) return false;
        // Callers can submit a second action before the game loop announces the first win.
        if (game.getMoveList().size() >= 2 * game.gameOptions.gridSize - 1
                && GamePiece.markShortestWinningChain((byte) (3 - player.getTeam()), game)) return false;
        if(game.gamePieces[hex.x][hex.y].getTeam() == 0) {
            setGamePiece(player.getTeam(), hex.x, hex.y, game);
            return true;
        }
        else if(game.getMoveNumber() == 2 && game.gamePieces[hex.x][hex.y].getTeam() == 1) {
            // Swap rule
            if(game.gameOptions.swap) {
                // Transpose the opening stone when it changes from the
                // left/right side to the top/bottom side. Keeping an off-axis
                // stone in place gives the swapping player a different move.
                int newX = hex.y;
                int newY = hex.x;
                game.getMoveList().makeSwapMove(newX, newY, player.getTeam(),
                        System.currentTimeMillis() - game.getMoveStart(), game.getMoveNumber());
                game.gamePieces[hex.x][hex.y].setTeam((byte) 0, game);
                game.gamePieces[newX][newY].setTeam(player.getTeam(), game);
                return true;
            }
        }
        return false;
    }

    public static void undo(int gameLocation, Game game) {
        if(game.getMoveNumber() > 1 && game.getPlayer1().supportsUndo(game) && game.getPlayer2().supportsUndo(game)) {
            checkedFlagReset(game);
            winFlagReset(game);

            // Remove the piece from the board and the movelist
            Move lastMove = game.getMoveList().getMove();
            game.gamePieces[lastMove.getX()][lastMove.getY()].setTeam((byte) 0, game);
            game.getMoveList().removeMove();
            game.getMoveList().replay(0, game);

            if(gameLocation == LOCAL_GAME) {
                if(game.isGameOver()) game.incrementCurrentPlayer();

                if(game.getCurrentPlayer().getType().equals(Player.Human)) {
                    game.getWaitingPlayer().undoCalled();

                    if(game.getWaitingPlayer().getType().equals(Player.AI)) {
                        if(game.getMoveNumber() > 1) {
                            lastMove = game.getMoveList().getMove();
                            game.gamePieces[lastMove.getX()][lastMove.getY()].setTeam((byte) 0, game);
                            game.getMoveList().removeMove();

                        }
                        else {
                            game.getCurrentPlayer().endMove();
                        }
                    }
                    else {
                        game.getCurrentPlayer().endMove();
                    }
                }
                else {
                    if(!game.isGameOver()) {
                        game.getCurrentPlayer().undoCalled();
                    }
                }
                if(game.isGameOver() && game.getWaitingPlayer().getType().equals(Player.Human)) game.incrementCurrentPlayer();
            }

            // Reset the game if it's already ended
            if(game.isGameOver()) {
                game.getMoveList().replay(0, game);
                game.start();
            }
        }

        if(game.getGameListener() != null) game.getGameListener().onUndo();
    }

    public static PlayingEntity getPlayer(int i, Game game) {
        if(i == 1) {
            return game.getPlayer1();
        }
        else if(i == 2) {
            return game.getPlayer2();
        }
        else {
            return null;
        }
    }
}
