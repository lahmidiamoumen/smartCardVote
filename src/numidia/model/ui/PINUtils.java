package numidia.model.ui;

import java.util.ArrayList;
import java.util.List;

import pt.gov.cartaodecidadao.PTEID_EIDCard;
import pt.gov.cartaodecidadao.PTEID_Exception;
import pt.gov.cartaodecidadao.PTEID_Pin;
import pt.gov.cartaodecidadao.PTEID_Pins;
import pt.gov.cartaodecidadao.PTEID_ulwrapper;

/**
 * Utility methods related to PINs
 * 
 * TODO move the methods verify PIN and change PIN from here to their respective class PTEID_PIN where they make sence
 * 
 * @author Ricardo Espírito Santo - Linkare TI
 * 
 */
public class PINUtils {

    /**
     * Retrieves the Address PIN object
     * 
     * @param card
     *            the card on which we want to retrieve the PIN
     * @return the Address PTEID_Pin object note that this is not our wrapper object but the live SDK one
     * 
     * @throws PTEID_Exception
     *             if it wasn't possible to retrieve the address PIN
     */
    public static PTEID_Pin getAddressPin(final PTEID_EIDCard card) throws PTEID_Exception {
	return getPinByLabel(card.getPins(), "Morada");
    }

    /**
     * Retrieves the Authentication PIN object
     * 
     * @param card
     *            the card on which we want to retrieve the PIN
     * @return the Authentication PTEID_Pin object note that this is not our wrapper object but the live SDK one
     * @throws PTEID_Exception
     *             if it wasn't possible to retrieve the authentication PIN
     */
    public static PTEID_Pin getAuthenticationPin(final PTEID_EIDCard card) throws PTEID_Exception {
	return getPinByLabel(card.getPins(), "Autentica");
    }

    /**
     * Retreives the Signature PIN object
     * 
     * @param card
     *            the card on which we want to retrieve the PIN
     * @return the Signature PTEID_Pin object note that this is not our wrapper object but the live SDK one
     * @throws PTEID_Exception
     *             if it wasn't possible to retrieve the signature PIN
     */
    public static PTEID_Pin getSignaturePin(final PTEID_EIDCard card) throws PTEID_Exception {
	return getPinByLabel(card.getPins(), "Assina");
    }

    /**
     * Retrieve the PIN by the given label
     * 
     * @param pins
     * @param label
     * @return
     * @throws PTEID_Exception
     */
    public static PTEID_Pin getPinByLabel(final PTEID_Pins pins, final String label) throws PTEID_Exception {
	PTEID_Pin pin = null;
	for (int i = 0, maxi = (int) pins.count(); i < maxi; ++i) {
	    pin = pins.getPinByNumber(i);
	    if (pin.getLabel().contains(label)) {
		return pin;
	    }
	}
	return pin;
    }

    /**
     * Retrieve all the PINs inside the given card
     * 
     * @param card
     *            the object on which we want the PINs
     * 
     * @return an array of PTEID_PIN objects
     * 
     * @throws PTEID_Exception
     *             if something goes wrong while attempting to fetch the PINs
     */

    /**
     * Returns the number of attempts left for the given PIN on the given card
     * 
     * @param card
     *            the card object on which we want to know how many attempts we have
     * @param PINIndex
     *            the PIN index for the query
     * 
     * @return a number of attempts left for the given PIN
     * 
     * @throws PTEID_Exception
     *             if it wasn't possible to retrieve the number of attempts for the given PIN
     */
    public static int getAttemptsLeft(final PTEID_EIDCard card, final long PINIndex) throws PTEID_Exception {
	return card.getPins().getPinByNumber(PINIndex).getTriesLeft();
    }

    /**
     * Changes the PIN code on the current PIN for the given card.
     * 
     * @param card
     *            the card on which we are attempting to change the PIN
     * 
     * @param isPinPadReader
     *            if we are reading the PIN from a PIN pad reader and false otherwise
     * @param oldPIN
     * @param newPIN
     * @param PINIndex
     * @return
     * @throws PTEID_Exception
     */
    public static int changePIN(final PTEID_EIDCard card, final boolean isPinPadReader, final String oldPIN, final String newPIN, final int PINIndex)
	    throws PTEID_Exception {
	PTEID_ulwrapper attemptsLeft = new PTEID_ulwrapper(-1);
	PTEID_Pin pin = card.getPins().getPinByNumber(PINIndex);
	if (pin.changePin(oldPIN, newPIN, attemptsLeft, pin.getLabel(), false)) {
	    return -1;
	} else {
	    return getAttemptsLeft(card, PINIndex);
	}
    }

    public static int verifyPINByIndex(final PTEID_EIDCard card, final String PINToVerify, final long PINIndex) throws PTEID_Exception {
	PTEID_ulwrapper attemptsLeft = new PTEID_ulwrapper(-1);
	if (card.getPins().getPinByNumber(PINIndex).verifyPin(PINToVerify, attemptsLeft, false)) {
	    return -1;
	} else {
	    return getAttemptsLeft(card, PINIndex);
	}
    }
}
