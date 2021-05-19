package numidia.model.ui;

import pt.gov.cartaodecidadao.PTEID_Exception;
import pt.gov.cartaodecidadao.PTEID_ReaderContext;

/**
 * Utility class for the reader. For now it only allows us to figure if the reader is a PIN pad or not.
 * 
 * FIXME consider using a more solid method of finding out there is a function to which only the pinpads respond sth...
 * 
 * @author Ricardo Espírito Santo - Linkare TI
 * 
 */
public class CardReaderUtils {

    private static final String[] PINPADS_IDENTIFIERS = { "xiring", "pinpad", "acr83" };

    public static boolean isPinPadReader(final PTEID_ReaderContext readerCtx) throws PTEID_Exception {
	final String readerName = readerCtx.getName().toLowerCase();
	for (final String identifier : PINPADS_IDENTIFIERS) {
	    if (readerName.contains(identifier)) {
		return true;
	    }
	}
	return false;
    }
}
