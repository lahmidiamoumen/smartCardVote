package numidia.model;

/**
 *
 * @author bnazare
 */
public class CCCardNotFoundException extends Exception {
    
    private static final String DEFAULT_MESSAGE = "Cartão do Cidadão não Detectado";

    public CCCardNotFoundException() {
        super(DEFAULT_MESSAGE);
    }

    public CCCardNotFoundException(Throwable cause) {
        super(DEFAULT_MESSAGE, cause);
    }
}
