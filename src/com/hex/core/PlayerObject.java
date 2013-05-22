package com.hex.core;

import java.io.Serializable;

public class PlayerObject implements PlayingEntity {
    private static final long serialVersionUID = 1L;
    private String name;
    private int color;
    private long timeLeft;
    public final int team;
    public int player1Type;
    public int player2Type;
    private Point point;

    public PlayerObject(int team) {
        this.team = team;
    }

    @Override
    public void getPlayerTurn(Game game) {}

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
    public void lose() {}

    @Override
    public boolean supportsSave() {
        return false;
    }

    @Override
    public void endMove() {
        point = new Point(-1, -1);
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
        this.point = point;
    }

    @Override
    public boolean giveUp() {
        return false;
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
    public int getType() {
        return 0;
    }
}
