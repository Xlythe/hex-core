package com.hex.core;

import java.io.Serializable;

import com.google.gson.Gson;

public class Game implements Runnable, Serializable {
    private static final long serialVersionUID = 1L;
    private int moveNumber;
    private MoveList moveList;
    private int currentPlayer;
    private PlayingEntity player1;
    private PlayingEntity player2;
    private transient long moveStart;
    public GameOptions gameOptions;
    private long gameStart;
    private long gameEnd;

    public final transient GamePiece[][] gamePieces;
    private transient GameListener gameListener;
    public transient boolean replayRunning = false;
    private transient boolean gameOver = false;
    private transient boolean gameRunning = true;

    public Game(GameOptions gameOptions, PlayingEntity player1, PlayingEntity player2) {
        this.gameOptions = gameOptions;
        this.player1 = player1;
        this.player2 = player2;

        gamePieces = new GamePiece[gameOptions.gridSize][gameOptions.gridSize];
        for(int i = 0; i < gameOptions.gridSize; i++) {
            for(int j = 0; j < gameOptions.gridSize; j++) {
                gamePieces[i][j] = new GamePiece();
            }
        }

        setMoveNumber(1);
        setMoveList(new MoveList());
        currentPlayer = 1;
        gameRunning = true;
        setGameOver(false);
    }

    public void start() {
        if(getGameListener() != null) getGameListener().onStart();
        setGameOver(false);
        gameRunning = true;
        getPlayer1().setTime(gameOptions.timer.totalTime);
        getPlayer2().setTime(gameOptions.timer.totalTime);
        gameOptions.timer.start(this);
        new Thread(this, "runningGame").start();
    }

    public void stop() {
        if(getGameListener() != null) getGameListener().onStop();
        gameRunning = false;
        gameOptions.timer.stop();
        getPlayer1().quit();
        getPlayer2().quit();
        setGameOver(true);
    }

    @Override
    public void run() {
        PlayingEntity player;
        gameStart = System.currentTimeMillis();

        // Loop the game
        if(getGameListener() != null) getGameListener().onTurn(getPlayer1());
        while(gameRunning) {
            if(!checkForWinner()) {
                setMoveStart(System.currentTimeMillis());
                player = getCurrentPlayer();

                // Let the player make its move
                player.getPlayerTurn(this);

                // Update the timer
                if(gameOptions.timer.type == 1) {
                    gameOptions.timer.startTime = System.currentTimeMillis();
                    player.setTime(gameOptions.timer.totalTime);
                }
                player.setTime(player.getTime() + gameOptions.timer.additionalTime);

                if(getGameListener() != null) getGameListener().onTurn(player);
            }

            incrementCurrentPlayer();
        }
    }

    private boolean checkForWinner() {
        GameAction.checkedFlagReset(this);
        if(GameAction.checkWinPlayer(1, this)) {
            gameRunning = false;
            gameEnd = System.currentTimeMillis();
            setGameOver(true);
            getPlayer1().win();
            getPlayer2().lose();
            gameListener.onWin(getPlayer1());
        }
        else if(GameAction.checkWinPlayer(2, this)) {
            gameRunning = false;
            gameEnd = System.currentTimeMillis();
            setGameOver(true);
            getPlayer1().lose();
            getPlayer2().win();
            gameListener.onWin(getPlayer2());
        }

        return isGameOver();
    }

    public void clearBoard() {
        for(int i = 0; i < gameOptions.gridSize; i++) {
            for(int j = 0; j < gameOptions.gridSize; j++) {
                gamePieces[i][j] = new GamePiece();
            }
        }
        gameListener.onClear();
    }

    protected void incrementCurrentPlayer() {
        currentPlayer = (currentPlayer % 2) + 1;
    }

    public void setGameListener(GameListener gameListener) {
        this.gameListener = gameListener;
    }

    public GameListener getGameListener() {
        return gameListener;
    }

    public PlayingEntity getCurrentPlayer() {
        return GameAction.getPlayer(currentPlayer, this);
    }

    public PlayingEntity getWaitingPlayer() {
        return GameAction.getPlayer(currentPlayer % 2 + 1, this);
    }

    public MoveList getMoveList() {
        return moveList;
    }

    void setMoveList(MoveList moveList) {
        this.moveList = moveList;
    }

    public long getMoveStart() {
        return moveStart;
    }

    private void setMoveStart(long moveStart) {
        this.moveStart = moveStart;
    }

    public PlayingEntity getPlayer1() {
        return player1;
    }

    public PlayingEntity getPlayer2() {
        return player2;
    }

    public int getMoveNumber() {
        return moveNumber;
    }

    public void setMoveNumber(int moveNumber) {
        this.moveNumber = moveNumber;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    private void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public long getGameLength() {
        if(gameEnd == 0) return System.currentTimeMillis() - gameStart;
        return gameEnd - gameStart;
    }

    public void replay(int time) {
        clearBoard();
        new Thread(new Replay(time, this), "replay").start();
    }

    public String save() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static Game load(String state) {
        Gson gson = new Gson();
        return gson.fromJson(state, Game.class);
    }

    public static class GameOptions implements Serializable {
        private static final long serialVersionUID = 1L;
        public Timer timer;
        public int gridSize;
        public boolean swap;

    }

    public static interface GameListener {
        public void onWin(PlayingEntity player);

        public void onClear();

        public void onStart();

        public void onStop();

        public void onTurn(PlayingEntity player);

        public void onReplayStart();

        public void onReplayEnd();

        public void onUndo();

        public void startTimer();

        public void displayTime(int minutes, int seconds);
    }
}
