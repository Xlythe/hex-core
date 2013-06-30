package com.hex.core;

import java.io.Serializable;
import java.util.concurrent.LinkedBlockingQueue;

public class PlayerObject implements PlayingEntity {
    private static final long serialVersionUID = 1L;
    private static final Point END_MOVE = new Point(-1, -1);
    private String name;
    private int color;
    private long timeLeft;
    public final int team;
    private boolean forfeit;
    private final transient LinkedBlockingQueue<Point> hex = new LinkedBlockingQueue<Point>();

    public PlayerObject(int team) {
        this.team = team;
    }

    @Override
    public void getPlayerTurn(Game game) {
        hex.clear();
        while(true) {
            Point p;
            try {
                p = hex.take();
            }
            catch(InterruptedException e) {
                e.printStackTrace();
                p = END_MOVE;
            }

            if(p.equals(END_MOVE)) {
                break;
            }
            if(GameAction.makeMove(this, p, game)) {
                break;
            }
        }
    }

    @Override
    public void undoCalled() {}

    @Override
    public void newgameCalled() {
        endMove();
    }

    @Override
    public boolean supportsUndo(Game game) {
        return true;
    }

    @Override
    public boolean supportsNewgame() {
        return true;
    }

    @Override
    public void quit() {
        endMove();
    }

    @Override
    public void win() {}

    @Override
    public void lose(Game game) {}

    @Override
    public boolean supportsSave() {
        return false;
    }

    @Override
    public void endMove() {
        try {
            hex.put(END_MOVE);
        }
        catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public int getColor() {
        return color;
    }

    @Override
    public void setTime(long time) {
        this.timeLeft = time;
    }

    @Override
    public long getTime() {
        return timeLeft;
    }

    protected void setMove(Game game, final Point point) {
        try {
            hex.put(point);
        }
        catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean giveUp() {
        return forfeit;
    }

    @Override
    public Serializable getSaveState() {
        return null;
    }

    @Override
    public void setSaveState(Serializable state) {}

    @Override
    public byte getTeam() {
        return (byte) team;
    }

    @Override
    public Player getType() {
        return Player.Human;
    }

    @Override
    public void startGame() {
        this.hex.clear();
    }

    public void forfeit() {
        this.forfeit = true;
    }
}
