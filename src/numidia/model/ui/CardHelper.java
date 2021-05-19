package numidia.model.ui;

import java.security.cert.X509Certificate;

import numidia.model.CCCardNotFoundException;
import numidia.model.CCError;
import pteidlib.PTEID_ADDR;
import pteidlib.PTEID_ID;
import pteidlib.PTEID_PIC;

/**
 *
 * @author bnazare
 */
public interface CardHelper {

    void detectCard() throws CCError, CCCardNotFoundException, CCCardNotFoundException;

    X509Certificate[] getCardCertificates() throws CCError;
    
    PTEID_ID getCitizenId() throws CCError;
    
    PTEID_ADDR getCitizenAddr() throws CCError;
    
    PTEID_PIC getCitizenPic() throws CCError;
    
    String getCitizenNotes() throws CCError;

    void releaseCard() throws CCError, CCError;

    byte[] signData(byte[] inputData) throws CCError;

    public interface PINCallback {

        public String getPIN(String pinName, String warning);
    }
}
