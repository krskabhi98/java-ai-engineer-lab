package com.AI.AILEAD.exception.InvalidFileException;

public abstract class AiApplicationException extends RuntimeException{

    protected AiApplicationException(String message) {
        super(message);
    }
}
