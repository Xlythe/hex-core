package com.hex.core;

import java.io.Serializable;

/**
 * @author Will Harmon
 **/
public class Timer implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final int NO_TIMER = 0;
    public static final int PER_MOVE = 1;
    public static final int ENTIRE_MATCH = 2;
    private transient boolean refresh = true;
    public long startTime;
    private long elapsedTime;
    public int type;
    public long totalTime;
    public long additionalTime;
    private PlayingEntity currentPlayer;

    public Timer(long totalTime, long additionalTime, int type) {
        this.totalTime = totalTime * 60 * 1000;
        this.additionalTime = additionalTime * 1000;
        this.type = type;
        startTime = System.currentTimeMillis();
    }

    public void start(final Game game) {
        refresh = true;
        if(type != 0) {
            if(game.getGameListener() != null) game.getGameListener().startTimer();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Timer t = Timer.this;
                    t.run(game);
                }
            }).start();
        }
    }

    public void stop() {
        refresh = false;
    }

    public void run(Game game) {
        while(refresh) {
            elapsedTime = System.currentTimeMillis() - startTime;
            currentPlayer = game.getCurrentPlayer();

            if(!game.isGameOver()) {
                currentPlayer.setTime(calculatePlayerTime(currentPlayer, game));
                if(currentPlayer.getTime() > 0) {
                    displayTime(game);
                }
                else {
                    PlayingEntity player = currentPlayer;
                    player.endMove();
                    if(game.getGameListener() != null) game.getGameListener().onTurn(player);
                }
            }

            try {
                Thread.sleep(1000);
            }
            catch(InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private long calculatePlayerTime(PlayingEntity player, Game game) {
        return totalTime - elapsedTime + totalTime - GameAction.getPlayer(player.getTeam() % 2 + 1, game).getTime();
    }

    private void displayTime(Game game) {
        long millis = game.getCurrentPlayer().getTime();
        int seconds = (int) (millis / 1000);
        int minutes = seconds / 60;
        seconds = seconds % 60;
        if(game.getGameListener() != null) game.getGameListener().displayTime(minutes, seconds);
    }
}
