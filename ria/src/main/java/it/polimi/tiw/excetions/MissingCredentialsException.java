package it.polimi.tiw.excetions;

import java.io.Serial;

public class MissingCredentialsException extends Exception {

    @Serial
    private static final long serialVersionUID = 6888776588844876395L;

    public MissingCredentialsException() {
        super();
    }

    public MissingCredentialsException(String message) {
        super(message);
    }
}
