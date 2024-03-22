package dev.userservice.exceptions;

public class UserDoesNotExistsException extends Exception{
    public UserDoesNotExistsException(String msg){
        super(msg);
    }
}
