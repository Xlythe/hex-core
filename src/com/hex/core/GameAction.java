package com.hex.core;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class GameAction {
    public static int LOCAL_GAME = 1;
    public static int NET_GAME = 2;

    private GameAction() {}

    public static synchronized boolean checkWinPlayer(int team, Game game) {
        if(team == 1) {
            if(game.gameOptions.timer.type != 0 && game.player2.getTime() < 0) return true;
            if(game.player2.giveUp()) return true;
            for(int i = 0; i < game.gameOptions.gridSize; i++) {
                if(RegularPolygonGameObject.checkWinTeam((byte) 1, game.gameOptions.gridSize, i, game.gamePiece)) {
                    System.out.println("Player one wins");
                    checkedFlagReset(game);
                    RegularPolygonGameObject.markWinningPath((byte) 2,game.gameOptions.gridSize,i,game);
                    return true;
                }
            }
            return false;
        }
        else {
            if(game.gameOptions.timer.type != 0 && game.player1.getTime() < 0) return true;
            if(game.player1.giveUp()) return true;
            for(int i = 0; i < game.gameOptions.gridSize; i++) {
                if(RegularPolygonGameObject.checkWinTeam((byte) 2, i, game.gameOptions.gridSize, game.gamePiece)) {
                    System.out.println("Player two wins");
                    checkedFlagReset(game);
                    RegularPolygonGameObject.markWinningPath((byte) 2,game.gameOptions.gridSize,i,game); 
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

    public static void setPiece(Point p, Game game) {
        getPlayer(game.currentPlayer, game).setMove(game, new GameAction(), p);
    }

    private static void setTeam(byte t, int x, int y, Game game) {
        game.moveList.makeMove(x, y, t, System.currentTimeMillis() - game.moveStart, game.moveNumber);
        game.gamePiece[x][y].setTeam(t, game);
        game.moveNumber++;
        game.gameListener.onTeamSet();
    }

    public static boolean makeMove(PlayingEntity player, int team, Point hex, Game game) {
        if(player == null || hex.x < 0 && hex.y < 0) return false;
        else if(game.gamePiece[hex.x][hex.y].getTeam() == 0) {
            setTeam((byte) team, hex.x, hex.y, game);
            return true;
        }
        else if(game.moveNumber == 2 && game.gamePiece[hex.x][hex.y].getTeam() == 1) {
            // Swap rule
            if(game.gameOptions.swap) {
                setTeam((byte) team, hex.x, hex.y, game);
                return true;
            }
        }
        return false;
    }

    public static void undo(int gameLocation, Game game) {
        if(game.moveNumber > 1 && game.player1.supportsUndo(game) && game.player2.supportsUndo(game)) {
            checkedFlagReset(game);

            // Remove the piece from the board and the movelist
            Move lastMove = game.moveList.thisMove;
            game.gamePiece[lastMove.getX()][lastMove.getY()].setTeam((byte) 0, game);
            game.moveList = game.moveList.nextMove;
            game.moveList.replay(0, game);
            game.moveNumber--;

            if(gameLocation == LOCAL_GAME) {
                if(game.gameOver) game.currentPlayer = (game.currentPlayer % 2) + 1;

                if(getPlayer(game.currentPlayer, game) instanceof PlayerObject) {
                    getPlayer(game.currentPlayer % 2 + 1, game).undoCalled();

                    if(!(getPlayer(game.currentPlayer % 2 + 1, game) instanceof PlayerObject)) {
                        if(game.moveNumber > 1) {
                            lastMove = game.moveList.thisMove;
                            game.gamePiece[lastMove.getX()][lastMove.getY()].setTeam((byte) 0, game);
                            game.moveList = game.moveList.nextMove;
                            game.moveNumber--;
                        }
                        else {
                            getPlayer(game.currentPlayer, game).endMove();
                        }
                    }
                    else {
                        getPlayer(game.currentPlayer, game).endMove();
                    }
                }
                else {
                    if(!game.gameOver) {
                        getPlayer(game.currentPlayer, game).undoCalled();
                    }
                }
                if(game.gameOver && (getPlayer(game.currentPlayer % 2 + 1, game) instanceof PlayerObject)) game.currentPlayer = (game.currentPlayer % 2) + 1;
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
            if(game.gameOver) {
                game.moveList.replay(0, game);
                game.start();
            }
        }

        game.gameListener.onUndo();
    }

    public static String insert(String text, Object name) {
        String inserted = text.replaceAll("#", name.toString());
        return inserted;
    }

    public static PlayingEntity getPlayer(int i, Game game) {
        if(i == 1) {
            return game.player1;
        }
        else if(i == 2) {
            return game.player2;
        }
        else {
            return null;
        }
    }

    final static String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static String pointToString(Point p, Game game) {
        if(game.moveNumber == 2 && game.moveList.thisMove.equals(game.moveList.nextMove.thisMove)) return "SWAP";
        String str = "";
        str += alphabet.charAt(p.y);
        str += (p.x + 1);
        return str;
    }

    public static Point stringToPoint(String str, Game game) {
        if(game.moveNumber == 1 && str.equals("SWAP")) return new Point(-1, -1);
        if(str.equals("SWAP")) return new Point(game.moveList.thisMove.getX(), game.moveList.thisMove.getY());
        int x = Integer.parseInt(str.substring(1)) - 1;
        char y = str.charAt(0);

        return new Point(x, alphabet.indexOf(y));
    }

    public static String md5(String s) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes(), 0, s.length());
            String hash = new BigInteger(1, digest.digest()).toString(16);
            return hash;
        }
        catch(NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

}
