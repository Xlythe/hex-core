/**
 * 
 */
package com.hex.core;

import java.io.Serializable;

/**
 * @author Sam Laane
 * 
 */
public interface PlayingEntity extends Serializable {
    /**
     * Must call GameAction.makeMove() eventually
     * 
     * Logic for making a move is here.
     * */
    public void getPlayerTurn(Game game);

    /**
     * Will you allow an undo? Return true if your PlayingEntity supports undo
     * Return false if it doesn't or if you want an asynchronous undo (such as
     * in LAN or Net play)
     * */
    public boolean supportsUndo(Game game);

    /**
     * Undo has been applied. The last move is blank again. If you're an AI and
     * keep a bunch of variables, roll them back
     * */
    public void undoCalled();

    /**
     * Will you allow a new game? Return true if your PlayingEntity supports new
     * games. Return false if it doesn't or if you want an asynchronous new game
     * (such as in LAN or Net play)
     * */
    public boolean supportsNewgame();

    /**
     * New game has been applied. The entire game board is wiped. Do not call
     * GameAction.makeMove() and quit as soon as possible.
     * */
    public void newgameCalled();

    /**
     * The player has decided they want to save this game. Return true if your
     * PlayingEntity supports saving. Return false if it doesn't. If you return
     * false, the default human player will be saved instead.
     * */
    public boolean supportsSave();

    /**
     * Put everything you want to save in this one object. It will be passed to
     * setSaveState() when the game is loaded up.
     * */
    public Serializable getSaveState();

    /**
     * What was once shall be again. You've been loaded so prepare to fight
     * again.
     * */
    public void setSaveState(Serializable state);

    /**
     * The game is over. Die gracefully.
     * */
    public void quit();

    /**
     * You won the game! Use this to handle any final actions such as sending
     * final moves or comparing game boards to make sure no one cheated.
     * Remember, you're still alive until quit() is called.
     **/
    public void win();

    /**
     * You lost the game! Use this to handle any final actions such as sending
     * final moves or comparing game boards to make sure no one cheated.
     * Remember, you're still alive until quit() is called.
     **/
    public void lose();

    /**
     * Quickly stop making a move. Do not run GameAction.makeMove()
     * */
    public void endMove();

    // Standard variables

    /**
     * Sets the player's name
     * */
    public void setName(String name);

    /**
     * Returns the player's name
     * */
    public String getName();

    /**
     * Sets the player's color
     * */
    public void setColor(int color);

    /**
     * Returns the player's color
     * */
    public int getColor();

    /**
     * Sets the player's time left
     * */
    public void setTime(long time);

    /**
     * Returns the player's time left
     * */
    public long getTime();

    /**
     * Store a point to play when its your turn
     * */
    public void setMove(Game game, Object o, Point hex);

    /**
     * Returns the player's team
     * */
    public byte getTeam();

    /**
     * Returns a unique number specific to the PlayingEntity class
     * */
    public int getType();

    /**
     * Return true to announce defeat mid-game
     * */
    public boolean giveUp();
}
