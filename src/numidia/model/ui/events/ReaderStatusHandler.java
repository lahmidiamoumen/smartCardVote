package numidia.model.ui.events;

/**
 * Interface that delineates the two methods expected on the {@link CardAndReaderStatusHandler} class.
 * 
 * this methods relate to the implementer's behaviour for the physical card reader events.
 * 
 * @author Ricardo Espírito Santo - Linkare TI
 * 
 */
public interface ReaderStatusHandler {

    /**
     * What should happen when the reader connection is lost. Probably due to the reader being unplugged from the usb.
     */
    public void onReaderLost();

    /**
     * What should happen when the reader connection is regained. Probably due to the reader being re-plugged on the usb.
     */
    public void onReaderReconnect();
}
