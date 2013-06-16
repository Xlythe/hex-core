package com.hex.core;

public class TurnMismatchException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public TurnMismatchException(String string) {
        super(string);
    }
}
