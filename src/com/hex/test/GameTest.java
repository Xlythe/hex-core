package com.hex.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.hex.core.Game;
import com.hex.core.Point;
import com.hex.core.Timer;

public class GameTest {
    @Test
    public void testInvalidMove() {
        Game.GameOptions gameOptions = new Game.GameOptions();
        TestPlayer player1 = new TestPlayer(1);
        TestPlayer player2 = new TestPlayer(2);

        gameOptions.gridSize = 7;
        gameOptions.swap = false;
        gameOptions.timer = new Timer(0, 0, Timer.NO_TIMER);

        Game game = new Game(gameOptions, player1, player2);

        player1.setMove(game, new Point(-1, -1));

        assertEquals("Player 1 must play on the game board", true, game.getCurrentPlayer().equals(player1));
    }

    @Test
    public void testValidMove() throws InterruptedException {
        Game.GameOptions gameOptions = new Game.GameOptions();
        TestPlayer player1 = new TestPlayer(1);
        TestPlayer player2 = new TestPlayer(2);

        gameOptions.gridSize = 7;
        gameOptions.swap = false;
        gameOptions.timer = new Timer(0, 0, Timer.NO_TIMER);

        Game game = new Game(gameOptions, player1, player2);

        player1.setMove(game, new Point(1, 1));

        wait(1000);

        assertEquals("Player 1 must play on the game board", false, game.getCurrentPlayer().equals(player1));
    }
}
