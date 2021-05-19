/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package numidia.model.ui;

import numidia.model.CCCardNotFoundException;
import numidia.model.CCError;
import numidia.model.CcSdkCardHelper;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.BaseNCodec;
import org.w3c.dom.Document;
import pt.gov.cartaodecidadao.PTEID_CCXML_Doc;
import pt.gov.cartaodecidadao.PTEID_EIDCard;
import pt.gov.cartaodecidadao.PTEID_Exception;
import pt.gov.cartaodecidadao.PTEID_XmlUserRequestedInfo;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Ricardo Espírito Santo - Linkare TI
 *
 */
public class StandaloneAuthBusiness {

    private static final DateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");

    private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    private static final TimeZone DEFAULT_TIMEZONE = TimeZone.getTimeZone("UTC");

    private static final Logger LOGGER = Logger.getLogger(StandaloneAuthBusiness.class.getName());

    private CcSdkCardHelper cardHelper;

    static {
        TIMESTAMP_FORMAT.setTimeZone(DEFAULT_TIMEZONE);
    }

    public StandaloneAuthBusiness(final PINCallback pinCallback) throws CCError {
        cardHelper = new CcSdkCardHelper(pinCallback);
    }

    public void detectCard() throws CCError, CCCardNotFoundException {
        cardHelper.detectCard();
        System.out.println("card detected!");
    }

    public void releaseCard() throws CCError {
        cardHelper.releaseCard();
    }

    public boolean areWeReadingPINFromPINPad() throws CCError {
        return cardHelper.areWeReadingPINFromPINPad();
    }

    public PTEID_EIDCard getCard() throws CCCardNotFoundException, CCError {
        return cardHelper.getCard();
    }

    public X509Certificate[] getCardCertificateChain() throws CCError {
        return cardHelper.getCardCertificates();
    }

    /**
     * Retrieves a response from the SDK itself in its original XML format.
     *
     * @param attributes
     *            the attributes we wish are requesting
     *
     * @return the PTEID_CCXML_Doc object the SDK uses to reply
     * @throws CCCardNotFoundException
     * @throws CCError
     * @throws PTEID_Exception
     * @throws UnknownHostException
     */
    public PTEID_CCXML_Doc getXMLResponseFromSDK(final List<Attribute> attributes, final String targetHost) throws CCError, CCCardNotFoundException,
            PTEID_Exception, UnknownHostException {

        final PTEID_XmlUserRequestedInfo info = new PTEID_XmlUserRequestedInfo(generateTimestamp(), targetHost, InetAddress.getByName(targetHost)
                .getHostAddress());

        // Since the address is the only field we could need a PIN for
        // we ask our helper class to deal with this set of attributes and
        // deal with PIN prompting
        final boolean accessGrantedOrUnecessary = cardHelper.accessSetOfAttributes(attributes);

        System.out.println(accessGrantedOrUnecessary ? " access granted or unecessary" : " no access may have been canceled :/");

        // The user must have cancelled the PIN insertion thus cancelling the whole process.
        // We must bubble this the best we can.
        if (!accessGrantedOrUnecessary) {
            return null;
        }

        for (final Attribute attribute : attributes) {
            if (attribute != null) {
                info.add(attribute.getSDKField());
            }
        }

        return cardHelper.getCard().getXmlCCDoc(info);
    }

    /**
     * Encodes a XML document as a String on the DEFAULT_CHARSET
     *
     * @param xmlResponse
     *            the document to encode
     * @return the String encoded representation of the given document
     * @throws CCError
     *             if the conversion to byteArray fails
     */
    public static String encodeXMLResponse(final Document xmlResponse) throws CCError {
        final byte[] userXmlData = XMLHelper.documentToByteArray(xmlResponse, DEFAULT_CHARSET);
        return new String(userXmlData, DEFAULT_CHARSET);
    }

    /**
     * Signs and encodes the XML response with a requested authentication PIN
     *
     * @param xmlResponse
     *            the response to be encoded and signed
     * @return the base 64 encoded and signed XML String representation
     *
     * @throws CCError
     */
    public String signEncodeXMLResponse(final Document xmlResponse) throws CCError {
        final byte[] userXmlData = XMLHelper.documentToByteArray(xmlResponse, DEFAULT_CHARSET);
        final byte[] response = cardHelper.signData(userXmlData);

        // The signing process might have been cancelled by user input or failed by some other reason
        if (response == null) {
            return null;
        }

        Base64 base64Encoder = new Base64(BaseNCodec.MIME_CHUNK_SIZE);
        return base64Encoder.encodeAsString(response);
    }

    /**
     * Encodes in base 64 the given Certificate
     *
     * @param cert
     *            the certificate to encode
     * @return a String representation of the encoded certificate
     * @throws CCError
     *             if the certification encoding couldn't be completed
     */
    public static String encodeCertificate(final Certificate cert) throws CCError {
        try {
            Base64 base64Encoder = new Base64(BaseNCodec.MIME_CHUNK_SIZE);
            return base64Encoder.encodeAsString(cert.getEncoded());
        } catch (CertificateEncodingException ex) {
            throw new CCError(ex);
        }
    }

    /**
     * @return the timestamp retrieved from our ntp server. This is meant to be used while validating the time validaty of a request / response. If the server
     *         fails to give us a timestamp the local machine's will be used
     */
    private static String generateTimestamp() {
        Date currentTime;
        try {
            currentTime = SntpUtils.getTime(DEFAULT_TIMEZONE);
        } catch (final CCError ex) {
            /*
             * NTP is bound to fail in many circumstances. The fallback is to use local time. Still we log it, for debug.
             */
            LOGGER.log(Level.FINE, "Failed to use the ntp server timestamp. reverting to local machine time", ex);
            currentTime = Calendar.getInstance(DEFAULT_TIMEZONE).getTime();
        }

        return TIMESTAMP_FORMAT.format(currentTime);
    }

    public static boolean validateCertificateChain(Certificate[] certChain) throws GeneralSecurityException, CertPathValidatorException {
        Set<TrustAnchor> anchors = new LinkedHashSet<TrustAnchor>();

        for (X509Certificate trustedCert : getSystemTrustedCertificates()) {
            anchors.add(new TrustAnchor(trustedCert, null));
        }

        PKIXParameters params = new PKIXParameters(anchors);

        // Activate certificate revocation checking
        params.setRevocationEnabled(true);

        // Activate OCSP
        Security.setProperty("ocsp.enable", "true");

        // Activate CRLDP
        System.setProperty("com.sun.security.enableCRLDP", "true");

        // Ensure that the ocsp.responderURL property is not set.
        if (Security.getProperty("ocsp.responderURL") != null) {
            throw new IllegalStateException("The ocsp.responderURL property must not be set");
        }

        CertPathValidator validator = CertPathValidator.getInstance("PKIX");

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        CertPath path = cf.generateCertPath(Arrays.asList(certChain));

        validator.validate(path, params);

        return true;
    }

    private static X509Certificate[] getSystemTrustedCertificates() throws KeyStoreException, NoSuchAlgorithmException {
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init((KeyStore) null);

        X509TrustManager x509tm = null;

        for (TrustManager tm : tmf.getTrustManagers()) {
            if (tm instanceof X509TrustManager) {
                x509tm = (X509TrustManager) tm;
            }
        }

        assert x509tm != null;
        return x509tm.getAcceptedIssuers();
    }
}
