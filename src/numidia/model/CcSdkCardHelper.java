package numidia.model;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import numidia.model.ui.*;
import pt.gov.cartaodecidadao.PTEID_ByteArray;
import pt.gov.cartaodecidadao.PTEID_Certificate;
import pt.gov.cartaodecidadao.PTEID_Certificates;
import pt.gov.cartaodecidadao.PTEID_EIDCard;
import pt.gov.cartaodecidadao.PTEID_ExNoCardPresent;
import pt.gov.cartaodecidadao.PTEID_ExNoReader;
import pt.gov.cartaodecidadao.PTEID_Exception;
import pt.gov.cartaodecidadao.PTEID_ReaderContext;
import pt.gov.cartaodecidadao.PTEID_ReaderSet;
import pteidlib.PTEID_ADDR;
import pteidlib.PTEID_ID;
import pteidlib.PTEID_PIC;

/**
 * This class creates a few utility methods around entities such as PTEID_EIDCard, PTEID_ADDR, PTEID_ID and PTEID_PIC
 *
 * @author Ricardo Espírito Santo - Linkare TI
 *
 */
public class CcSdkCardHelper {

    private PTEID_EIDCard card;

    private final PINCallback pinCallback;

    private boolean isPinPadCardReader;

    private static final String DEFAULT_COUNTRY = "PT";

    private static final String DEFAULT_LANGUAGE = "pt";

    private final static ResourceBundle bundle = ResourceBundle.getBundle("Applet", new Locale(DEFAULT_LANGUAGE, DEFAULT_COUNTRY));

    /**
     * Initialise this SDK Card helper class with the only required attribute which is a pin callback class. This will ensure that this helper will be able to
     * autonomously deal with whatever PIN or PUK prompts it is meant to show
     *
     * @param pinCallback
     *            the PINCallback object
     */
    public CcSdkCardHelper(final PINCallback pinCallback) {
        this.pinCallback = pinCallback;
    }

    /**
     * Attempt to detect a card and if found assign it to the card object on this class
     *
     * @throws CCError
     *             if a general error (PTEID_Exception) happens
     * @throws CCCardNotFoundException
     *             if no card present either PTEID_ExNoReader or PTEID_ExNoCardPresent
     */
    public void detectCard() throws CCError, CCCardNotFoundException {
        try {
            final PTEID_ReaderSet readerSet = PTEID_ReaderSet.instance();
            final PTEID_ReaderContext readerContext = readerSet.getReader();
            this.isPinPadCardReader = CardReaderUtils.isPinPadReader(readerContext);
            card = readerContext.getEIDCard();

        } catch (PTEID_ExNoReader | PTEID_ExNoCardPresent ex) {
            throw new CCCardNotFoundException(ex);
        } catch (Throwable ex) {
            throw new CCError(ex);
        }
    }

    /**
     * @return the card object if found
     * @throws CCError
     *             if a general error (PTEID_Exception) happens
     * @throws CCCardNotFoundException
     *             if no card present either PTEID_ExNoReader or PTEID_ExNoCardPresent
     */
    public PTEID_EIDCard getCard() throws CCError, CCCardNotFoundException {
        if (card == null) {
            detectCard();
        }
        return card;
    }

    /**
     * A proxy method to release the card on the SDK itself
     *
     * @throws CCError
     */
    public void releaseCard() throws CCError {
        try {
            PTEID_ReaderSet.releaseSDK();
        } catch (PTEID_Exception ex) {
            throw new CCError(ex);
        }
    }

    /**
     * @return true if this is a pinpad and false otherwise
     * @throws CCError
     */
    public boolean areWeReadingPINFromPINPad() throws CCError {
        return isPinPadCardReader;
    }

    /**
     * @return an array of X.509 certificates on the card
     * @throws CCError
     *             if something fails
     */
    public X509Certificate[] getCardCertificates() throws CCError {
        try {
            final PTEID_Certificates certs = card.getCertificates();
            final X509Certificate userCert = toJavaCertificate(certs.getCertFromCard(0));
            final X509Certificate subCACert = toJavaCertificate(certs.getCertFromCard(3));

            return new X509Certificate[] { userCert, subCACert };
        } catch (CertificateException ex) {
            throw new CCError(ex);
        } catch (PTEID_Exception ex) {
            throw new CCError(ex);
        }
    }

    /**
     * Attempts to access a set of attributes knowingly of the rules it is meant to use to access each field. If a particular field needs a particular PIN code
     * than this method will handle it.
     *
     * @param attributes
     *            the set of attributes to be accessed
     * @return true if we were able to access the required given fields and false otherwise
     * @throws CCError
     * @throws PTEID_Exception
     */
    public boolean accessSetOfAttributes(final List<Attribute> attributes) throws CCError, PTEID_Exception {
        boolean accessGrantedOrNotRequired;
        for (final Attribute attribute : attributes) {
            if (attribute != null) {
                final PTEID_PIN PIN = new PTEID_PIN(PINUtils.getAddressPin(card));
                if (attribute.getType().equals(AttributeType.ADDRESS)) {
                    accessGrantedOrNotRequired = pinCallback.verifyPIN(PIN, PIN.getLabel(), bundle.getString("request.address.pin.msg"),
                            bundle.getString("request.address.pin.msgForPINPad"));

                    return accessGrantedOrNotRequired;
                }
            }
        }
        return true;
    }

    /**
     * @return a PTEID_ID
     * @throws CCError
     */
    public PTEID_ID getCitizenId() throws CCError {
        try {
            return new PTEID_M_ID(card.getID());
        } catch (PTEID_Exception ex) {
            throw new CCError(ex);
        }
    }

    /**
     * @return PTEID_ADDR
     * @throws CCError
     */
    public PTEID_ADDR getCitizenAddr() throws CCError {
        try {
            final PTEID_PIN PIN = new PTEID_PIN(PINUtils.getAddressPin(card));
            pinCallback.verifyPIN(PIN, PIN.getLabel(), bundle.getString("request.address.pin.msg"), bundle.getString("request.address.pin.msgForPINPad"));
            return new PTEID_M_ADDR(card.getAddr());
        } catch (PTEID_Exception ex) {
            throw new CCError(ex);
        }
    }

    /**
     * @return a PTEID_PIC
     * @throws CCError
     */
    public PTEID_PIC getCitizenPic() throws CCError {
        try {
            return new PTEID_M_PIC(card.getID());
        } catch (PTEID_Exception ex) {
            throw new CCError(ex);
        }
    }

    /**
     * @return a String representing the person's notes
     * @throws CCError
     */
    public String getCitizenNotes() throws PTEID_Exception {
        return card.readPersonalNotes();
    }

    /**
     * Sign a given data with the SHA-1 algorithm. This process required the Authentication PIN
     *
     * @param inputData
     *            the data to be signed
     * @return the byte[] correspondent to what was given but signed
     *
     * @throws CCError
     */
    public byte[] signData(final byte[] inputData) throws CCError {
        // we shouldn't use a byte[] from parameter instead we should use a copy of the data.
        final byte[] dataToBeSigned = inputData.clone();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            final byte[] digest = md.digest(dataToBeSigned);

            final byte[] SHA1_MAGIC_STRING = new byte[] { 0x30, 0x21, 0x30, 0x09, 0x06, 0x05, 0x2b, 0x0e, 0x03, 0x02, 0x1a, 0x05, 0x00, 0x04, 0x14 };

            final byte[] hashMagicString = SHA1_MAGIC_STRING;

            final byte[] fullDigest = Arrays.copyOf(hashMagicString, hashMagicString.length + digest.length);
            System.arraycopy(digest, 0, fullDigest, hashMagicString.length, digest.length);

            final PTEID_PIN PIN = new PTEID_PIN(PINUtils.getAuthenticationPin(card));
            final boolean OKToSignData = pinCallback.verifyPIN(PIN, PIN.getLabel(), bundle.getString("request.authentication.pin.msg"),
                    bundle.getString("request.authentication.pin.msgForPINPad"));

            if (!OKToSignData) {
                return null;
            }

            final PTEID_ByteArray outputData = card.Sign(new PTEID_ByteArray(fullDigest, fullDigest.length));

            return outputData.GetBytes();
        } catch (PTEID_Exception ex) {
            throw new CCError(ex);
        } catch (NoSuchAlgorithmException ex) {
            throw new CCError(ex);
        }
    }

    /**
     * Convert a PTEID_Certificate to a X.509 Certificate.
     *
     * @param certificate
     *            the certificate we are attempting to convert
     * @return the converted certificate
     *
     * @throws CertificateException
     *             if the X.509 certificate cannot be used or if it can't be generated
     * @throws PTEID_Exception
     *             the we can't extract the data from the given certificate
     */
    private static X509Certificate toJavaCertificate(PTEID_Certificate certificate) throws CertificateException, PTEID_Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream is = new ByteArrayInputStream(certificate.getCertData().GetBytes());
        X509Certificate javaCert = (X509Certificate) cf.generateCertificate(is);

        return javaCert;
    }
}
