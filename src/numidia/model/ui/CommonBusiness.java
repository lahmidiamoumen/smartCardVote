package numidia.model.ui;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 *
 * @author bnazare
 */
public class CommonBusiness {
    
    public static boolean validateCertificate(Certificate cert) throws GeneralSecurityException, CertPathValidatorException {
        return validateCertificateChain(new Certificate[] { cert });
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

        return x509tm.getAcceptedIssuers();
    }

    private static void dumpHex(byte[] byteStream) {
        for (byte b : byteStream) {
            System.out.print(Integer.toHexString((b & 0xF0) >> 4) + Integer.toHexString(b & 0x0F) + ":");
        }
        System.out.println();
    }
}
