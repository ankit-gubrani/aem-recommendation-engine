package in.co.codebrains.recommendations.engine.exceptions;

public class EmptyIteratorException extends Exception {

    public EmptyIteratorException() {

    }

    public EmptyIteratorException(String message) {
        super(message);
    }
}
