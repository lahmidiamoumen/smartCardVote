package numidia.model.ui;

/**
 * VERIFIED here means that the PIN was correctly entered and is validated. The opposite being NOT_VERIFIED which means the PIN was invalid or not correctly
 * entered. VERIFYING states the process of verification status this can be useful in interval checking and such.
 * 
 * @author Ricardo Espírito Santo - Linkare TI
 */
public enum PINStatus {

    VERIFIED,

    VERIFYING,

    NOT_VERIFIED
}
