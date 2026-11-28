package com.hex.core;

import java.io.Serializable;
import java.util.LinkedList;

/**
 * @author sam
 * 
 */
public class MoveList implements Serializable {

    private static final long serialVersionUID = 1L;

    private LinkedList<Move> moveList;

    /**
     * Makes a new move list
     */
    public MoveList() {
        this.moveList = new LinkedList<Move>();
    }

    /**
     * @return returns the last move
     */
    public Move getMove() {
        return moveList.isEmpty() ? null : moveList.getLast();
    }

    /**
     * @param num
     *            the amount of moves to go back
     * @return returns the move made num moves ago
     */
    public Move getPastMove(int num) {
        if(num < this.size()) {
            return moveList.get(this.size() - (num - 1));
        }
        return null;
    }

    /**
     * makes a new move and adds it to the list
     * 
     * @param x
     * @param y
     * @param teamNumber
     * @param time
     *            this is the time the move was made
     * @param moveNumber
     *            this is the move number stating at one for the first move.
     */
    public void makeMove(int x, int y, byte teamNumber, long time, int moveNumber) {
        moveList.add(new Move(x, y, teamNumber, time, moveNumber));
    }

    public void makeSwapMove(int x, int y, byte teamNumber, long time, int moveNumber) {
        moveList.add(new Move(x, y, teamNumber, time, moveNumber, true));
    }

    /**
     * @param move
     *            add a pre-made move to the list
     */
    public void makeMove(Move move) {
        moveList.add(move);
    }

    /**
     * @return the size of the list, equivalent to calling .size() on a
     *         uitl.linkedlist
     */
    public int size() {
        return moveList.size();
    }

    // for replays
    /**
     * @param time
     *            the time to pause between moves
     * @param game
     *            a copy of the running game
     */
    public void replay(int time, Game game) {
        Move previous = null;
        for(Move m : moveList) {
            if(m.isSwap() && previous != null) {
                game.gamePieces[previous.getX()][previous.getY()].setTeam((byte) 0, game);
            }
            game.gamePieces[m.getX()][m.getY()].setTeam(m.getTeam(), game);
            previous = m;
            if(game.getGameListener() != null) game.getGameListener().onTurn(null);
            if(m != moveList.get(moveList.size() - 1)) {
                try {
                    if(game.replayRunning) Thread.sleep(time);
                }
                catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * removes the last item from the list
     */
    public void removeMove() {
        moveList.removeLast();
    }
}
