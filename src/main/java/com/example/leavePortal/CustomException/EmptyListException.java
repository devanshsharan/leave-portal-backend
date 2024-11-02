package com.example.leavePortal.CustomException;

public class EmptyListException extends RuntimeException{
    public EmptyListException(String message) {
        super(message);
    }
}
