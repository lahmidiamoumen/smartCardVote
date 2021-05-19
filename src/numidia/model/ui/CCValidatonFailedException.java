package numidia.model.ui;

/**
 *
 * @author bnazare
 */
public class CCValidatonFailedException extends Exception {

    public CCValidatonFailedException(String message) {
        super(message);
    }

    public CCValidatonFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
