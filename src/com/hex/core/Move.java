package com.hex.core;

import java.io.Serializable;

public class Move implements Serializable {
    private static final long serialVersionUID = -7439386690818203133L;
    private int x;
    private int y;
    private long time;
    private byte team;
    private int moveNumber;

    public Move(int x, int y, byte team, long time, int moveNumber) {
        this.setX(x);
        this.setY(y);
        this.setTime(time);
        this.setTeam(team);
        this.setMoveNumber(moveNumber);

    }

    protected void setTime(long time) {
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    protected void setX(int x) {
        this.x = x;
    }

    public int getX() {
        return x;
    }

    protected void setY(int y) {
        this.y = y;
    }

    public int getY() {
        return y;
    }

    protected void setTeam(byte team) {
        this.team = team;
    }

    public byte getTeam() {
        return team;
    }

    public int getMoveNumber() {
        return moveNumber;
    }

    public void setMoveNumber(int moveNumber) {
        this.moveNumber = moveNumber;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj.getClass().isAssignableFrom(Move.class)) {
            Move m = (Move) obj;
            return m.getMoveNumber() == this.getMoveNumber() && m.getTeam() == this.getTeam() && m.getTime() == this.getTime() && m.getX() == this.getX()
                    && m.getY() == this.getY();
        }
        return false;
    }
}
