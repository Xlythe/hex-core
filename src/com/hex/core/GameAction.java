package com.hex.core;

public class GameAction {
    public static int LOCAL_GAME = 1;
    public static int NET_GAME = 2;

    private GameAction() {}

    public static synchronized boolean checkWinPlayer(int team, Game game) {
        if(team == 1) {
            if(game.gameOptions.timer.type != 0 && game.getPlayer2().getTime() < 0) return true;
            if(game.getPlayer2().giveUp()) return true;
            for(int i = 0; i < game.gameOptions.gridSize; i++) {
                if(GamePiece.checkWinTeam((byte) 1, game.gameOptions.gridSize, i, game.gamePiece)) {
                    System.out.println("Player one wins");
                    checkedFlagReset(game);

                    GamePiece.markWinningPath((byte) 1, game.gameOptions.gridSize, i, game);

                    return true;
                }
            }
            return false;
        }
        else {
            if(game.gameOptions.timer.type != 0 && game.getPlayer1().getTime() < 0) return true;
            if(game.getPlayer1().giveUp()) return true;
            for(int i = 0; i < game.gameOptions.gridSize; i++) {
                if(GamePiece.checkWinTeam((byte) 2, i, game.gameOptions.gridSize, game.gamePiece)) {
                    System.out.println("Player two wins");
                    checkedFlagReset(game);
                    GamePiece.markWinningPath((byte) 2, i, game.gameOptions.gridSize, game);
                    return true;
                }
            }
            return false;
        }
    }

    public static void checkedFlagReset(Game game) {
        for(int x = game.gameOptions.gridSize - 1; x >= 0; x--) {
            for(int y = game.gameOptions.gridSize - 1; y >= 0; y--) {
                game.gamePiece[x][y].checkedflage = false;
            }
        }
    }

    public static void winFlagReset(Game game) {
        for(int x = game.gameOptions.gridSize - 1; x >= 0; x--) {
            for(int y = game.gameOptions.gridSize - 1; y >= 0; y--) {
                game.gamePiece[x][y].setWinningPath(false);
            }
        }
    }

    public static void setPiece(Point p, Game game) {
        if(game.getCurrentPlayer() instanceof PlayerObject) ((PlayerObject) game.getCurrentPlayer()).setMove(game, p);
    }

    private static void setGamePiece(byte t, int x, int y, Game game) {
        game.getMoveList().makeMove(x, y, t, System.currentTimeMillis() - game.getMoveStart(), game.getMoveNumber());
        game.gamePiece[x][y].setTeam(t, game);
        game.setMoveNumber(game.getMoveNumber() + 1);
    }

    public static boolean makeMove(PlayingEntity player, Point hex, Game game) {

        if(player == null || hex.x < 0 && hex.y < 0) return false;
        if(game.replayRunning) return false;

        else if(game.gamePiece[hex.x][hex.y].getTeam() == 0) {
            setGamePiece(player.getTeam(), hex.x, hex.y, game);
            return true;
        }
        else if(game.getMoveNumber() == 2 && game.gamePiece[hex.x][hex.y].getTeam() == 1) {
            // Swap rule
            if(game.gameOptions.swap) {
                setGamePiece(player.getTeam(), hex.x, hex.y, game);
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
            Move lastMove = game.getMoveList().thisMove;
            game.gamePiece[lastMove.getX()][lastMove.getY()].setTeam((byte) 0, game);
            game.setMoveList(game.getMoveList().nextMove);
            game.getMoveList().replay(0, game);
            game.setMoveNumber(game.getMoveNumber() - 1);

            if(gameLocation == LOCAL_GAME) {

                if(game.isGameOver()) game.incrementCurrentPlayer();
                PlayingEntity t = game.getCurrentPlayer();

                if(game.getCurrentPlayer().getClass().isAssignableFrom(PlayerObject.class)) {
                    getPlayer(game.getCurrentPlayer().getTeam() % 2 + 1, game).undoCalled();

                    if(!(getPlayer(game.getCurrentPlayer().getTeam() % 2 + 1, game).getClass().isAssignableFrom(PlayerObject.class))) {
                        if(game.getMoveNumber() > 1) {
                            // System.out.println("I am playing an ai");
                            lastMove = game.getMoveList().thisMove;
                            // game.gamePiece[lastMove.getX()][lastMove.getY()].setTeam((byte)
                            // 0, game);
                            game.setMoveList(game.getMoveList().nextMove);
                            game.setMoveNumber(game.getMoveNumber() - 1);
                        }
                        else {
                            // System.out.println("I am playing a human");
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
                if(game.isGameOver() && (getPlayer(game.getCurrentPlayer().getTeam() % 2 + 1, game) instanceof PlayerObject)) game.incrementCurrentPlayer();
            }
            else if(gameLocation == NET_GAME) {
                // // Inside a net game
                // if(game.currentPlayer == 1) {// First player's turn
                // if(game.player1 instanceof NetPlayerObject) {// First player
                // // is on the
                // // network (not
                // // local)
                // if(NetGlobal.undoRequested) {// First player requested
                // // the undo
                // // undo twice, don't switch players
                // if(game.moveNumber > 1) {
                // lastMove = game.moveList.thisMove;
                // game.gamePiece[lastMove.getX()][lastMove.getY()].setTeam((byte)
                // 0, game);
                // game.moveList = game.moveList.nextMove;
                // game.moveNumber--;
                // }
                // if(game.gameOver) game.currentPlayer = (game.currentPlayer %
                // 2) + 1;
                // }
                // else {// Second player requested the undo
                // // undo once, switch players
                // GameAction.getPlayer(game.currentPlayer, game).endMove();
                // }
                // }
                // else {// First player is local (not on the network)
                // if(NetGlobal.undoRequested) {// Second player requested
                // // the undo
                // // undo once, switch players
                // getPlayer(game.currentPlayer, game).endMove();
                // }
                // else {// First player requested the undo
                // // undo twice, don't switch players
                // if(game.moveNumber > 1) {
                // lastMove = game.moveList.thisMove;
                // game.gamePiece[lastMove.getX()][lastMove.getY()].setTeam((byte)
                // 0, game);
                // game.moveList = game.moveList.nextMove;
                // game.moveNumber--;
                // }
                // if(game.gameOver) game.currentPlayer = (game.currentPlayer %
                // 2) + 1;
                // }
                // }
                // }
                // else {// Second player's turn
                // if(game.player2 instanceof NetPlayerObject) {// Second
                // // player is
                // // local (not
                // // on the
                // // network)
                // if(NetGlobal.undoRequested) {// First player requested
                // // the undo
                // // undo once, switch players
                // getPlayer(game.currentPlayer, game).endMove();
                // }
                // else {// Second player requested the undo
                // // undo twice, don't switch players
                // if(game.moveNumber > 1) {
                // lastMove = game.moveList.thisMove;
                // game.gamePiece[lastMove.getX()][lastMove.getY()].setTeam((byte)
                // 0, game);
                // game.moveList = game.moveList.nextMove;
                // game.moveNumber--;
                // }
                // if(game.gameOver) game.currentPlayer = (game.currentPlayer %
                // 2) + 1;
                // }
                // }
                // else {// Second player is on the network (not local)
                // if(NetGlobal.undoRequested) {// Second player requested
                // // the undo
                // // undo twice, don't switch players
                // if(game.moveNumber > 1) {
                // lastMove = game.moveList.thisMove;
                // game.gamePiece[lastMove.getX()][lastMove.getY()].setTeam((byte)
                // 0, game);
                // game.moveList = game.moveList.nextMove;
                // game.moveNumber--;
                // }
                // if(game.gameOver) game.currentPlayer = (game.currentPlayer %
                // 2) + 1;
                // }
                // else {// First player requested the undo
                // // undo once, switch players
                // GameAction.getPlayer(game.currentPlayer, game).endMove();
                // }
                // }
                // }
                //
                // NetGlobal.undoRequested = false;
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
