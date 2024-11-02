package com.example.leavePortal.CustomException;

public class UnauthorizedException extends RuntimeException  {
    public UnauthorizedException(String message) {
        super(message);
    }
}
