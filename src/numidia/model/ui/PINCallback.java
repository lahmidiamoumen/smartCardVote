package numidia.model.ui;

import java.util.Locale;
import java.util.ResourceBundle;


/**
 * Implementors must ensure we have a way to retrieve the PIN code from the PIN callback mechanism so we provide an interface for easier guidance on how to
 * implement that object.
 * 
 * @author Ricardo Espírito Santo - Linkare TI
 * 
 */
public interface PINCallback {

    static final String DEFAULT_COUNTRY = "PT";

    static final String DEFAULT_LANGUAGE = "pt";

    public final static ResourceBundle bundle = ResourceBundle.getBundle("Domain", new Locale(DEFAULT_LANGUAGE, DEFAULT_COUNTRY));

    public static String FINAL_ATTEMPT_MESSAGE = bundle.getString("final.attempt.msg");

    public static String PIN_OK = bundle.getString("pin.ok.msg");

    public static String UNABLE_TO_VERIFY_PIN = bundle.getString("unable.verify.pin.msg");

    public static String PIN_NOT_OK = bundle.getString("pin.not.ok.msg");

    public static String CARD_BLOCKED_TOO_MANY_ATTEMPTS = bundle.getString("card.blocked.too.many.attempts");

    /**
     * Verifies the PIN
     * 
     * @param PIN
     *            the PIN object to be verified
     * @param title
     *            a title to be displayed on the eventual dialog prompting the user
     * @param msg
     *            a message to display to the user on that same dialog
     * @param msgForPINPad
     *            in case the PIN is being requested through a PINPad we show a nice message instead
     */
    public boolean verifyPIN(final PTEID_PIN PIN, final String title, final String msg, final String msgForPINPad);

}
