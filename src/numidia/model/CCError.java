package numidia.model;

/**
 *
 * @author bnazare
 */
public class CCError extends Exception {
    
    private final boolean justWraps;

    public CCError(String message, Throwable cause) {
        super(message, cause);
        this.justWraps = false;
    }

    public CCError(Throwable cause) {
        super(cause);
        this.justWraps = true;
    }
    
    public boolean justWraps() {
        return justWraps;
    }
}
