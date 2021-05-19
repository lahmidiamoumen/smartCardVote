/*
 * StandaloneAuthPanel.java
 *
 * Created on 30/Mar/2011, 17:04:57
 */
package numidia.model.ui;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.font.TextAttribute;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import numidia.model.CCCardNotFoundException;
import numidia.model.CCError;
import numidia.model.ui.events.CardAndReaderStatusHandler;
import numidia.model.ui.events.CardStatusHandler;
import numidia.model.ui.events.ReaderStatusHandler;
import org.w3c.dom.Document;

import pt.gov.cartaodecidadao.PTEID_CCXML_Doc;
import pt.gov.cartaodecidadao.PTEID_Exception;
import pt.gov.cartaodecidadao.PTEID_ReaderSet;

/**
 * @author Ricardo Espírito Santo - Linkare TI
 *
 */
public class StandaloneAuthPanel extends JPanel implements CardStatusHandler, ReaderStatusHandler {

    private static final long serialVersionUID = -4979181658015212196L;

    private static final String DEFAULT_COUNTRY = "PT";

    private static final String DEFAULT_LANGUAGE = "pt";

    private ResourceBundle bundle = ResourceBundle.getBundle("Applet", new Locale(DEFAULT_LANGUAGE, DEFAULT_COUNTRY));

    private static final Logger LOGGER = Logger.getLogger(StandaloneAuthPanel.class.getName());

    private static String CARD_RECONNECTED;

    private static String CARD_LOST;

    private static String READER_LOST;

    private static String READER_RECONNECTED;

    private static String INTERNAL_ERROR_MSG;

    private static String CARD_COMM_ERROR_MSG;

    private static String NON_HTTPS_ERROR_MSG;

    private static String READING_CARD_MSG;

    private static String DETECTING_CARD_MSG;

    private static String REDIRECTING_MSG;

    private StandaloneAuthBusiness appletBusiness;

    private List<Attribute> attrList;

    private URL submitURL;

    private boolean headless;

    private JSHelper jsHelper;

    private CustomPINCallback pinCallback;

    public StandaloneAuthPanel() {
        INTERNAL_ERROR_MSG = bundle.getString("internalError");
        CARD_COMM_ERROR_MSG = bundle.getString("comCardError");
        NON_HTTPS_ERROR_MSG = bundle.getString("unsecureURL");
        READING_CARD_MSG = bundle.getString("readingCard");
        DETECTING_CARD_MSG = bundle.getString("detectingCard");
        REDIRECTING_MSG = bundle.getString("redirecting");
        CARD_LOST = bundle.getString("card.lost");
        CARD_RECONNECTED = bundle.getString("card.reconnected");
        READER_LOST = bundle.getString("reader.lost");
        READER_RECONNECTED = bundle.getString("reader.reconnected");
        initComponents();
    }

    /**
     * We just ensure we release the card and leave
     */
    public void stop() {
        try {
            appletBusiness.releaseCard();
        } catch (CCError ex) {
            handleError(ex);
        }
    }

    /**
     * We start by verifying if we are communicating in HTTPS then assign a PIN callback mechanism, instantiate the jsHelper class to get a javascript handler
     * and show a idle message afterwards
     */
    public void init() {
        try {
            if (!submitURL.getProtocol().equalsIgnoreCase("https")) {
                throw new CCError(NON_HTTPS_ERROR_MSG, null);
            }

            pinCallback = new CustomPINCallback();
            appletBusiness = new StandaloneAuthBusiness(pinCallback);

            new CardAndReaderStatusHandler(PTEID_ReaderSet.instance(), this, this);

            if (getTopLevelAncestor() instanceof Applet) {
                jsHelper = new JSHelper((Applet) getTopLevelAncestor());
            }

            showCard("idleCard");
        } catch (final CCError ex) {
            handleError(ex);
        } catch (final SecurityException e) {
            e.printStackTrace();
        } catch (PTEID_Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCardLost() {
        System.out.println("card was removed!!");
        jsHelper.showAlertBox(CARD_LOST);
    }

    @Override
    public void onCardReconnect() {
        System.out.println("Re-reading the card!");
        jsHelper.showAlertBox(CARD_RECONNECTED);
        showCard("detectingCardCard");
        new DetectionWorker().execute();
    }

    @Override
    public void onReaderReconnect() {
        System.out.println("The reader is back on");
        jsHelper.showAlertBox(READER_RECONNECTED);
        showCard("detectingCardCard");
        new DetectionWorker().execute();
    }

    @Override
    public void onReaderLost() {
        System.out.println("The reader is lost");
        jsHelper.showAlertBox(READER_LOST);
    }

    private void showCard(final String cardName) {
        final CardLayout cl = (CardLayout) foregroundPanel.getLayout();
        if (headless) {
            cl.show(foregroundPanel, "h_" + cardName);
        } else {
            cl.show(foregroundPanel, cardName);
        }
    }

    /**
     * Handle Card not found errors
     *
     * @param err
     *            the exception
     */
    private void handleError(final CCCardNotFoundException err) {
        LOGGER.log(Level.SEVERE, "Card not found", err);

        final String tryAgainMessage = bundle.getString("handleError.try.again.msg");
        innerDisplayErrorMessage(err.getMessage(), true, tryAgainMessage);
    }

    /**
     * Handle generic card errors
     *
     * @param err
     *            the exception
     */
    private void handleError(final CCError err) {
        LOGGER.log(Level.SEVERE, "Generic error", err);

        String message;
        if (err.justWraps()) {
            if (err.getCause() instanceof PTEID_Exception) {
                message = CARD_COMM_ERROR_MSG;
            } else {
                message = INTERNAL_ERROR_MSG;
            }
        } else {
            message = err.getMessage();
        }

        final String retryMessage = bundle.getString("handleError.try.again.msg");

        innerDisplayErrorMessage(message, false, retryMessage);
    }

    /**
     * Handling the mother of all exceptions
     *
     * @param t
     *            the exception
     */
    private void handleThrowable(final Throwable t) {
        LOGGER.log(Level.SEVERE, null, t);

        innerDisplayErrorMessage(INTERNAL_ERROR_MSG, false, "");
    }

    /**
     * A utility method to display error messages
     *
     * @param msg
     *            the error message we wish to present to the user
     * @param allowRetry
     *            if it allows a retry or not
     */
    private void innerDisplayErrorMessage(final String msg, final boolean allowRetry, final String retryMessage) {

        if (!headless) {
            if (allowRetry) {
                showCard("cardMissingCard");
            } else {
                errorTextPane.setText(msg);
                showCard("errorCard");
            }
        } else {
            jsHelper.showError(msg, allowRetry, retryMessage);
        }
    }

    private void redirectBrowser(String encodedXMLResponse, String encodedSignature, String encodedCertificate, String encodedCACertificate) {
        if (jsHelper != null) {
            jsHelper.createSubmitForm(submitURL, encodedXMLResponse, encodedSignature, encodedCertificate, encodedCACertificate);
        } else {
            System.out.println("Redirect Data:");
            System.out.println("Submit URL: " + submitURL);
            System.out.println("User Data: " + encodedXMLResponse);
            System.out.println("Signature: " + encodedSignature);
            System.out.println("Certificate: " + encodedCertificate);
            System.out.println("CA Certificate: " + encodedCACertificate);
        }
    }

    private void initComponents() {
        layeringPane = new JLayeredPane();
        foregroundPanel = new JPanel();
        loadingPanel = new JPanel();
        loadingAppletLabel = new JLabel();
        idlePanel = new JPanel();
        jPanel9 = new JPanel();
        detectCardButton = new JButton();
        jPanel7 = new JPanel();
        detectingCardPanel = new JPanel();
        jLabel5 = new JLabel();
        jPanel4 = new JPanel();
        cardDetectedPanel = new JPanel();
        jScrollPane1 = new JScrollPane();
        attributesPanel = new JPanel();
        dataToRetrieveLabel = new JLabel();
        jPanel10 = new JPanel();
        authorizeButton = new JButton();
        rejectButton = new JButton();
        cardMissingPanel = new JPanel();
        cardNotDetectedTextPane = new JTextPane();
        jPanel12 = new JPanel();
        jButton4 = new JButton();
        jPanel13 = new JPanel();
        jPanel14 = new JPanel();
        ccTitleLabel = new JLabel();
        cardInUsePanel = new JPanel();
        jLabel6 = new JLabel();
        jPanel8 = new JPanel();
        redirectingPanel = new JPanel();
        jLabel4 = new JLabel();
        jPanel11 = new JPanel();
        errorPanel = new JPanel();
        jPanel15 = new JPanel();
        ccTitleLabel2 = new JLabel();
        jPanel1 = new JPanel();
        appErrorMsgTextPane = new JTextPane();
        appErrorHeaderLabel = new JLabel();
        errorTextPane = new JTextPane();
        h_idlePanel = new JPanel();
        jLabel8 = new JLabel();
        pinRequestPanel = new JPanel();
        PINInsertionPanel = new JPanel();
        okButton = new JButton();
        cancelButton = new JButton();
        jLabel2 = new JLabel();
        PINInputField = new JPasswordField();
        jScrollPane3 = new JScrollPane();
        warningTextPane = new JTextPane();
        backgroundPanel = new JPanel();
        jPanel3 = new JPanel();
        jLabel9 = new JLabel();
        jPanel2 = new JPanel();

        setPreferredSize(new Dimension(180, 180));
        setLayout(new BorderLayout());

        foregroundPanel.setOpaque(false);
        foregroundPanel.setPreferredSize(new Dimension(180, 180));
        foregroundPanel.setLayout(new CardLayout());

        loadingPanel.setOpaque(false);
        loadingPanel.setLayout(null);

        loadingAppletLabel.setHorizontalAlignment(SwingConstants.CENTER);
        java.util.ResourceBundle bundle = ResourceBundle.getBundle("Applet", new Locale(DEFAULT_LANGUAGE, DEFAULT_COUNTRY));
        loadingAppletLabel.setText(bundle.getString("loading_applet"));
        loadingPanel.add(loadingAppletLabel);
        loadingAppletLabel.setBounds(20, 139, 117, 15);

        foregroundPanel.add(loadingPanel, "loadingCard");

        idlePanel.setOpaque(false);
        idlePanel.setLayout(null);

        jPanel9.setPreferredSize(new java.awt.Dimension(170, 36));
        jPanel9.setLayout(null);

        detectCardButton.setText(bundle.getString("detect_card"));
        detectCardButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                detectCardActionPerformed(evt);
            }
        });
        jPanel9.add(detectCardButton);
        detectCardButton.setBounds(33, 8, 104, 24);

        idlePanel.add(jPanel9);
        jPanel9.setBounds(5, 139, 170, 36);

        jPanel7.setName("cartaoCursor");
        jPanel7.setPreferredSize(new java.awt.Dimension(120, 101));

        GroupLayout jPanel7Layout = new GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(jPanel7Layout.createParallelGroup(Alignment.LEADING).addGap(0, 120, Short.MAX_VALUE));
        jPanel7Layout.setVerticalGroup(jPanel7Layout.createParallelGroup(Alignment.LEADING).addGap(0, 101, Short.MAX_VALUE));

        idlePanel.add(jPanel7);
        jPanel7.setBounds(40, 30, 120, 101);

        foregroundPanel.add(idlePanel, "idleCard");

        detectingCardPanel.setOpaque(false);
        detectingCardPanel.setLayout(null);

        jLabel5.setHorizontalAlignment(SwingConstants.CENTER);
        jLabel5.setText(bundle.getString("detecting_card"));
        detectingCardPanel.add(jLabel5);
        jLabel5.setBounds(19, 139, 110, 15);

        jPanel4.setName("cartaoAmpulheta");

        GroupLayout jPanel4Layout = new GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(jPanel4Layout.createParallelGroup(Alignment.LEADING).addGap(0, 120, Short.MAX_VALUE));
        jPanel4Layout.setVerticalGroup(jPanel4Layout.createParallelGroup(Alignment.LEADING).addGap(0, 101, Short.MAX_VALUE));

        detectingCardPanel.add(jPanel4);
        jPanel4.setBounds(40, 30, 120, 101);

        foregroundPanel.add(detectingCardPanel, "detectingCardCard");

        cardDetectedPanel.setOpaque(false);
        cardDetectedPanel.setLayout(null);

        jScrollPane1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setOpaque(false);
        jScrollPane1.getViewport().setOpaque(false);
        jScrollPane1.setPreferredSize(new Dimension(170, 109));

        attributesPanel.setOpaque(false);
        attributesPanel.setLayout(new BoxLayout(attributesPanel, BoxLayout.Y_AXIS));

        dataToRetrieveLabel.setText(bundle.getString("data_to_retrieve"));
        dataToRetrieveLabel.setName("strongLabel");
        attributesPanel.add(dataToRetrieveLabel);

        jScrollPane1.setViewportView(attributesPanel);

        cardDetectedPanel.add(jScrollPane1);
        jScrollPane1.setBounds(5, 30, 170, 109);

        jPanel10.setPreferredSize(new java.awt.Dimension(170, 36));
        jPanel10.setLayout(null);

        authorizeButton.setText(bundle.getString("authorize"));
        authorizeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                authorizeActionPerformed(evt);
            }
        });
        jPanel10.add(authorizeButton);
        authorizeButton.setBounds(6, 8, 75, 24);

        rejectButton.setText(bundle.getString("reject"));
        rejectButton.setEnabled(areAnyAttributesSelected());
        rejectButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                resetAllAtributes();
            }
        });
        jPanel10.add(rejectButton);
        rejectButton.setBounds(93, 8, 71, 24);

        cardDetectedPanel.add(jPanel10);
        jPanel10.setBounds(5, 139, 170, 36);

        foregroundPanel.add(cardDetectedPanel, "cardDetectedCard");

        cardMissingPanel.setOpaque(false);
        cardMissingPanel.setLayout(null);

        cardNotDetectedTextPane.setContentType("text/html");
        cardNotDetectedTextPane.setEditable(false);
        cardNotDetectedTextPane.setText(bundle.getString("card_not_detected"));
        cardNotDetectedTextPane.setName("errorTextPane");
        cardMissingPanel.add(cardNotDetectedTextPane);
        cardNotDetectedTextPane.setBounds(22, 92, 136, 25);

        jPanel12.setPreferredSize(new java.awt.Dimension(170, 36));
        jPanel12.setLayout(null);

        jButton4.setText(bundle.getString("try_again"));
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                retryDetectCardActionPerformed(evt);
            }
        });
        jPanel12.add(jButton4);
        jButton4.setBounds(33, 8, 104, 24);

        cardMissingPanel.add(jPanel12);
        jPanel12.setBounds(5, 139, 170, 36);

        jPanel13.setName("cartaoErro");
        jPanel13.setPreferredSize(new java.awt.Dimension(120, 101));

        GroupLayout jPanel13Layout = new GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(jPanel13Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGap(0, 120, Short.MAX_VALUE));
        jPanel13Layout.setVerticalGroup(jPanel13Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGap(0, 101, Short.MAX_VALUE));

        cardMissingPanel.add(jPanel13);
        jPanel13.setBounds(40, 30, 120, 101);

        jPanel14.setName("errorTopPanel");
        jPanel14.setPreferredSize(new java.awt.Dimension(180, 26));
        jPanel14.setLayout(null);

        ccTitleLabel.setText(bundle.getString("citizen_card"));
        ccTitleLabel.setName("titleLabel");
        jPanel14.add(ccTitleLabel);
        ccTitleLabel.setBounds(6, 3, 115, 15);

        cardMissingPanel.add(jPanel14);
        jPanel14.setBounds(0, 0, 180, 26);

        foregroundPanel.add(cardMissingPanel, "cardMissingCard");

        cardInUsePanel.setOpaque(false);
        cardInUsePanel.setLayout(null);

        jLabel6.setHorizontalAlignment(SwingConstants.CENTER);
        jLabel6.setText(bundle.getString("reading_card"));
        cardInUsePanel.add(jLabel6);
        jLabel6.setBounds(40, 139, 83, 15);

        jPanel8.setName("cartaoComunicacao");

        GroupLayout jPanel8Layout = new GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(jPanel8Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGap(0, 120, Short.MAX_VALUE));
        jPanel8Layout.setVerticalGroup(jPanel8Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGap(0, 101, Short.MAX_VALUE));

        cardInUsePanel.add(jPanel8);
        jPanel8.setBounds(40, 30, 120, 101);

        foregroundPanel.add(cardInUsePanel, "cardInUseCard");

        redirectingPanel.setOpaque(false);
        redirectingPanel.setLayout(null);

        jLabel4.setHorizontalAlignment(SwingConstants.CENTER);
        jLabel4.setText(bundle.getString("redirecting"));
        jLabel4.setHorizontalTextPosition(SwingConstants.CENTER);
        redirectingPanel.add(jLabel4);
        jLabel4.setBounds(28, 139, 102, 15);

        jPanel11.setName("cartaoRedireccao");

        GroupLayout jPanel11Layout = new GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(jPanel11Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGap(0, 120, Short.MAX_VALUE));
        jPanel11Layout.setVerticalGroup(jPanel11Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGap(0, 101, Short.MAX_VALUE));

        redirectingPanel.add(jPanel11);
        jPanel11.setBounds(40, 30, 120, 101);

        foregroundPanel.add(redirectingPanel, "redirectingCard");

        errorPanel.setOpaque(false);
        errorPanel.setLayout(null);

        jPanel15.setName("errorTopPanel");
        jPanel15.setPreferredSize(new java.awt.Dimension(180, 26));
        jPanel15.setLayout(null);

        ccTitleLabel2.setText(bundle.getString("citizen_card"));
        ccTitleLabel2.setName("titleLabel");
        jPanel15.add(ccTitleLabel2);
        ccTitleLabel2.setBounds(6, 3, 115, 15);

        errorPanel.add(jPanel15);
        jPanel15.setBounds(0, 0, 0, 0);

        jPanel1.setName("backgroundErrorPanel");
        jPanel1.setPreferredSize(new java.awt.Dimension(170, 145));
        jPanel1.setLayout(null);

        appErrorMsgTextPane.setContentType("text/html");
        appErrorMsgTextPane.setEditable(false);
        appErrorMsgTextPane.setText(bundle.getString("application_error_msg"));
        appErrorMsgTextPane.setName("errorTextPane");
        appErrorMsgTextPane.setOpaque(false);
        jPanel1.add(appErrorMsgTextPane);
        appErrorMsgTextPane.setBounds(13, 31, 144, 60);

        appErrorHeaderLabel.setText(bundle.getString("application_error_header_msg"));
        appErrorHeaderLabel.setName("hugeLabel");
        jPanel1.add(appErrorHeaderLabel);
        appErrorHeaderLabel.setBounds(13, 17, 140, 15);

        errorTextPane.setContentType("text/html");
        errorTextPane.setEditable(false);
        errorTextPane.setText(bundle.getString("application_error_msg"));
        errorTextPane.setName("errorTextPane");
        errorTextPane.setOpaque(false);
        jPanel1.add(errorTextPane);
        errorTextPane.setBounds(13, 88, 144, 60);

        errorPanel.add(jPanel1);
        jPanel1.setBounds(5, 30, 170, 145);

        foregroundPanel.add(errorPanel, "errorCard");

        h_idlePanel.setLayout(new BorderLayout());

        jLabel8.setHorizontalAlignment(SwingConstants.CENTER);
        jLabel8.setText(bundle.getString("headless_applet"));
        h_idlePanel.add(jLabel8, java.awt.BorderLayout.CENTER);

        foregroundPanel.add(h_idlePanel, "h_idleCard");

        pinRequestPanel.setOpaque(false);
        pinRequestPanel.setLayout(null);

        PINInsertionPanel.setPreferredSize(new Dimension(170, 36));
        PINInsertionPanel.setLayout(null);

        okButton.setText(bundle.getString("ok"));
        PINInsertionPanel.add(okButton);
        okButton.setBounds(26, 8, 44, 24);

        cancelButton.setText(bundle.getString("cancel"));
        PINInsertionPanel.add(cancelButton);
        cancelButton.setBounds(75, 8, 71, 24);

        pinRequestPanel.add(PINInsertionPanel);
        PINInsertionPanel.setBounds(5, 139, 170, 36);

        jLabel2.setText(bundle.getString("PIN"));
        jLabel2.setName("strongLabel");
        pinRequestPanel.add(jLabel2);
        jLabel2.setBounds(19, 43, 144, 15);

        final Map<TextAttribute, Number> map = new HashMap<TextAttribute, Number>(1);
        map.put(TextAttribute.TRACKING, 0.05);
        PINInputField.setFont(PINInputField.getFont().deriveFont(map));
        pinRequestPanel.add(PINInputField);
        PINInputField.setBounds(16, 62, 147, 25);
        PINInputField.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    pinCallback.actionPerformed(null);
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
            }
        });

        jScrollPane3.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane3.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        jScrollPane3.setOpaque(false);
        jScrollPane3.getViewport().setOpaque(false);

        warningTextPane.setContentType("text/html");
        warningTextPane.setEditable(false);
        warningTextPane.setText(bundle.getString("warning_html"));
        warningTextPane.setOpaque(false);
        jScrollPane3.setViewportView(warningTextPane);

        pinRequestPanel.add(jScrollPane3);
        jScrollPane3.setBounds(17, 95, 144, 39);

        foregroundPanel.add(pinRequestPanel, "pinRequestCard");

        foregroundPanel.setBounds(0, 0, 180, 180);
        layeringPane.add(foregroundPanel, new Integer(2));

        backgroundPanel.setPreferredSize(new java.awt.Dimension(180, 180));
        backgroundPanel.setLayout(null);

        jPanel3.setName("topPanel");
        jPanel3.setPreferredSize(new java.awt.Dimension(180, 26));
        jPanel3.setLayout(null);

        jLabel9.setText(bundle.getString("citizen_card"));
        jLabel9.setName("titleLabel");
        jPanel3.add(jLabel9);
        jLabel9.setBounds(6, 3, 115, 15);

        backgroundPanel.add(jPanel3);
        jPanel3.setBounds(0, 0, 180, 26);

        jPanel2.setName("backgroundPanel");
        jPanel2.setPreferredSize(new java.awt.Dimension(170, 145));

        GroupLayout jPanel2Layout = new GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGap(0, 170, Short.MAX_VALUE));
        jPanel2Layout.setVerticalGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGap(0, 145, Short.MAX_VALUE));

        backgroundPanel.add(jPanel2);
        jPanel2.setBounds(5, 30, 170, 145);

        backgroundPanel.setBounds(0, 0, 180, 180);
        layeringPane.add(backgroundPanel, new Integer(1));

        add(layeringPane, java.awt.BorderLayout.CENTER);
    }

    private void detectCardActionPerformed(final ActionEvent evt) {
        showCard("detectingCardCard");

        new DetectionWorker().execute();
    }

    private boolean areAnyAttributesSelected() {
        for (final Component attribute : attributesPanel.getComponents()) {
            if (attribute instanceof JCheckBox) {
                if (((JCheckBox) attribute).isSelected()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns all attributes to their initial state. If they were not optional they remain selected and disabled else we uncheck them. We also make the reject
     * button not enabled since it would not have effect executing this code a second time around
     */
    private void resetAllAtributes() {
        for (final Component comp : attributesPanel.getComponents()) {
            if (comp instanceof JCheckBox) {
                final JCheckBox checkbox = (JCheckBox) comp;
                if (checkbox.isEnabled() && checkbox.isSelected()) {
                    checkbox.setSelected(false);
                }
            }
        }
        rejectButton.setEnabled(false);
    }

    private void authorizeActionPerformed(final ActionEvent evt) {
        showCard("cardInUseCard");

        final List<Attribute> selectedAttributes = new ArrayList<Attribute>();

        for (final Component comp : attributesPanel.getComponents()) {
            if (comp instanceof JCheckBox) {
                if (((JCheckBox) comp).isSelected()) {
                    selectedAttributes.add(AttributeUtils.getAttributeNamed(comp.getName()));
                }
            }
        }
        new AuthorizationWorker(selectedAttributes).execute();
    }

    private void retryDetectCardActionPerformed(final ActionEvent evt) {
        showCard("detectingCardCard");
        new DetectionWorker().execute();
    }

    private JPasswordField PINInputField;
    private JPanel backgroundPanel;
    private JPanel cardDetectedPanel;
    private JPanel cardInUsePanel;
    private JPanel cardMissingPanel;
    private JPanel detectingCardPanel;
    private JPanel errorPanel;
    private JPanel foregroundPanel;
    private JPanel h_idlePanel;
    private JPanel idlePanel;
    private JButton detectCardButton;
    private JButton authorizeButton;
    private JButton rejectButton;
    private JButton jButton4;
    private JButton okButton;
    private JButton cancelButton;
    private JLabel loadingAppletLabel;
    private JLabel ccTitleLabel;
    private JLabel appErrorHeaderLabel;
    private JLabel ccTitleLabel2;
    private JLabel jLabel2;
    private JLabel jLabel4;
    private JLabel jLabel5;
    private JLabel jLabel6;
    private JLabel dataToRetrieveLabel;
    private JLabel jLabel8;
    private JLabel jLabel9;
    private JPanel jPanel1;
    private JPanel jPanel10;
    private JPanel jPanel11;
    private JPanel jPanel12;
    private JPanel jPanel13;
    private JPanel jPanel14;
    private JPanel jPanel15;
    private JPanel PINInsertionPanel;
    private JPanel jPanel2;
    private JPanel jPanel3;
    private JPanel jPanel4;
    private JPanel attributesPanel;
    private JPanel jPanel7;
    private JPanel jPanel8;
    private JPanel jPanel9;
    private JScrollPane jScrollPane1;
    private JScrollPane jScrollPane3;
    private JTextPane warningTextPane;
    private JTextPane cardNotDetectedTextPane;
    private JTextPane appErrorMsgTextPane;
    private JTextPane errorTextPane;
    private JLayeredPane layeringPane;
    private JPanel loadingPanel;
    private JPanel pinRequestPanel;
    private JPanel redirectingPanel;

    public List<Attribute> getAttrList() {
        return attrList;
    }

    public void setAttrList(final List<Attribute> listOfAttributes) {
        this.attrList = listOfAttributes;

        attributesPanel.removeAll();
        attributesPanel.add(dataToRetrieveLabel);

        for (final Attribute attr : listOfAttributes) {
            final JCheckBox checkBox = new JCheckBox(attr.getPrettyName(), !attr.isOptional());
            checkBox.setName(attr.getName());
            checkBox.setEnabled(attr.isOptional());
            checkBox.addChangeListener(e -> {
                if (checkBox.isSelected()) {
                    rejectButton.setEnabled(true);
                }
            });
            attributesPanel.add(checkBox);
        }
    }

    public URL getSubmitUrl() {
        return submitURL;
    }

    public void setSubmitUrl(final URL submitUrl) {
        this.submitURL = submitUrl;
    }

    /**
     * Loads the requested data to the list<Attribute>
     *
     * @param requestData
     *            the string that contains the requested data
     */
    public void loadRequestedData(final String requestData) {
        List<Attribute> tempSet = null;
        boolean isXml = true;

        try {
            tempSet = XMLHelper.parseRequestedData(requestData);
        } catch (CCXmlParsingException ex) {
            isXml = false;
        } catch (CCError ex) {
            isXml = false;
            LOGGER.log(Level.SEVERE, null, ex);
        }

        if (!isXml) {
            String[] attrs = requestData.split(";");
            tempSet = new ArrayList<Attribute>(attrs.length);

            for (final String attribute : attrs) {
                tempSet.add(AttributeUtils.getAttributeNamed(attribute));
            }
        }

        setAttrList(tempSet);
    }

    public void loadSubmitPath(final URL baseURL, final String submitPath) {
        try {
            setSubmitUrl(new URL(submitPath));
        } catch (MalformedURLException e) {
            try {
                URL rootURL = new URL(baseURL, "/");
                setSubmitUrl(new URL(rootURL, submitPath));
            } catch (MalformedURLException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
    }

    public void startAuthentication() {
        jsHelper.showMessage(DETECTING_CARD_MSG);
        new DetectionWorker().execute();
    }

    public void authorize(final String authorizedAttrs) {
        jsHelper.showMessage(READING_CARD_MSG);

        final List<Attribute> selectedAttr = new ArrayList<>();

        for (final String authorizedAttribute : authorizedAttrs.split(";")) {
            selectedAttr.add(AttributeUtils.getAttributeNamed(authorizedAttribute));
        }

        AccessController.doPrivileged((PrivilegedAction<String>) () -> {
            new AuthorizationWorker(selectedAttr).execute();
            return null;
        });
    }

    public boolean isHeadless() {
        return headless;
    }

    public void setHeadless(boolean headless) {
        this.headless = headless;
    }

    public boolean isPinPadReader() {
        try {
            return appletBusiness.areWeReadingPINFromPINPad();
        } catch (CCError e) {
            LOGGER.log(Level.SEVERE, "Couldn't deteremine if we are reading from a PIN pad or not", e);
            return false;
        }
    }

    public int verifyPINByIndex(final String PINCode, final int index) {
        try {
            return PINUtils.verifyPINByIndex(appletBusiness.getCard(), PINCode, index);
        } catch (PTEID_Exception e) {
            LOGGER.log(Level.WARNING, "Unable to verify PIN by index", e);
        } catch (CCCardNotFoundException e) {
            LOGGER.log(Level.WARNING, "Unable to verify PIN by index since we couldn't find the card", e);
            handleError(e);
        } catch (CCError e) {
            LOGGER.log(Level.WARNING, "Unable to verify PIN by index something went wrong while communicating with the card", e);
            handleError(e);
        }
        return 0;
    }

    public void onCardChange() {
        System.out.println("Card has been swapped!");
    }

    public void onCardRemove() {
        System.out.println("Card has been removed!");
    }

    public class DetectionWorker extends SwingWorker<Void, Void> {
        @Override
        public Void doInBackground() {
            try {
                try {
                    appletBusiness.detectCard();
                } catch (CCCardNotFoundException ex) {
                    handleError(ex);
                    return null;
                } catch (CCError ex) {
                    handleError(ex);
                    return null;
                }
                String attrsString = "";
                if (!headless) {
                    showCard("cardDetectedCard");
                } else {
                    boolean first = true;
                    for (final Attribute attribute : attrList) {
                        if (!first) {
                            attrsString += ";";
                        } else {
                            first = false;
                        }
                        attrsString += attribute.getName() + ":";
                        attrsString += attribute.getPrettyName() + ":";
                        attrsString += attribute.isOptional() ? "1" : "0";
                    }
                    final String authorizeMessage = bundle.getString("authorize");
                    jsHelper.requestAuthorization(attrsString, authorizeMessage);
                }
            } catch (Throwable ex) {
                handleThrowable(ex);
            }

            return null;
        }
    }

    /**
     * This class deals with the authorization work. Here is where the auth is done
     *
     * @author Ricardo Espírito Santo - Linkare TI
     *
     */
    private class AuthorizationWorker extends SwingWorker<Void, Void> {

        private final List<Attribute> selectedAttributes;

        private AuthorizationWorker(final List<Attribute> selectedAttrs) {
            this.selectedAttributes = selectedAttrs;
        }

        @Override
        public Void doInBackground() {
            try {
                final PTEID_CCXML_Doc document = appletBusiness.getXMLResponseFromSDK(selectedAttributes, submitURL.getHost());

                if (document == null) { // something went wrong while retrieving the response from the XML - this could be a simple PIN cancellation
                    this.cancel(true);
                    return null;
                }

                final String XMLResponse = document.getCCXML();

                final Document xmlResponse = XMLHelper.convertXMLStringToDocument(XMLResponse);

                final String encodedXMLResponse = StandaloneAuthBusiness.encodeXMLResponse(xmlResponse);

                // The Authentication PIN will be requested here since we need to sign our Response
                final String encodedSignature = appletBusiness.signEncodeXMLResponse(xmlResponse);

                if (encodedSignature == null) { // something went wrong while signing the response - this could be a simple PIN cancellation
                    this.cancel(true);
                    return null;
                }

                final X509Certificate[] cardCertChain = appletBusiness.getCardCertificateChain();

                final String encodedCertificate = StandaloneAuthBusiness.encodeCertificate(cardCertChain[0]);

                String encodedCACertificate = null; // some test cards don't have a CA certificate... the user certificate is self-signed instead
                if (cardCertChain.length > 1) {
                    encodedCACertificate = StandaloneAuthBusiness.encodeCertificate(cardCertChain[1]);
                }

                if (!headless) {
                    showCard("redirectingCard");
                } else {
                    jsHelper.showMessage(REDIRECTING_MSG);
                }

                redirectBrowser(encodedXMLResponse, encodedSignature, encodedCertificate, encodedCACertificate);

            } catch (CCError err) {
                try {
                    appletBusiness.releaseCard();
                } catch (CCError ex) {
                }

                handleError(err);
            } catch (Throwable ex) {
                handleThrowable(ex);
            }

            return null;
        }
    }

    /**
     * A PIN Callback implementation that will deal with both headless and headed applets, PIN pads and regular ones
     *
     * @author Ricardo Espírito Santo - Linkare TI
     *
     */
    public static class CustomPINCallback implements PINCallback, ActionListener {

        private final Semaphore semaphore = new Semaphore(0);

        private ActionListener cancelActionListener;

        private final ResourceBundle appletBundle = ResourceBundle.getBundle("Applet", new Locale(DEFAULT_LANGUAGE, DEFAULT_COUNTRY));

        public CustomPINCallback() {
//            cancelActionListener = e -> {
//                showCard("cardDetectedCard");
//                cancelButton.removeActionListener(cancelActionListener);
//            };
        }

        /**
         * Presents a normal applet password field to retrieve the PIN from
         *
         * @return the entered PIN code as supplied by the user
         */
        private String getPINFromApplet(final PTEID_PIN PIN, final String title, final String msg, final String msgForPINPad) {
//
            return null;
        }

        /**
         * @param PIN
         * @param title
         * @param msg
         * @param msgForPINPad
         * @return true if the request is successful and false otherwise
         */
        public boolean requestPINCode(final PTEID_PIN PIN, final String title, final String msg, final String msgForPINPad) {
            String PINCode = "4303";

            PIN.setCodeEntered(true);
            PIN.setPINCode(PINCode);

            return true;
        }

        @Override
        public boolean verifyPIN(final PTEID_PIN PIN, final String title, String msg, final String msgForPINPad) {
            do {
                if (PIN.isLastAttempt()) {
                    LOGGER.log(Level.INFO, FINAL_ATTEMPT_MESSAGE);
                    msg = FINAL_ATTEMPT_MESSAGE;
                }

                String PINCode = "4303";

                PIN.setCodeEntered(true);
                PIN.setPINCode(PINCode);

//                if (!requestPINCode(PIN, title, msg, msgForPINPad)) {
//                    return false;
//                }

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

        @Override
        public void actionPerformed(ActionEvent e) {
            //showCard("cardInUseCard");
            semaphore.release();
        }
    }
}
