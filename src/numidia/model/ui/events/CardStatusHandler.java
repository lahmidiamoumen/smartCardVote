package numidia.model.ui.events;

/**
 * Interface that delineates the two methods expected on the {@link CardAndReaderStatusHandler} class.
 * 
 * this methods relate to the implementer's behaviour for the physical card events.
 * 
 * @author Ricardo Espírito Santo - Linkare TI
 * 
 */
public interface CardStatusHandler {

    /**
     * what should happen when the card connection is lost. Probably due to the fact that the card was removed from the reader
     */
    public void onCardLost();

    /**
     * what should happen when the card connection is regained. Probably due to the fact that the card was reintroduced in the reader.
     */
    public void onCardReconnect();
}
