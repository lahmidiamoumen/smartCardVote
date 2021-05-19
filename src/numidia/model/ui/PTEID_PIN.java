package numidia.model.ui;

import pt.gov.cartaodecidadao.PTEID_Exception;
import pt.gov.cartaodecidadao.PTEID_Pin;
import pt.gov.cartaodecidadao.PTEID_ulwrapper;

/**
 * Wrapper class on the SDK's PTEID_Pin class
 * 
 * @author Ricardo Espírito Santo - Linkare TI
 * 
 */
public class PTEID_PIN {

    private boolean codeEntered = false;

    private String label;

    private String PINCode;

    private String newPINCode;

    private long id;

    private PINType type;

    private long ref;

    private PINStatus status;

    private long index;

    private int attemptsLeft;

    private PTEID_Pin sdkPIN;

    /**
     * Construct a PIN wrapper element based on the given PTEID_Pin object.
     * 
     * @param sdkPIN
     *            the given object on which this PTEID_PIN is based
     * 
     * @throws PTEID_Exception
     *             if something goes wrong when retrieving the information required
     */
    public PTEID_PIN(final PTEID_Pin sdkPIN) throws PTEID_Exception {
	this.sdkPIN = sdkPIN;
	this.setLabel(sdkPIN.getLabel());
	this.setId(sdkPIN.getId());
	this.setAttemptsLeft(sdkPIN.getTriesLeft());
	this.setRef(sdkPIN.getPinRef());
	this.setType();
	this.setIndex(sdkPIN.getIndex());
    }

    private void setLabel(final String label) {
	this.label = label;
    }

    public String getLabel() {
	return label;
    }

    /**
     * @return the id
     */
    public long getId() {
	return id;
    }

    /**
     * @param id
     *            the id to set
     */
    private final void setId(final long id) {
	this.id = id;
    }

    /**
     * @return the type
     */
    public PINType getType() {
	return type;
    }


    private void setType() {
	switch ((int) this.getRef()) {
	case 130:
	    this.type = PINType.SIGNATURE;
	    break;
	case 131:
	    this.type = PINType.ADDRESS;
	    break;
	default:
	    this.type = PINType.AUTHENTICATION;
	    break;
	}
    }

    /**
     * @return the status
     */
    public PINStatus getStatus() {
	return status;
    }

    /**
     * @param status
     *            the status to set
     */
    private void setStatus(final PINStatus status) {
	this.status = status;
    }

    /**
     * @return the index
     */
    public long getIndex() {
	return index;
    }

    /**
     * @param index
     *            the index to set
     */
    private void setIndex(final long index) {
	this.index = index;
    }

    /**
     * @return the attemptsLeft
     */
    public int getAttemptsLeft() {
	try {
	    return this.sdkPIN.getTriesLeft();
	} catch (PTEID_Exception e) {
	    e.printStackTrace();
	}
	return attemptsLeft;
    }

    /**
     * @param attemptsLeft
     *            the attemptsLeft to set
     */
    private void setAttemptsLeft(final int attemptsLeft) {
	this.attemptsLeft = attemptsLeft;
    }

    public boolean isLastAttempt() {
	return getAttemptsLeft() == 1;
    }

    public boolean hasAttemptsLeft() {
	return getAttemptsLeft() > 0;
    }

    /**
     * @return the PIN reference ->130; address->131; auth->1 for IAS and 129 for GEM Safe
     */
    private long getRef() {
	return ref;
    }

    /**
     * @param ref
     *            the ref to set
     */
    private void setRef(final long ref) {
	this.ref = ref;
    }


    @Override
    public String toString() {
	return this.getType() + " - " + index;
    }

    public boolean isVerifying() {
	return this.getStatus().equals(PINStatus.VERIFYING);
    }

    public boolean isVerified() {
	return this.getStatus().equals(PINStatus.VERIFIED);
    }

    public void setVerifyingAs(final boolean verifying) {
	this.setStatus(verifying ? PINStatus.VERIFYING : PINStatus.NOT_VERIFIED);
    }

    public void setVerifiedAs(final boolean verified) {
	this.setStatus(verified ? PINStatus.VERIFIED : PINStatus.NOT_VERIFIED);
    }

    /**
     * @return the code
     */
    public String getPINCode() {
	return PINCode;
    }

    /**
     * @param code
     *            the code to set
     */
    public void setPINCode(final String code) {
	this.PINCode = code;
    }

    /**
     * @return the endOfPinEntering
     */
    public final void setCodeEntered(final boolean b) {
	this.codeEntered = b;
    }

    /**
     * @return true if the code has been entered and false otherwise
     */
    public boolean isCodeEntered() {
	return codeEntered;
    }

    /**
     * @return the newPINCode
     */
    public final String getNewPINCode() {
	return newPINCode;
    }

    /**
     * @param newPINCode
     *            the newPINCode to set
     */
    public final void setNewPINCode(String newPINCode) {
	this.newPINCode = newPINCode;
    }

    /**
     * @return true if PIN code is valid and false if it isn't
     * 
     * @throws PTEID_Exception
     *             if we can't verify the PIN code
     */
    public final boolean verifyPIN() throws PTEID_Exception {
	return this.sdkPIN.verifyPin(this.getPINCode(), new PTEID_ulwrapper(-1), false);
    }

    /**
     * @return true if the pin was correctly changed and false if it wasn't
     * 
     * @throws PTEID_Exception
     *             if we can't change the PIN code
     */
    public final boolean changePIN() throws PTEID_Exception {
	return this.sdkPIN.changePin(this.getPINCode(), this.getNewPINCode(), new PTEID_ulwrapper(-1), this.getLabel(), false);
    }
}
