package org.example.scrd.exception;

public class TokenExpiredException  extends RuntimeException {
    public TokenExpiredException(String message) {
        super(message);
    }
}
