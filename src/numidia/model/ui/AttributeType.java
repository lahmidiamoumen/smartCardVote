package numidia.model.ui;

/**
 * The possible types of fields according to where they appear on the ccpt.xml file.
 * 
 * Their respective prefix in the bundle resource file (Attributes.properties) is also attached to each enum.
 * 
 * @author Ricardo Espírito Santo - Linkare TI
 * 
 */
public enum AttributeType {

    BASIC_INFORMATION("basicInformation"),

    CIVIL("civilFields"),

    IDENTIFICATION_NUMBER("identificationNumbersFields"),

    CARD_VALUE("cardValuesFields"),

    ADDRESS("addressFields"),

    OTHER("");

    private String bundlePrefix;

    AttributeType(final String prefix) {
	this.bundlePrefix = prefix;
    }

    public String getBundlePrefix() {
	return bundlePrefix.length() > 0 ? bundlePrefix + "." : "";
    }

}
