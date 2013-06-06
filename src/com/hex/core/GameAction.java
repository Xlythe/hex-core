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
                if(GamePiece.checkWinTeam((byte) 1, game.gameOptions.gridSize, i, game.gamePieces)) {
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
                if(GamePiece.checkWinTeam((byte) 2, i, game.gameOptions.gridSize, game.gamePieces)) {
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
        if(game.replayRunning || player == null || hex.x < 0 && hex.y < 0) return false;
        else if(game.gamePieces[hex.x][hex.y].getTeam() == 0) {
            setGamePiece(player.getTeam(), hex.x, hex.y, game);
            return true;
        }
        else if(game.getMoveNumber() == 2 && game.gamePieces[hex.x][hex.y].getTeam() == 1) {
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
