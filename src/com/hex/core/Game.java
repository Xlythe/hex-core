package com.hex.core;

import java.io.Serializable;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Game implements Runnable, Serializable {
    private static final long serialVersionUID = 1L;
    public static final boolean DEBUG = true;

    private MoveList moveList;
    private int currentPlayer;
    private PlayingEntity player1;
    private PlayingEntity player2;
    public GameOptions gameOptions;
    private long gameStart;
    private long gameEnd;

    public final transient GamePiece[][] gamePieces;
    private transient GameListener gameListener;
    public transient boolean replayRunning = false;
    private transient boolean gameOver = false;
    private transient boolean gameRunning = true;
    private transient long moveStart;
    private transient PlayingEntity winner;

    private Thread gameThread;

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

        setMoveList(new MoveList());
        currentPlayer = 1;
        gameRunning = true;
        setGameOver(false);
    }

    public void start() {
        if(getGameListener() != null) getGameListener().onStart();
        setGameOver(false);
        gameRunning = true;
        winner = null;
        getPlayer1().setTime(gameOptions.timer.totalTime);
        getPlayer2().setTime(gameOptions.timer.totalTime);
        gameOptions.timer.start(this);
        setGameThread(new Thread(this, "runningGame"));
        getGameThread().start();
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
        PlayingEntity player = null;
        gameStart = System.currentTimeMillis();
        // tell the players of a new game
        player1.startGame();
        player2.startGame();
        // Loop the game
        if(getGameListener() != null) getGameListener().onTurn(getPlayer1());
        while(gameRunning) {
            if(!checkForWinner()) {
                setMoveStart(System.currentTimeMillis());
                player = getCurrentPlayer();
                if(getGameListener() != null) getGameListener().onTurn(player);

                // Let the player make its move
                player.getPlayerTurn(this);

                // Update the timer
                if(gameOptions.timer.type == Timer.PER_MOVE) {
                    gameOptions.timer.startTime = System.currentTimeMillis();
                    player.setTime(gameOptions.timer.totalTime);
                }
                player.setTime(player.getTime() + gameOptions.timer.additionalTime);
            }

            incrementCurrentPlayer();
        }
    }

    private boolean checkForWinner() {
        GameAction.checkedFlagReset(this);
        if(GameAction.checkWinPlayer(1, this)) {
            winner = getPlayer1();
        }
        else if(GameAction.checkWinPlayer(2, this)) {
            winner = getPlayer2();
        }

        if(winner != null) {
            gameRunning = false;
            gameEnd = System.currentTimeMillis();
            setGameOver(true);
            winner.win();
            getOtherPlayer(winner).lose(this);
            gameListener.onWin(winner);
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

    public PlayingEntity getOtherPlayer(PlayingEntity player) {
        return player.getTeam() == 2 ? getPlayer1() : getPlayer2();
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
        return moveList.size() + 1; // this list starts at zero but move is move
                                    // number one
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

    public long getGameStart() {
        return gameStart;
    }

    public long getGameEnd() {
        return gameEnd;
    }

    public PlayingEntity getWinner() {
        return winner;
    }

    public void replay(int time) {
        clearBoard();
        new Thread(new Replay(time, this), "replay").start();
    }

    public void undo(int moveNumber) {
        if(getMoveNumber() > 1 && moveNumber >= 1 && moveNumber < getMoveNumber()) {
            GameAction.checkedFlagReset(this);
            GameAction.winFlagReset(this);

            while(getMoveNumber() != moveNumber) {
                // Remove the piece from the board and the movelist
                Move lastMove = getMoveList().getMove();

                gamePieces[lastMove.getX()][lastMove.getY()].setTeam((byte) 0, this);
                getMoveList().removeMove();
            }

            if(moveNumber % 2 + 1 == getCurrentPlayer().getTeam()) {
                getCurrentPlayer().endMove();
            }

            getMoveList().replay(0, this);

            // Reset the game if it's already ended
            if(isGameOver()) {
                start();
            }
        }

        if(getGameListener() != null) getGameListener().onUndo();
    }

    public String save() {
        Gson gson = new Gson();
        JsonObject state = new JsonObject();
        state.add("gameOptions", gson.toJsonTree(gameOptions));
        state.add("moveList", gson.toJsonTree(moveList));
        state.addProperty("moveNumber", getMoveNumber());
        state.addProperty("currentPlayer", currentPlayer);
        state.addProperty("gameStart", gameStart);
        state.addProperty("gameEnd", gameEnd);

        JsonObject player1State = new JsonObject();
        player1State.addProperty("type", (player1.getType().equals(Player.AI)) ? ((AI) player1).getAIType() : player1.getType().toString());
        player1State.addProperty("color", player1.getColor());
        player1State.addProperty("name", player1.getName());

        JsonObject player2State = new JsonObject();
        player2State.addProperty("type", (player2.getType().equals(Player.AI)) ? ((AI) player2).getAIType() : player2.getType().toString());
        player2State.addProperty("color", player2.getColor());
        player2State.addProperty("name", player2.getName());

        state.add("player1", player1State);
        state.add("player2", player2State);
        return gson.toJson(state);
    }

    public static Game load(String state) {
        return load(state, new PlayerObject(1), new PlayerObject(2));
    }

    public static Game load(String state, PlayingEntity player1, PlayingEntity player2) {
        final Game game;
        final GameOptions options;
        final MoveList moves;

        Gson gson = new Gson();
        JsonObject object = new JsonParser().parse(state).getAsJsonObject();
        options = gson.fromJson(object.get("gameOptions"), GameOptions.class);
        moves = gson.fromJson(object.get("moveList"), MoveList.class);
        player1.setColor(object.get("player1").getAsJsonObject().get("color").getAsInt());
        player1.setName(object.get("player1").getAsJsonObject().get("name").getAsString());
        player2.setColor(object.get("player2").getAsJsonObject().get("color").getAsInt());
        player2.setName(object.get("player2").getAsJsonObject().get("name").getAsString());

        game = new Game(options, player1, player2);
        int moveNumber = object.get("moveNumber").getAsInt();
        game.currentPlayer = object.get("currentPlayer").getAsInt();
        game.gameStart = object.get("gameStart").getAsLong();
        game.gameEnd = object.get("gameEnd").getAsLong();
        game.moveList = moves;

        if(moveNumber != game.getMoveNumber()) {
            System.err.println("error game number missmach");
            System.err.println("the game has " + game.getMoveNumber() + " but the save file says it shoud have" + moveNumber);
            if(DEBUG) {
                throw new TurnMismatchException("Load error " + game.getMoveNumber() + " vs " + moveNumber);
            }
        }

        return game;
    }

    public Thread getGameThread() {
        return gameThread;
    }

    public void setGameThread(Thread gameThread) {
        this.gameThread = gameThread;
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

    public boolean canNewGame() {
        return(this.player1.supportsNewgame() && this.player2.supportsNewgame());

    }
}
