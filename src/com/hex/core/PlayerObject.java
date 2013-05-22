package com.hex.core;

import java.io.Serializable;
import java.util.concurrent.LinkedBlockingQueue;

public class PlayerObject implements PlayingEntity {
    private static final long serialVersionUID = 1L;
    private String name;
    private int color;
    private long timeLeft;
    public final int team;
    private final LinkedBlockingQueue<Point> hex = new LinkedBlockingQueue<Point>();
    public int player1Type;
    public int player2Type;

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
                // TODO Auto-generated catch block
                e.printStackTrace();
                p = new Point(-1, -1);
            }
            if(p.equals(new Point(-1, -1))) {
                break;
            }
            if(GameAction.makeMove(this, team, p, game)) {
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
    public void lose() {}

    @Override
    public boolean supportsSave() {
        return false;
    }

    @Override
    public void endMove() {
        try {
            hex.put(new Point(-1, -1));
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

    @Override
    public void setMove(Game game, final Object o, final Point point) {
        if(o instanceof GameAction && game.getCurrentPlayer() == this) try {
            hex.put(point);
        }
        catch(InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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
