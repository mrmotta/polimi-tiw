package it.polimi.tiw.excetions;

import java.io.Serial;

public class EmptyCredentialsException extends Exception {

    @Serial
    private static final long serialVersionUID = -3779592973139384305L;

    public EmptyCredentialsException() {
        super();
    }

    public EmptyCredentialsException(String message) {
        super(message);
    }
}
