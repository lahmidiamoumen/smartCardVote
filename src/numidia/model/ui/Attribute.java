package numidia.model.ui;

import java.util.Locale;
import java.util.ResourceBundle;

import pt.gov.cartaodecidadao.XMLUserData;

/**
 * 
 * All attributes that will be used both for requests and responses while dealing with the SDK and other bits of the system. the first parameter is an internal
 * name the second the correspondent field on the SDK layer and the third the type of attribute.
 * 
 * @author Ricardo Espírito Santo - Linkare TI
 * 
 */
public enum Attribute {

    NAME("name", XMLUserData.XML_NAME, AttributeType.BASIC_INFORMATION, true),

    GIVEN_NAME("givenName", XMLUserData.XML_GIVEN_NAME, AttributeType.BASIC_INFORMATION, true),

    SURNAME("surName", XMLUserData.XML_SURNAME, AttributeType.BASIC_INFORMATION, true),

    NIC("nic", XMLUserData.XML_NIC, AttributeType.BASIC_INFORMATION, true),

    EXPIRY_DATE("expiryDate", XMLUserData.XML_EXPIRY_DATE, AttributeType.BASIC_INFORMATION, true),

    GENDER("sex", XMLUserData.XML_GENDER, AttributeType.CIVIL, true),

    HEIGHT("height", XMLUserData.XML_HEIGHT, AttributeType.CIVIL, true),

    NATIONALITY("nationality", XMLUserData.XML_NATIONALITY, AttributeType.CIVIL, true),

    DATE_OF_BIRTH("dateOfBirth", XMLUserData.XML_DATE_OF_BIRTH, AttributeType.CIVIL, true),

    GIVEN_NAME_FATHER("givenNameFather", XMLUserData.XML_GIVEN_NAME_FATHER, AttributeType.CIVIL, true),

    SURNAME_FATHER("surnameFather", XMLUserData.XML_SURNAME_FATHER, AttributeType.CIVIL, true),

    GIVEN_NAME_MOTHER("givenNameMother", XMLUserData.XML_GIVEN_NAME_MOTHER, AttributeType.CIVIL, true),

    SURNAME_MOTHER("surnameMother", XMLUserData.XML_SURNAME_MOTHER, AttributeType.CIVIL, true),

    NOTES("notes", XMLUserData.XML_ACCIDENTAL_INDICATIONS, AttributeType.CIVIL, true),

    DOCUMENT_NO("DocumentNo", XMLUserData.XML_DOCUMENT_NO, AttributeType.IDENTIFICATION_NUMBER, true),

    TAX_NO("TaxNo", XMLUserData.XML_TAX_NO, AttributeType.IDENTIFICATION_NUMBER, true),

    SOCIAL_SEC_NO("SocialSecurityNo", XMLUserData.XML_SOCIAL_SECURITY_NO, AttributeType.IDENTIFICATION_NUMBER, true),

    HEALTH_NO("HealthNo", XMLUserData.XML_HEALTH_NO, AttributeType.IDENTIFICATION_NUMBER, true),

    MRZ1("mrz1", XMLUserData.XML_MRZ1, AttributeType.OTHER, true),

    MRZ2("mrz2", XMLUserData.XML_MRZ2, AttributeType.OTHER, true),

    MRZ3("mrz3", XMLUserData.XML_MRZ3, AttributeType.OTHER, true),

    CARD_VERSION("cardVersion", XMLUserData.XML_CARD_VERSION, AttributeType.CARD_VALUE, true),

    CARD_NUMBER_PAN("cardNumberPAN", XMLUserData.XML_CARD_NUMBER_PAN, AttributeType.CARD_VALUE, true),

    ISSUING_DATE("issuingDate", XMLUserData.XML_ISSUING_DATE, AttributeType.CARD_VALUE, true),

    ISSUING_ENTITY("issuingEntity", XMLUserData.XML_ISSUING_ENTITY, AttributeType.CARD_VALUE, true),

    DOCUMENT_TYPE("documentType", XMLUserData.XML_DOCUMENT_TYPE, AttributeType.CARD_VALUE, true),

    LOCAL_OF_REQUEST("localOfRequest", XMLUserData.XML_LOCAL_OF_REQUEST, AttributeType.CARD_VALUE, true),

    VERSION("version", XMLUserData.XML_VERSION, AttributeType.CARD_VALUE, true),

    DISTRICT("district", XMLUserData.XML_DISTRICT, AttributeType.ADDRESS, true),

    MUNICIPALITY("municipality", XMLUserData.XML_MUNICIPALITY, AttributeType.ADDRESS, true),

    CIVIL_PARISH("civilParish", XMLUserData.XML_CIVIL_PARISH, AttributeType.ADDRESS, true),

    ABR_STREET_TYPE("abrStreetType", XMLUserData.XML_ABBR_STREET_TYPE, AttributeType.ADDRESS, true),

    STREET_TYPE("streetType", XMLUserData.XML_STREET_TYPE, AttributeType.ADDRESS, true),

    STREET_NAME("streetName", XMLUserData.XML_STREET_NAME, AttributeType.ADDRESS, true),

    ABR_BUILDING_TYPE("abrBuildingType", XMLUserData.XML_ABBR_BUILDING_TYPE, AttributeType.ADDRESS, true),

    BUILDING_TYPE("buildingType", XMLUserData.XML_BUILDING_TYPE, AttributeType.ADDRESS, true),

    DOOR_NO("doorNo", XMLUserData.XML_DOOR_NO, AttributeType.ADDRESS, true),

    FLOOR("floor", XMLUserData.XML_FLOOR, AttributeType.ADDRESS, true),

    SIDE("side", XMLUserData.XML_SIDE, AttributeType.ADDRESS, true),

    PLACE("place", XMLUserData.XML_PLACE, AttributeType.ADDRESS, true),

    LOCALITY("locality", XMLUserData.XML_LOCALITY, AttributeType.ADDRESS, true),

    ZIP4("zip4", XMLUserData.XML_ZIP4, AttributeType.ADDRESS, true),

    ZIP3("zip3", XMLUserData.XML_ZIP3, AttributeType.ADDRESS, true),

    POSTAL_LOCALITY("postalLocality", XMLUserData.XML_POSTAL_LOCALITY, AttributeType.ADDRESS, true),

    PHOTO("photo", XMLUserData.XML_PHOTO, AttributeType.OTHER, true),

    USER_NOTES("userNotes", XMLUserData.XML_PERSONAL_NOTES, AttributeType.OTHER, true);

    private final String name;

    private final String preetyName;

    private final XMLUserData sdkCorrespondentfield;

    private boolean optional; // this field is meant to be changed so not final =) 

    private final AttributeType type;

    private final static String DEFAULT_LANGUAGE = "pt";

    private final static String DEFAULT_COUNTRY = "PT";

    private final ResourceBundle attributeBundle = ResourceBundle.getBundle("Attributes", new Locale(DEFAULT_LANGUAGE, DEFAULT_COUNTRY));

    private Attribute(final String internalName, final XMLUserData sdkField, final AttributeType attType, final boolean opt) {
	this.name = internalName;
	this.preetyName = attributeBundle.getString(attType.getBundlePrefix() + internalName);
	this.sdkCorrespondentfield = sdkField;
	this.type = attType;
	this.optional = opt;
    }

    public String getName() {
	return name;
    }

    public String getPrettyName() {
	return preetyName;
    }

    public XMLUserData getSDKField() {
	return sdkCorrespondentfield;
    }

    public AttributeType getType() {
	return type;
    }

    public boolean isOptional() {
	return optional;
    }

    public void setOptional(final boolean opt) {
	this.optional = opt;
    }
}
