package numidia.model.ui;

import netscape.javascript.JSException;
import netscape.javascript.JSObject;

import java.applet.Applet;
import java.net.URL;

/**
 * A bridge class to allow communication with the Javascript from and to java. This is used only on the authentication applet.
 *
 * @author Ricardo Espírito Santo - Linkare TI
 *
 */
public class JSHelper {

    private static final int FREQUENCY_TO_CHECK_FOR_PIN_INPUT_MS = 200;

    private final JSObject window;

    public final StandaloneAuthApplet applet;

    /**
     * @param targetApplet
     *            the applet we are attempting to communicate with
     */
    public JSHelper(final Applet targetApplet) {
        this.applet = (StandaloneAuthApplet) targetApplet;
        this.window = JSObject.getWindow(targetApplet);
    }

    /**
     * @param message
     *            the message we want to display on the html
     */
    public void showMessage(final String message) {
        window.call("showMessage", new Object[] { message });
    }

    public void showAlertBox(final String message) {
        window.eval("javascript:alert('" + message + "');");
    }

    /**
     * Displays an error message allowing a retry or not based on the second given parameter
     *
     * @param errorMessage
     *            the message to display
     * @param allowRetry
     *            true to allow a retry.
     */
    public void showError(final String errorMessage, final boolean allowRetry, final String retryMessage) {
        window.call("showError", new Object[] { errorMessage, allowRetry, retryMessage });
    }

    public void requestAuthorization(final String requestedAttributes, final String authorizeMessage) {
        window.call("requestAuthorization", new Object[] { requestedAttributes, authorizeMessage });
    }

    public void createSubmitForm(URL submitUrl, String encodedXMLResponse, String encodedSignature, String encodedCertificate, String encodedCACertificate)
            throws JSException {
        JSObject document = (JSObject) window.getMember("document");
        JSObject body = (JSObject) document.getMember("body");

        JSObject redirectForm = (JSObject) document.call("createElement", new Object[] { "form" });

        redirectForm.setMember("name", "redirectForm");
        redirectForm.setMember("method", "POST");
        redirectForm.setMember("action", submitUrl.getPath());

        addInputField(document, redirectForm, "userData", encodedXMLResponse);
        addInputField(document, redirectForm, "signature", encodedSignature);
        addInputField(document, redirectForm, "certificate", encodedCertificate);
        if (encodedCACertificate != null)
            addInputField(document, redirectForm, "caCertificate", encodedCACertificate);

        body.call("appendChild", new Object[] { redirectForm });

        redirectForm.call("submit", null);
    }

    private void addInputField(JSObject parentDocument, JSObject targetForm, String fieldName, String fieldValue) throws JSException {
        JSObject newField = (JSObject) parentDocument.call("createElement", new Object[] { "input" });
        newField.setMember("type", "hidden");
        newField.setMember("name", fieldName);
        newField.setMember("value", fieldValue);

        targetForm.call("appendChild", new Object[] { newField });
    }


    public String getPINCode(final PTEID_PIN PIN, final String title, final String msg, final String msgForPINPad) {
        window.call("askForPIN", new Object[] { PIN, title, msg, msgForPINPad });

        while (!PIN.isCodeEntered()) {
            try {
                Thread.sleep(FREQUENCY_TO_CHECK_FOR_PIN_INPUT_MS);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }

        return PIN.getPINCode();
    }

}
