package it.polimi.tiw.excetions;

import java.io.Serial;

public class WrongValuesException extends Exception {

    @Serial
    private static final long serialVersionUID = 6888776588844876395L;

    public WrongValuesException() {
        super();
    }

    public WrongValuesException(String message) {
        super(message);
    }
}
