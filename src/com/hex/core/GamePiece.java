package com.hex.core;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GamePiece implements Serializable {
    private static final long serialVersionUID = 1L;
    private byte teamNumber = 0; // 1 is left-right, 2 is top-down
    private transient boolean winningPath;
    private transient boolean checkedflage = false;

    public void setTeam(byte t, Game game) {
        teamNumber = t;
    }

    public byte getTeam() {
        return teamNumber;
    }

    // used for checking victory condition
    public boolean checkpiece(byte team, int x, int y, GamePiece[][] gamePeace) {
        if(team == teamNumber && !isCheckedflage()) {
            setCheckedflage(!isCheckedflage());
            if(checkSpot(team, x, y) || checkWinTeam(team, x, y, gamePeace)) {
                return true;
            }
        }
        return false;
    }

    // used for checking victory condition
    public static boolean checkWinTeam(byte team, int x, int y, GamePiece[][] gamePeace) {
        if(y < gamePeace.length && x - 1 >= 0 && gamePeace[x - 1][y].checkpiece(team, x - 1, y, gamePeace)) {
            return true;
        }
        if(y < gamePeace.length && x + 1 < gamePeace.length && gamePeace[x + 1][y].checkpiece(team, x + 1, y, gamePeace)) {
            return true;
        }
        if(x < gamePeace.length && y - 1 >= 0 && gamePeace[x][y - 1].checkpiece(team, x, y - 1, gamePeace)) {
            return true;
        }
        if(x < gamePeace.length && y + 1 < gamePeace.length && gamePeace[x][y + 1].checkpiece(team, x, y + 1, gamePeace)) {
            return true;
        }
        if(y + 1 < gamePeace.length && x - 1 >= 0 && gamePeace[x - 1][y + 1].checkpiece(team, x - 1, y + 1, gamePeace)) {
            return true;
        }
        if(y - 1 < gamePeace.length && x + 1 < gamePeace.length && y - 1 >= 0 && gamePeace[x + 1][y - 1].checkpiece(team, x + 1, y - 1, gamePeace)) {
            return true;
        }

        return false;
    }

    // used for checking victory condition
    public String checkpieceShort(byte team, int x, int y, GamePiece[][] gamePeace) {
        if(team == teamNumber && !isCheckedflage()) {
            setCheckedflage(true);
            String tempHolder = findShortestPath(team, x, y, gamePeace);
            setCheckedflage(false);
            if(tempHolder != null) {
                return tempHolder;
            }
            setCheckedflage(false);
        }

        return null;

    }

    // used for checking victory condition
    public static void markWinningPath(byte team, int x, int y, Game game) {
        String path = findShortestPath(team, x, y, game.gamePieces);
        colorPath(x, y, path, game);
    }

    private static final int[] DX = {-1, 1, 0, 0, -1, 1};
    private static final int[] DY = {0, 0, -1, 1, 1, -1};
    private static final String[] LABEL = {"lx", "rx", "uy", "dy", "dd", "ud"};

    // A null start seeds every occupied cell on the far edge at distance zero.
    private static List<Point> shortestChain(byte team, GamePiece[][] board, Point start) {
        int size = board.length;
        byte[][] parent = new byte[size][size]; // 0 = unseen, 1 = source, 2..7 = incoming direction
        ArrayDeque<Point> queue = new ArrayDeque<Point>();
        if (team != 1 && team != 2) return Collections.emptyList();
        for (int i = 0; i < (start == null ? size : 1); i++) {
            int x = start == null ? (team == 1 ? size - 1 : i) : start.x;
            int y = start == null ? (team == 2 ? size - 1 : i) : start.y;
            if (x >= 0 && x < size && y >= 0 && y < size && board[x][y].getTeam() == team) {
                parent[x][y] = 1;
                queue.addLast(new Point(x, y));
            }
        }
        while (!queue.isEmpty()) {
            Point cell = queue.removeFirst();
            if (checkSpot(team, cell.x, cell.y)) {
                List<Point> path = new ArrayList<Point>();
                path.add(cell);
                while (parent[cell.x][cell.y] != 1) {
                    int direction = parent[cell.x][cell.y] - 2;
                    cell = new Point(cell.x - DX[direction], cell.y - DY[direction]);
                    path.add(cell);
                }
                Collections.reverse(path);
                return path;
            }
            for (int d = 0; d < DX.length; d++) {
                int x = cell.x + DX[d], y = cell.y + DY[d];
                if (x >= 0 && x < size && y >= 0 && y < size
                        && parent[x][y] == 0 && board[x][y].getTeam() == team) {
                    parent[x][y] = (byte) (d + 2);
                    queue.addLast(new Point(x, y));
                }
            }
        }
        return Collections.emptyList();
    }

    static boolean markShortestWinningChain(byte team, Game game) {
        List<Point> path = shortestChain(team, game.gamePieces, null);
        if (path.isEmpty()) return false;
        GameAction.winFlagReset(game);
        for (Point point : path) game.gamePieces[point.x][point.y].setWinningPath(true);
        return true;
    }

    /** Historical single-start API. Returns direction pairs leading to the near edge. */
    public static String findShortestPath(byte team, int x, int y, GamePiece[][] board) {
        List<Point> chain = shortestChain(team, board, new Point(x, y));
        if (chain.isEmpty()) return null;
        StringBuilder path = new StringBuilder();
        for (int i = 1; i < chain.size(); i++) {
            Point previous = chain.get(i - 1), next = chain.get(i);
            for (int d = 0; d < DX.length; d++) {
                if (previous.x + DX[d] == next.x && previous.y + DY[d] == next.y) {
                    path.append(LABEL[d]);
                    break;
                }
            }
        }
        return path.toString();
    }

    // used for checking victory condition
    static int findShortestString(String[] paths, int lo, int hi) {
        if((lo == hi)) {
            return hi;
        }
        int temp = findShortestString(paths, lo + 1, hi);
        return stringL(paths[lo]) < stringL(paths[temp]) ? lo : temp;
    }

    // used for checking victory condition
    private static int stringL(String temp) {
        if(temp == null) return Integer.MAX_VALUE;
        else return temp.length();
    }

    public static boolean checkSpot(byte team, int x, int y) {
        if(team == 1 && x == 0) {
            return true;
        }
        if(team == 2 && y == 0) {
            return true;
        }
        return false;
    }

    private enum posDir {
        lx, rx, uy, dy, dd, ud

    }

    public static void colorPath(int x, int y, String path, Game game) {
        if(path != null) game.gamePieces[x][y].setWinningPath(true);
        while(path != null && !path.isEmpty()) {
            switch(posDir.valueOf(path.substring(0, 2))) {
            // ud=y-1 & x+1 dd = y+1 & x-1 uy=y-1 dy=y+1 lx=x-1 rx=x+1
            case lx:
                x -= 1;
                break;
            case rx:
                x += 1;
                break;
            case uy:
                y -= 1;
                break;
            case dy:
                y += 1;
                break;
            case dd:
                y += 1;
                x -= 1;
                break;
            case ud:
                y -= 1;
                x += 1;
                break;
            }
            game.gamePieces[x][y].setWinningPath(true);
            path = path.substring(2, path.length());
        }
    }

    public boolean isWinningPath() {
        return winningPath;
    }

    public void setWinningPath(boolean winningPath) {
        this.winningPath = winningPath;
    }

    public boolean isCheckedflage() {
        return checkedflage;
    }

    public void setCheckedflage(boolean checkedflage) {
        this.checkedflage = checkedflage;
    }

}
