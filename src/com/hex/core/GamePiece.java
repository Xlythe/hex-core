package com.hex.core;

import java.io.Serializable;
import java.util.ArrayDeque;

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

    public static String findShortestPath(byte team, int x, int y, GamePiece[][] gamePeace) {
        int size = gamePeace.length;
        if(x < 0 || y < 0 || x >= size || y >= size || gamePeace[x][y].getTeam() != team) return null;
        int[] dx = {-1, 1, 0, 0, -1, 1};
        int[] dy = {0, 0, -1, 1, 1, -1};
        String[] labels = {"lx", "rx", "uy", "dy", "dd", "ud"};
        boolean[][] seen = new boolean[size][size];
        int[][] previousX = new int[size][size];
        int[][] previousY = new int[size][size];
        int[][] previousDirection = new int[size][size];
        ArrayDeque<int[]> queue = new ArrayDeque<int[]>();
        queue.add(new int[] {x, y});
        seen[x][y] = true;
        int endX = -1, endY = -1;
        while(!queue.isEmpty()) {
            int[] current = queue.removeFirst();
            if(checkSpot(team, current[0], current[1])) {
                endX = current[0]; endY = current[1];
                break;
            }
            for(int direction = 0; direction < 6; direction++) {
                int nextX = current[0] + dx[direction];
                int nextY = current[1] + dy[direction];
                if(nextX < 0 || nextY < 0 || nextX >= size || nextY >= size
                        || seen[nextX][nextY] || gamePeace[nextX][nextY].getTeam() != team) continue;
                seen[nextX][nextY] = true;
                previousX[nextX][nextY] = current[0];
                previousY[nextX][nextY] = current[1];
                previousDirection[nextX][nextY] = direction;
                queue.addLast(new int[] {nextX, nextY});
            }
        }
        if(endX < 0) return null;
        StringBuilder path = new StringBuilder();
        while(endX != x || endY != y) {
            int direction = previousDirection[endX][endY];
            path.insert(0, labels[direction]);
            int parentX = previousX[endX][endY];
            endY = previousY[endX][endY];
            endX = parentX;
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
