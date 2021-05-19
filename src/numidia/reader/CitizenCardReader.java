/*
 * Copyright (c) 2018 Alexandre Almeida.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package numidia.reader;

import logging.Loggable;
import logging.PteidCardCallback;
import netscape.javascript.JSException;
import netscape.javascript.JSObject;
import numidia.model.CCError;
import numidia.model.CardHelper;
import numidia.model.ui.*;
import numidia.reader.model.CardData;
import numidia.reader.model.EventData;
import numidia.reader.model.ReaderRef;
import numidia.reader.model.ReadingStatus;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.BaseNCodec;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import pt.gov.cartaodecidadao.*;

import javax.imageio.ImageIO;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class CitizenCardReader implements Loggable, PINCallback {
    private PTEID_EIDCard card;

    private static final DateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");

    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private static final TimeZone DEFAULT_TIMEZONE = TimeZone.getTimeZone("UTC");

    private static final Logger LOGGER = Logger.getLogger(CitizenCardReader.class.getName());

    private static final HashMap<String, ReaderRef> READER_REFERENCE_HANDLER = new HashMap<>();

    private static final String PIC_PATH = "pictures";

    private static boolean libLoaded;

    private final List<CitizenCardEventListener> listeners = new ArrayList<>();

    public CitizenCardReader() {}
    static {
        TIMESTAMP_FORMAT.setTimeZone(DEFAULT_TIMEZONE);
    }

    public Integer getTries() throws PTEID_Exception {
        return card == null ? -1 : card.getPins().getPinByPinRef(PTEID_Pin.AUTH_PIN).getTriesLeft();
    }

    public String getAuthCert() throws PTEID_Exception {
        return  card.getCert(PTEID_CertifType.PTEID_CERTIF_TYPE_AUTHENTICATION).getIssuer().getIssuerName();
    }

    public Boolean getAuthentication() throws PTEID_Exception {
        if( card == null) {
            System.out.println("card variables is null!");
            return false;
        }
        PTEID_Pins pins = card.getPins();
        PTEID_Pin pin = pins.getPinByPinRef(PTEID_Pin.AUTH_PIN);

        //PTEID_ulwrapper triesLeft = new PTEID_ulwrapper(-1);
        System.out.println(card.getAuthentication().getIssuerName());
        System.out.println("Tries left :" + pin.getTriesLeft());

        String notes = "";
        PTEID_ByteArray pb = new PTEID_ByteArray(notes.getBytes(), notes.getBytes().length);
        return card.writePersonalNotes(pb,pin );
    }

    public void init() {
        try {
            System.out.println("init.0..");
            System.out.println(PTEID_ReaderSet.instance().readerCount());
            for (int i = 0; i < PTEID_ReaderSet.instance().readerCount(); i++) {
                System.out.println("Reading card "+i+"...");
                final PTEID_ReaderContext readerContext = PTEID_ReaderSet.instance().getReaderByNum(i);
                readerContext.SetEventCallback(new PteidCardCallback(), null);

                String readerName = readerContext.getName();
                final EventData eventData = new EventData(readerName);

                long handle = readerContext.SetEventCallback((l, statusCode, data) -> {
                    try {

                        card = readerContext.getEIDCard();
                        // card.getCertificates();


                        PTEID_Pins pins = card.getPins();
                        PTEID_Pin pin = pins.getPinByPinRef(PTEID_Pin.AUTH_PIN);

                        //PTEID_ulwrapper triesLeft = new PTEID_ulwrapper(-1);
                        System.out.println(card.getAuthentication().getIssuerName());
                        System.out.println("Tries left :" + pin.getTriesLeft());
//                      MessageDigest md = MessageDigest.getInstance("SHA-1");

                        System.out.println("CAP : " + card.getCert(PTEID_CertifType.PTEID_CERTIF_TYPE_AUTHENTICATION).getIssuer().getIssuerName());;
//                        String notes = "";
//                        PTEID_ByteArray pb = new PTEID_ByteArray(notes.getBytes(), notes.getBytes().length);
//                        boolean bOk = card.writePersonalNotes(pb,pin );
//                        System.out.println("TRies left:" + pin.getTriesLeft() + (bOk ? " vorrect---" : " false---"));
                        //validateCertificateChain(getCardCertificates());

//                        if (pin.verifyPin("4303", triesLeft, true)) {
//
//                            System.out.println("CORRECT PIN");
//                            final PTEID_CCXML_Doc document = getXMLResponseFromSDK();
//
//                            final String XMLResponse = document.getCCXML();
//                            System.out.println("XML Response >> " + XMLResponse);
//                            final Document xmlResponse = XMLHelper.convertXMLStringToDocument(XMLResponse);
//
//                            final byte[] userXmlData = XMLHelper.documentToByteArray(xmlResponse, DEFAULT_CHARSET);
//
//                            final byte[] digest = md.digest(userXmlData);
//
//                            final byte[] hashMagicString = new byte[] { 0x30, 0x21, 0x30, 0x09, 0x06, 0x05, 0x2b, 0x0e, 0x03, 0x02, 0x1a, 0x05, 0x00, 0x04, 0x14 };
//
//                            final byte[] fullDigest = Arrays.copyOf(hashMagicString, hashMagicString.length + digest.length);
//                            System.arraycopy(digest, 0, fullDigest, hashMagicString.length, digest.length);
//                            final PTEID_ByteArray outputData ;
//                            outputData = card.Sign(new PTEID_ByteArray(fullDigest, fullDigest.length), true);
//
//
////                            PTEID_Address  addr =  card.getAddr();
////                            System.out.println("Addr: "+addr.getMunicipality());
//                        }else {
//                            System.out.println("RONG PIN!");
//                        }
                        if (!readerContext.isCardPresent()) {
                            if (statusCode == 18) {
                                sendCardChangedEvent(ReadingStatus.NO_CARD);
                            } else if (statusCode == 546) {
                                sendCardChangedEvent(ReadingStatus.NOT_CC_CARD);
                            }


                            logger().trace("Card not present on reader '{}'. statusCode={}", readerName, statusCode);
                            System.out.println("Card not present on reader '{}'. statusCode={}" + readerName + statusCode);
                            return;
                        }
                        System.out.println("Card present on reader "+ readerName+" statusCode= "  + statusCode);


                        ReaderRef nh = READER_REFERENCE_HANDLER.get(readerName);
                        PTEID_ulwrapper wrapCardID = new PTEID_ulwrapper(nh.getCardID());

                        if (readerContext.isCardChanged(wrapCardID)) {
                            System.out.println("Card isCardChanged on reader "+ readerName+" statusCode = "  + statusCode);

                            logger().trace("Card present on reader '{}'. statusCode={}", readerName, statusCode);
                            sendCardChangedEvent(ReadingStatus.READING);

                            nh.setCardID(wrapCardID.m_long);

                            logger().trace("Card changed on card reader '{}'. cardId={}", readerName, wrapCardID.m_long);
                            CardData citizenCard = read(wrapCardID.m_long, readerContext.getEIDCard());

                            sendCardChangedEvent(ReadingStatus.READ);
                            sendCardReadEvent(citizenCard);

                            if (statusCode == 34) {
                                sendCardChangedEvent(ReadingStatus.READ);
                                sendCardReadEvent(citizenCard);
                            }
                        }
                    } catch (Exception e) {
                        logger().error("Error while processing event callback.", e);
                        System.out.println("erro:"+ e.getMessage());
                        sendCardChangedEvent(ReadingStatus.ERROR);
                    }
                }, eventData);

                ReaderRef nh = new ReaderRef(readerName, handle);
                READER_REFERENCE_HANDLER.put(readerName, nh);
            }
            System.out.println("OUt..");

        } catch (PTEID_Exception e) {
            logger().error("Failed to init citizen card", e);
        }
    }

    public void unblock() throws PTEID_Exception {
        PTEID_Pins pins = card.getPins();
        PTEID_Pin pin = pins.getPinByPinRef(PTEID_Pin.ADDR_PIN);
        PTEID_ulwrapper triesLeft = new PTEID_ulwrapper(-1);
        boolean bOk = false;
        do {
            bOk = pin.unlockPin("","", triesLeft,-1);
            System.out.println(bOk ? "vorrect---" : "false---");
        }while (!bOk);
    }

    public PTEID_CCXML_Doc getXMLResponseFromSDK()  {
        PTEID_ulwrapper triesLeft = new PTEID_ulwrapper(-1);
        System.out.println("triesLeft "+ triesLeft.m_long);
        boolean bool = false;

        try {

            System.out.println("Tries left :" + card.getPins().getPinByPinRef(PTEID_Pin.ADDR_PIN).getTriesLeft());
            bool = card.getPins().getPinByPinRef(PTEID_Pin.ADDR_PIN).verifyPin("0800", triesLeft, true);

        } catch (PTEID_Exception e) {
            e.printStackTrace();
        }
        System.out.println(bool ? "pin is correct" : "pin is not correct");
        PTEID_XmlUserRequestedInfo requestedInfo = new PTEID_XmlUserRequestedInfo();
        return card.getXmlCCDoc(requestedInfo);
    }




    public void release() {
        try {
            PTEID_ReaderSet.releaseSDK();
        } catch (PTEID_Exception e) {
            logger().error("Failed to release citizen card", e);
        }
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
        final byte[] response = signData(userXmlData);

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

    public static boolean validateCertificateChain(Certificate[] certChain) throws GeneralSecurityException {
        Set<TrustAnchor> anchors = new LinkedHashSet<>();

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

    public void createSubmitForm(URL submitUrl, String encodedXMLResponse, String encodedSignature, String encodedCertificate, String encodedCACertificate)
            throws JSException {
//        JSObject document = (JSObject) window.getMember("document");
//        JSObject body = (JSObject) document.getMember("body");
//
//        JSObject redirectForm = (JSObject) document.call("createElement", new Object[] { "form" });
//
//        redirectForm.setMember("name", "redirectForm");
//        redirectForm.setMember("method", "POST");
//        redirectForm.setMember("action", submitUrl.getPath());
//
//        addInputField(document, redirectForm, "userData", encodedXMLResponse);
//        addInputField(document, redirectForm, "signature", encodedSignature);
//        addInputField(document, redirectForm, "certificate", encodedCertificate);
//        if (encodedCACertificate != null)
//            addInputField(document, redirectForm, "caCertificate", encodedCACertificate);
//
//        body.call("appendChild", new Object[] { redirectForm });
//
//        redirectForm.call("submit", null);
    }

    private void addInputField(JSObject parentDocument, JSObject targetForm, String fieldName, String fieldValue) throws JSException {
        JSObject newField = (JSObject) parentDocument.call("createElement", new Object[] { "input" });
        newField.setMember("type", "hidden");
        newField.setMember("name", fieldName);
        newField.setMember("value", fieldValue);

        targetForm.call("appendChild", new Object[] { newField });
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
        } catch (CertificateException | PTEID_Exception ex) {
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
        return  (X509Certificate) cf.generateCertificate(is);
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

            final byte[] hashMagicString = new byte[] { 0x30, 0x21, 0x30, 0x09, 0x06, 0x05, 0x2b, 0x0e, 0x03, 0x02, 0x1a, 0x05, 0x00, 0x04, 0x14 };

            final byte[] fullDigest = Arrays.copyOf(hashMagicString, hashMagicString.length + digest.length);
            System.arraycopy(digest, 0, fullDigest, hashMagicString.length, digest.length);
            PTEID_ulwrapper triesLeft = new PTEID_ulwrapper(-1);

            PTEID_Pins pins = card.getPins();
            PTEID_Pin pin = pins.getPinByPinRef(PTEID_Pin.SIGN_PIN);
            final PTEID_ByteArray outputData ;

            System.out.println("tries left: " + triesLeft);

            if (pin.verifyPin("4303", triesLeft, true)){
                outputData = card.Sign(new PTEID_ByteArray(fullDigest, fullDigest.length),true);
                return outputData.GetBytes();
//                addr =  card.getAddr();
//                String municipio =  addr.getMunicipality();
            }
            else {
                System.out.println("PIN RONG");
            }
            return null;



        } catch (PTEID_Exception | NoSuchAlgorithmException ex) {
            throw new CCError(ex);
        }
    }

    private CardData read(long id, PTEID_EIDCard idCard) {
        CardData ccData = null;
        try {
            PTEID_EId cardData = idCard.getID();

            ccData = new CardData.Builder(id)
                    .setGivenName(cardData.getGivenName())
                    .setSurname(cardData.getSurname())
                    .setGender(cardData.getGender())
                    .setDateOfBirth(cardData.getDateOfBirth())

                    .setCardNumber(cardData.getDocumentNumber())
                    .setCivilianIdNumber(cardData.getCivilianIdNumber())
                    .setTaxNumber(cardData.getTaxNo())
                    .setSocialSecurityNumber(cardData.getSocialSecurityNumber())
                    .setHealthNumbe(cardData.getHealthNumber())

                    .setAccidentalIndications(cardData.getAccidentalIndications())

                    .setValidityBeginDate(cardData.getValidityBeginDate())
                    .setValidityEndDate(cardData.getValidityEndDate())
                    //.setPicture(getPicture(idCard.))
                    .build();

            System.out.println("Card readed. cardData= "+ ccData);

            logger().debug("Card readed. cardData={}", ccData);
        } catch (PTEID_Exception e) {
            logger().error("Failed to read card", e);
        }
        return ccData;
    }

    public byte[] getPicture(long id) {
        try {
            for (String readerName : READER_REFERENCE_HANDLER.keySet()) {
                final PTEID_ReaderContext readerContext = PTEID_ReaderSet.instance().getReaderByName(readerName);

                if (!readerContext.isCardPresent()) {
                    return null;
                }

                ReaderRef nh = READER_REFERENCE_HANDLER.get(readerName);
                if (nh.getCardID() != id) {
                    logger().debug("Card id not match.");
                    continue;
                }
                PTEID_ulwrapper wrapCardID = new PTEID_ulwrapper(nh.getCardID());

                if (readerContext.isCardChanged(wrapCardID)) {
                    return readerContext.getEIDCard().getID().getPhotoObj().getphoto().GetBytes();
                }
                return readerContext.getEIDCard().getID().getPhotoObj().getphoto().GetBytes();
            }
        } catch (Exception e) {
            logger().error("Error while processing event callback.", e);
        }
        return null;
    }

    public ReadingStatus getStatus() {
        try {
            for (String readerName : READER_REFERENCE_HANDLER.keySet()) {
                final PTEID_ReaderContext readerContext = PTEID_ReaderSet.instance().getReaderByName(readerName);

                if (!readerContext.isCardPresent()) {
                    return ReadingStatus.NO_CARD;
                }

                ReaderRef nh = READER_REFERENCE_HANDLER.get(readerName);
                PTEID_ulwrapper wrapCardID = new PTEID_ulwrapper(nh.getCardID());

                if (readerContext.isCardChanged(wrapCardID)) {
                    return ReadingStatus.READING;
                }

                return ReadingStatus.READ;
            }
        } catch (Exception e) {
            logger().error("Error while processing event callback.", e);
            return ReadingStatus.ERROR;
        }

        return ReadingStatus.NO_CARD;
    }

    public CardData getCardData() {
        try {
            for (String readerName : READER_REFERENCE_HANDLER.keySet()) {
                final PTEID_ReaderContext readerContext = PTEID_ReaderSet.instance().getReaderByName(readerName);

                if (!readerContext.isCardPresent()) {
                    return null;
                }

                ReaderRef nh = READER_REFERENCE_HANDLER.get(readerName);
                PTEID_ulwrapper wrapCardID = new PTEID_ulwrapper(nh.getCardID());

                if (readerContext.isCardChanged(wrapCardID)) {
                    return read(nh.getCardID(), readerContext.getEIDCard());
                }

                return read(nh.getCardID(), readerContext.getEIDCard());
            }
        } catch (Exception e) {
            logger().error("Error while processing event callback.", e);
        }

        return null;
    }



    /**
     * Adds the listener.
     *
     * @param toAdd
     *            the to add
     */
    public void addListener(CitizenCardEventListener toAdd) {
        listeners.add(toAdd);
    }

    private void sendCardChangedEvent(ReadingStatus status) {
        for (CitizenCardEventListener listener : listeners) {
            listener.cardChangedEvent(status);
        }
    }

    private void sendCardReadEvent(CardData citizenCardData) {
        for (CitizenCardEventListener listener : listeners) {
            listener.cardReadEvent(citizenCardData);
        }
    }

    private void savePhoto(final byte[] picture, final String photoName) throws IOException {
        InputStream in = new ByteArrayInputStream(picture);
        BufferedImage img = ImageIO.read(in);

        File f = new File(PIC_PATH, photoName + "." + "bmp");
        if (!f.exists()) {
            f.mkdirs();
            f.createNewFile();
        }
        ImageIO.write(img, "bmp", f);
        System.out.println(f);
        ImageIO.read(f);
    }

    public static boolean loadLibrary() throws UnsatisfiedLinkError {
        return ((Boolean) AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            try {
                LoggerFactory.getLogger(CitizenCardReader.class).info("Load libraries from configured path '{}'",
                        System.getProperty("java.library.path"));
                String osName = System.getProperty("os.name");
                if (osName.equals("Mac OS X")) {
                    System.load("/usr/local/lib/libpteidlibj.2.dylib");
                } else if (osName.equals("Linux")) {
                    System.load("/usr/local/lib/libpteidlibj.so");
                } else {
                    System.loadLibrary("pteidlibj");
                }
                libLoaded = true;
            } catch (UnsatisfiedLinkError e) {
                if (!e.getMessage().contains("already loaded")) {
                    LoggerFactory.getLogger(CitizenCardReader.class).error("Cannot load library, check if you had installed Autenticação.gov application.", e);
                }
                libLoaded = false;
            }
            return libLoaded;
        }));
    }

    private static void verifyPin(PTEID_Pin pin, CardHelper.PINCallback pinCallback) throws PTEID_Exception, CCError {
        String pinName = pin.getLabel();
        long triesLeft = 3;

//        if(pin.getTriesLeft() > 0) {
//            if (tokInfo.isProtectedAuthenticationPath()) {
//                session.login(Session.UserType.USER, null);
//            } else {
        do {
            String pinData;
            if(triesLeft == 1) {
                pinData = pinCallback.getPIN(pinName, "esta é a sua última tentativa antes de bloquear o cartão.");
            } else {
                pinData = pinCallback.getPIN(pinName, null);
            }
            PTEID_ulwrapper ulwrapper = new PTEID_ulwrapper(11);
            if(pin.verifyPin(pinData,  ulwrapper, false)) {
                return;
            } else {
                triesLeft = ulwrapper.m_long;
            }
        } while(triesLeft > 0);
//                } while(!tokInfo.isUserPinLocked());

        throw new CCError("Devido a várias tentativas incorrectas, o Cartão foi bloqueado", null);
//            }
//        } else {
//            throw new CCError("O Cartão inserido encontra-se bloqueado", null);
//        }
    }

    @Override
    public boolean verifyPIN(PTEID_PIN PIN, String title, String msg, String msgForPINPad) {
        do {
            if (PIN.isLastAttempt()) {
                LOGGER.log(Level.INFO, FINAL_ATTEMPT_MESSAGE);
                msg = FINAL_ATTEMPT_MESSAGE;
            }

            if (!requestPINCode(PIN, title, msg, msgForPINPad)) {
                return false;
            }

            try {
                if (PIN.verifyPIN()) {
                    LOGGER.log(Level.INFO, PIN_OK);
                    msg = PIN_OK;
                    return true;
                }
            } catch (final PTEID_Exception e) {
                LOGGER.log(Level.INFO, UNABLE_TO_VERIFY_PIN, e);
                return false;
            }
            LOGGER.log(Level.INFO, PIN_NOT_OK);
            msg = PIN_NOT_OK;
        } while (PIN.hasAttemptsLeft());
        msg = CARD_BLOCKED_TOO_MANY_ATTEMPTS;
        LOGGER.log(Level.INFO, CARD_BLOCKED_TOO_MANY_ATTEMPTS);
        return false;
    }

    public boolean requestPINCode(final PTEID_PIN PIN, final String title, final String msg, final String msgForPINPad) {
        String PINCode;
        PINCode = "4303";
//        if (headless) {
//            PINCode = jsHelper.getPINCode(PIN, title, msg, msgForPINPad);
//        } else {
//            PINCode = this.getPINFromApplet(PIN, title, msg, msgForPINPad);
//        }
//
//        // check if the user cancelled the PIN in a headless applet
//        if ( headless) {
//            startAuthentication();
//            return false;
//        }

//        if (PINCode != null && !PINCode.isEmpty()) {
            PIN.setCodeEntered(true);
            PIN.setPINCode(PINCode);
//        }

        return true;
    }
}

