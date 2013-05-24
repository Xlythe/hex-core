package com.hex.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Game implements Runnable, Serializable {
    private static final long serialVersionUID = 1L;
    private boolean gameRunning = true;
    public final GamePiece[][] gamePiece;
    private int moveNumber;
    private MoveList moveList;
    private int currentPlayer;
    private boolean gameOver = false;
    private PlayingEntity player1;
    private PlayingEntity player2;
    private int player1Type;
    private int player2Type;
    private long moveStart;
    public boolean replayRunning = false;
    private transient GameListener gameListener;
    public GameOptions gameOptions;
    private Thread replayThread;
    private long gameStart;
    private long gameEnd;

    public Game(GameOptions gameOptions, PlayingEntity player1, PlayingEntity player2) {
        this.gameOptions = gameOptions;
        this.player1 = player1;
        this.player2 = player2;

        gamePiece = new GamePiece[gameOptions.gridSize][gameOptions.gridSize];
        for(int i = 0; i < gameOptions.gridSize; i++) {
            for(int j = 0; j < gameOptions.gridSize; j++) {
                gamePiece[i][j] = new GamePiece();
            }
        }

        setMoveNumber(1);
        setMoveList(new MoveList());
        currentPlayer = 1;
        gameRunning = true;
        setGameOver(false);
    }

    private void writeObject(ObjectOutputStream outputStream) throws IOException {
        outputStream.writeObject(gameOptions.gridSize);
        outputStream.writeObject(gameOptions.swap);
        outputStream.writeObject(player1Type);
        outputStream.writeObject(player2Type);
        outputStream.writeObject(getPlayer1().getColor());
        outputStream.writeObject(getPlayer2().getColor());
        outputStream.writeObject(getPlayer1().getName());
        outputStream.writeObject(getPlayer2().getName());
        outputStream.writeObject(getMoveList());
        outputStream.writeObject(getMoveNumber());
        outputStream.writeObject(0);// Timer type
        outputStream.writeObject((gameOptions.timer.totalTime / 60) / 1000);
    }

    private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
        int gridSize = (Integer) inputStream.readObject();
        boolean swap = (Boolean) inputStream.readObject();
        GameOptions go = new GameOptions();
        go.gridSize = gridSize;
        go.swap = swap;

        setPlayer1(new PlayerObject(1));
        setPlayer2(new PlayerObject(2));

        player1Type = (Integer) inputStream.readObject();
        player2Type = (Integer) inputStream.readObject();
        getPlayer1().setColor((Integer) inputStream.readObject());
        getPlayer2().setColor((Integer) inputStream.readObject());
        getPlayer1().setName((String) inputStream.readObject());
        getPlayer2().setName((String) inputStream.readObject());
        setMoveList((MoveList) inputStream.readObject());
        setMoveNumber((Integer) inputStream.readObject());
        int timertype = (Integer) inputStream.readObject();
        long timerlength = (Long) inputStream.readObject();
        gameOptions.timer = new Timer(timerlength, 0, timertype);

        inputStream.close();

        currentPlayer = ((getMoveNumber() + 1) % 2) + 1;
        replayRunning = false;

        // Does not support saving PlayingEntities yet
        player1Type = 0;
        player2Type = 0;
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
        gameEnd = System.currentTimeMillis();
    }

    private boolean checkForWinner() {
        GameAction.checkedFlagReset(this);
        if(GameAction.checkWinPlayer(1, this)) {
            gameRunning = false;
            setGameOver(true);
            getPlayer1().win();
            getPlayer2().lose();
            gameListener.onWin(getPlayer1());
        }
        else if(GameAction.checkWinPlayer(2, this)) {
            gameRunning = false;
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
                gamePiece[i][j] = new GamePiece();
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

    private void setPlayer1(PlayingEntity player1) {
        this.player1 = player1;
    }

    public PlayingEntity getPlayer2() {
        return player2;
    }

    private void setPlayer2(PlayingEntity player2) {
        this.player2 = player2;
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
        replayThread = new Thread(new Replay(time, this), "replay");
        replayThread.start();
    }

    public static Game load(File file) throws ClassNotFoundException, IOException {
        // Construct the ObjectInputStream object
        ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(file));

        int gridSize = (Integer) inputStream.readObject();
        boolean swap = (Boolean) inputStream.readObject();
        GameOptions go = new GameOptions();
        go.gridSize = gridSize;
        go.swap = swap;
        Game game = new Game(go, new PlayerObject(1), new PlayerObject(2));

        inputStream.readObject(); // These load player type
        inputStream.readObject(); // which isnt used
        game.getPlayer1().setColor((Integer) inputStream.readObject());
        game.getPlayer2().setColor((Integer) inputStream.readObject());
        game.getPlayer1().setName((String) inputStream.readObject());
        game.getPlayer2().setName((String) inputStream.readObject());
        game.setMoveList((MoveList) inputStream.readObject());
        game.setMoveNumber((Integer) inputStream.readObject());
        int timertype = (Integer) inputStream.readObject();
        long timerlength = (Long) inputStream.readObject();
        game.gameOptions.timer = new Timer(timerlength, 0, timertype);

        inputStream.close();

        game.currentPlayer = ((game.getMoveNumber() + 1) % 2) + 1;
        game.replayRunning = false;

        return game;
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
