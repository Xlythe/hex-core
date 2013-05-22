package com.hex.core;

import java.io.Serializable;

public class GamePiece implements Serializable {
    private static final long serialVersionUID = 1L;
    private byte teamNumber = 0; // 1 is left-right, 2 is top-down
    private boolean winningPath;

    boolean checkedflage = false;

    public void setTeam(byte t, Game game) {
        teamNumber = t;
    }

    public byte getTeam() {
        return teamNumber;
    }

    // used for checking victory condition
    public boolean checkpiece(byte team, int x, int y, GamePiece[][] gamePeace) {
        if(team == teamNumber && !checkedflage) {
            checkedflage = !checkedflage;
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
        if(team == teamNumber && !checkedflage) {
            checkedflage = true;
            String tempHolder = findShortestPath(team, x, y, gamePeace);
            checkedflage = false;
            if(tempHolder != null) {
                return tempHolder;
            }
            checkedflage = false;
        }

        return null;

    }

    // used for checking victory condition
    public static void markWinningPath(byte team, int x, int y, Game game) {
        String path = findShortestPath(team, x, y, game.gamePiece);
        colorPath(x, y, path, game);
    }

    public static String findShortestPath(byte team, int x, int y, GamePiece[][] gamePeace) {
        if(checkSpot(team, x, y)) {
            return "";
        }
        String[] allPath = new String[6];

        if(y < gamePeace.length && x - 1 >= 0) {
            allPath[0] = gamePeace[x - 1][y].checkpieceShort(team, x - 1, y, gamePeace);
        }
        if(y < gamePeace.length && x + 1 < gamePeace.length) {
            allPath[1] = gamePeace[x + 1][y].checkpieceShort(team, x + 1, y, gamePeace);

        }
        if(x < gamePeace.length && y - 1 >= 0) {
            allPath[2] = gamePeace[x][y - 1].checkpieceShort(team, x, y - 1, gamePeace);
        }
        if(x < gamePeace.length && y + 1 < gamePeace.length) {
            allPath[3] = gamePeace[x][y + 1].checkpieceShort(team, x, y + 1, gamePeace);
        }
        if(y + 1 < gamePeace.length && x - 1 >= 0) {
            allPath[4] = gamePeace[x - 1][y + 1].checkpieceShort(team, x - 1, y + 1, gamePeace);
        }

        if(y - 1 < gamePeace.length && x + 1 < gamePeace.length && y - 1 >= 0) {
            allPath[5] = gamePeace[x + 1][y - 1].checkpieceShort(team, x + 1, y - 1, gamePeace);
        }
        int dir = findShortestString(allPath, 0, 5);
        if(allPath[dir] == null || allPath[dir] == "null") return null;
        switch(dir) {
        // ud=y-1 & x+1 dd = y+1 & x-1 uy=y-1 dy=y+1 lx=x-1 rx=x+1
        case 0:
            return "lx" + allPath[0];
        case 1:
            return "rx" + allPath[1];
        case 2:
            return "uy" + allPath[2];
        case 3:
            return "dy" + allPath[3];
        case 4:
            return "dd" + allPath[4];
        case 5:
            return "ud" + allPath[5];
        }
        return null;
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
            game.gamePiece[x][y].setWinningPath(true);
            path = path.substring(2, path.length());
        }
    }

    public boolean isWinningPath() {
        return winningPath;
    }

    public void setWinningPath(boolean winningPath) {
        this.winningPath = winningPath;
    }

}
