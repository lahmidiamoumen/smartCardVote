package numidia.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import numidia.Main;
import numidia.model.CCError;
import numidia.model.ui.*;
import numidia.model.ui.events.CardAndReaderStatusHandler;
import numidia.model.ui.events.CardStatusHandler;
import numidia.model.ui.events.ReaderStatusHandler;
import numidia.reader.CitizenCardEventListener;
import numidia.reader.CitizenCardReader;
import numidia.reader.model.CardData;
import numidia.reader.model.ReadingStatus;
import numidia.reader.model.pteid;
import org.w3c.dom.Document;
import pt.gov.cartaodecidadao.PTEID_CCXML_Doc;
import pt.gov.cartaodecidadao.PTEID_Exception;
import pt.gov.cartaodecidadao.PTEID_Pin;
import pt.gov.cartaodecidadao.PTEID_ReaderSet;
import pteidlib.PteidException;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Home implements Initializable, CitizenCardEventListener, CardStatusHandler, ReaderStatusHandler {

    private CitizenCardReader citizenCard;
    private  Button btn;
    private CardData cardData;
    private URL submitURL;
    private static final Logger LOGGER = Logger.getLogger(Home.class.getName());



    @FXML
    Button close,minimise,maximise;
    public static BorderPane searchPane;
    @FXML
    public BorderPane holdPane;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            new CardAndReaderStatusHandler(PTEID_ReaderSet.instance(), this, this);
        } catch (PTEID_Exception e) {
            e.printStackTrace();
        }

        citizenCard = new CitizenCardReader();
        citizenCard.init();
        citizenCard.addListener(this);
//        if (!submitURL.getProtocol().equalsIgnoreCase("https")) {
//            try {
//                throw new CCError("NON_HTTPS_ERROR_MSG", null);
//            } catch (CCError ccError) {
//                //handleError(ccError);
//                ccError.printStackTrace();
//            }
//        }
//        try {
//            pteid.Init(null);
//            pteid.SetSODChecking(true);
//        } catch (PteidException e) {
//            e.printStackTrace();
//        }
        try {
            searchPane = FXMLLoader.load(getClass().getResource("../view/test.fxml"));
        } catch (IOException ex) {
            Logger.getLogger(Home.class.getName()).log(Level.SEVERE, null, ex);
        }






        //eventSearchPane();
        close.setOnAction(event -> {
            citizenCard.release();
            Platform.exit();
            System.exit(0);
        });

        Main.primaryStage.setOnCloseRequest( t ->{
            citizenCard.release();
            Platform.exit();
            System.exit(0);
        });

        minimise.setOnAction(e -> Main.primaryStage.setIconified(true));
        maximise.setOnAction(e -> Main.primaryStage.setMaximized(!Main.primaryStage.isMaximized()));
    }

    public void setNode() {
        BorderPane node = new BorderPane(searchPane);
        holdPane.getChildren().clear();
        holdPane.setTop(node.getTop());
        holdPane.setBottom(node.getBottom());
        holdPane.setLeft(node.getLeft());
        holdPane.setRight(node.getRight());
        holdPane.setCenter(node.getCenter());
        eventSearchPane();
    }

    void eventSearchPane(){
        VBox vBox0= (VBox) searchPane.getCenter();

        HBox hBox= (HBox) vBox0.getChildren().get(0);
        HBox hBox2= (HBox) vBox0.getChildren().get(1);

        ImageView imageView = (ImageView) hBox.getChildren().get(0);
        Image img = new Image(new ByteArrayInputStream(citizenCard.getPicture(cardData.getId())));
        imageView.prefHeight(250);
        imageView.prefWidth(250);
        imageView.setImage(img);
        VBox vBox = (VBox) hBox.getChildren().get(1);

        Pane cardNumberPan = (Pane) vBox.getChildren().get(0);
        Label cardNumber = (Label) cardNumberPan.getChildren().get(1);
        cardNumber.setText(cardData.getCardNumber());

        Pane civilianIdNumberPan = (Pane) vBox.getChildren().get(1);
        Label civilianIdNumber = (Label) civilianIdNumberPan.getChildren().get(1);
        civilianIdNumber.setText(cardData.getCivilianIdNumber());

        Pane givenNamePan = (Pane) vBox.getChildren().get(2);
        Label givenName = (Label) givenNamePan.getChildren().get(1);
        givenName.setText(cardData.getGivenName());

        Pane surnamePan = (Pane) vBox.getChildren().get(3);
        Label surname = (Label) surnamePan.getChildren().get(1);
        surname.setText(cardData.getSurname());

        Pane dateOfBirthPan = (Pane) vBox.getChildren().get(4);
        Label dateOfBirth = (Label) dateOfBirthPan.getChildren().get(1);
        dateOfBirth.setText(cardData.getDateOfBirth());


        btn = (Button) hBox2.getChildren().get(1);
        btn.setOnAction(event -> startAuthentication());


    }

    public void startAuthentication() {
        //jsHelper.showMessage(DETECTING_CARD_MSG);
        System.out.println("Start authentication...");
        try {
            if(citizenCard.getAuthentication()){
                btn.setDisable(true);
            }
        } catch (PTEID_Exception e) {
            e.printStackTrace();
        }
//        AccessController.doPrivileged((PrivilegedAction<String>) () -> {
//            new AuthorizationWorker().execute();
//            return null;
//        });
    }


    @Override
    public void cardChangedEvent(ReadingStatus status) {

        System.out.println("cardChangedEvent "+status);
        if (ReadingStatus.READ.equals(status)) {
            cardData = citizenCard.getCardData();
            Platform.runLater(this::setNode);
            System.out.println("cardChangedEvent2 :"+ cardData.getCardNumber());
        }
    }

    @Override
    public void cardReadEvent(CardData citizenCardData) {
        System.out.println("cardReadEvent "+citizenCardData);
    }

    @Override
    public void onCardLost() {
        System.out.println("card was removed!!");
    }

    @Override
    public void onCardReconnect() {
        System.out.println("Re-reading the card!");

        //new StandaloneAuthPanel.DetectionWorker().execute();
    }

    @Override
    public void onReaderReconnect() {
        System.out.println("The reader is back on");

        //new StandaloneAuthPanel.DetectionWorker().execute();
    }

    @Override
    public void onReaderLost() {
        System.out.println("The reader is lost");
    }

    /**
     * This class deals with the authorization work. Here is where the auth is done
     *
     * @author Ricardo Espírito Santo - Linkare TI
     *
     */
    private class AuthorizationWorker extends SwingWorker<Void, Void> {

        private AuthorizationWorker() {}

        @Override
        public Void doInBackground() {
            try {
                System.out.println("On Background...");
                final PTEID_CCXML_Doc document = citizenCard.getXMLResponseFromSDK();

                if (document == null) { // something went wrong while retrieving the response from the XML - this could be a simple PIN cancellation
                    this.cancel(true);
                    return null;
                }

                final String XMLResponse = document.getCCXML();
                System.out.println("XML Response >> " + XMLResponse);


                final Document xmlResponse = XMLHelper.convertXMLStringToDocument(XMLResponse);

                final String encodedXMLResponse = StandaloneAuthBusiness.encodeXMLResponse(xmlResponse);

                System.out.println("XML Encoded "+encodedXMLResponse);

                // The Authentication PIN will be requested here since we need to sign our Response
                final String encodedSignature = citizenCard.signEncodeXMLResponse(xmlResponse);

                if (encodedSignature == null) { // something went wrong while signing the response - this could be a simple PIN cancellation
                    this.cancel(true);
                    return null;
                }

                final X509Certificate[] cardCertChain = citizenCard.getCardCertificates();

                final String encodedCertificate = StandaloneAuthBusiness.encodeCertificate(cardCertChain[0]);

                String encodedCACertificate = null; // some test cards don't have a CA certificate... the user certificate is self-signed instead
                if (cardCertChain.length > 1) {
                    encodedCACertificate = StandaloneAuthBusiness.encodeCertificate(cardCertChain[1]);
                }

//                if (!headless) {
//                    showCard("redirectingCard");
//                } else {
//                    jsHelper.showMessage(REDIRECTING_MSG);
//                }

                redirectBrowser(encodedXMLResponse, encodedSignature, encodedCertificate, encodedCACertificate);

            } catch (CCError err) {
                citizenCard.release();
                System.out.println("Err card released!... "+err.getMessage());

                //handleError(err);
            } catch (Throwable ex) {
                System.out.println("Throwable err  "+ex.getStackTrace());


                //handleThrowable(ex);
            }

            return null;
        }


    }
    private void redirectBrowser(String encodedXMLResponse, String encodedSignature, String encodedCertificate, String encodedCACertificate) {
        if (citizenCard != null) {
            System.out.println("Redirect Data:");
            System.out.println("Submit URL: " + submitURL);
            System.out.println("User Data: " + encodedXMLResponse);
            System.out.println("Signature: " + encodedSignature);
            System.out.println("Certificate: " + encodedCertificate);
            System.out.println("CA Certificate: " + encodedCACertificate);
            citizenCard.createSubmitForm(submitURL, encodedXMLResponse, encodedSignature, encodedCertificate, encodedCACertificate);
        } else {
            System.out.println("Redirect Data:");
            System.out.println("Submit URL: " + submitURL);
            System.out.println("User Data: " + encodedXMLResponse);
            System.out.println("Signature: " + encodedSignature);
            System.out.println("Certificate: " + encodedCertificate);
            System.out.println("CA Certificate: " + encodedCACertificate);
        }
    }

    public static class CustomPINCallback implements PINCallback {


        @Override
        public boolean verifyPIN(final PTEID_PIN PIN, final String title, String msg, final String msgForPINPad) {
            do {
                if (PIN.isLastAttempt()) {
                    LOGGER.log(Level.INFO, FINAL_ATTEMPT_MESSAGE);
                    msg = FINAL_ATTEMPT_MESSAGE;
                    System.out.println("Verify pin " + msg);
                }

                String PINCode = "4303";

                PIN.setCodeEntered(true);
                PIN.setPINCode(PINCode);

                try {
                    if (PIN.verifyPIN()) {
                        System.out.println("PIN Verified");
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

    }
}
