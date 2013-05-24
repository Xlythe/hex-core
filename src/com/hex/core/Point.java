package com.hex.core;

import java.io.Serializable;

public class Point implements Serializable {
    private static final long serialVersionUID = 1L;
    public int x;
    public int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object object) {
        if(object instanceof Point) {
            return x == ((Point) object).x && y == ((Point) object).y;
        }
        return false;
    }
}
