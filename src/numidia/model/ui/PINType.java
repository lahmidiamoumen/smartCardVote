package numidia.model.ui;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Types of PIN are ADDRESS, AUTHENTICATION, SIGNATURE
 * 
 * All the types have a String associated with which is what the card stores them as.
 * 
 * Also there is a PIN Reference which is basically an ID that maps to the correct PINType we are not including that in here since we cannot perform switches on
 * variables and therefore it would be really useless.
 * 
 * @author Ricardo Espírito Santo - Linkare TI
 * 
 */
public enum PINType {

    ADDRESS("morada"),

    SIGNATURE("assinatura"),

    AUTHENTICATION("autenticação");

    private static final String DEFAULT_COUNTRY = "PT";

    private static final String DEFAULT_LANGUAGE = "pt";

    private static final String RESOURCE_BUNDLE_NAME = "Domain";

    private String commonLocalizedName;

    private PINType(final String name) {
	this.commonLocalizedName = name;
    }

    public String getCommonLocalizedName(final String language, final String country) {
	if (language.isEmpty() || country.isEmpty()) {
	    return this.getCommonLocalizedName();
	}
	final ResourceBundle rb = ResourceBundle.getBundle(RESOURCE_BUNDLE_NAME, new Locale(language, country));
	return rb.getString(commonLocalizedName);
    }

    public String getCommonLocalizedName() {
	final ResourceBundle rb = ResourceBundle.getBundle(RESOURCE_BUNDLE_NAME, new Locale(DEFAULT_LANGUAGE, DEFAULT_COUNTRY));
	return rb.getString(commonLocalizedName);
    }

    /**
     * @return the name of this PIN and its localised name within brackets []
     */
    @Override
    public String toString() {
	return this.getCommonLocalizedName();
    }
}
