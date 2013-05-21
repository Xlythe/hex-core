package com.hex.core;

/**
 * @author Will Harmon
 **/
public class Replay implements Runnable {
    private final int time;
    private final Game game;

    public Replay(int time, Game game) {
        this.time = time;
        this.game = game;
    }

    @Override
    public void run() {
        if(game.getGameListener() != null) game.getGameListener().onReplayStart();
        game.replayRunning = true;
        game.getMoveList().replay(time, game);
        if(game.getGameListener() != null) game.getGameListener().onTurn(null);
        game.replayRunning = false;
        if(game.isGameOver()) {
            game.incrementCurrentPlayer();
            game.getCurrentPlayer().endMove();
        }
        if(game.getGameListener() != null) game.getGameListener().onReplayEnd();
        game.start();
    }
}
