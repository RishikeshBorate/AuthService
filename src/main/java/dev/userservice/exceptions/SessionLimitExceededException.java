package dev.userservice.exceptions;

public class SessionLimitExceededException extends Exception{
    public SessionLimitExceededException(String msg){
        super(msg);
    }
}
