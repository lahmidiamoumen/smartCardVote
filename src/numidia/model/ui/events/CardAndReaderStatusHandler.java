package numidia.model.ui.events;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import pt.gov.cartaodecidadao.PTEID_Exception;
import pt.gov.cartaodecidadao.PTEID_ReaderContext;
import pt.gov.cartaodecidadao.PTEID_ReaderSet;

/**
 * 
 * This class implements a set of card events and receives the references of classes implementing the methods it should execute upon each event.
 * 
 * Currently the only two events supported are the card removal and the card reconnecting
 * 
 * @author Ricardo Espírito Santo - Linkare TI
 * 
 */
public final class CardAndReaderStatusHandler {

    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private static final int POOL_INTERVAL = 1;

    private static final int INITIAL_DELAY = 3;

    private static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;

    private boolean cardWasRemoved;

    private boolean readerWasRemoved;

    private ReaderStatusHandler readerStatusHandler;

    private CardStatusHandler cardStatusHandler;

    public CardAndReaderStatusHandler(final PTEID_ReaderSet readerSet, final ReaderStatusHandler readerStatusHandler, final CardStatusHandler cardStatusHandler) {
	this.readerStatusHandler = readerStatusHandler;
	this.cardStatusHandler = cardStatusHandler;

	final Runnable readerAndCardChangeMonitor = () -> {

	PTEID_ReaderContext readerContext = null;
	try {
		readerContext = readerSet.getReader();
	} catch (PTEID_Exception e2) {
		readerNotPresent();
		return;
	}

	readerPresent();

	try {
		if (!readerContext.isCardPresent()) {
		cardNotPresent();
		} else {
		cardIsPresent();
		}
	} catch (PTEID_Exception e) {
		// reader was disconnected in between method execution
		readerNotPresent();
	}
	};

	executor.scheduleAtFixedRate(readerAndCardChangeMonitor, INITIAL_DELAY, POOL_INTERVAL, TIME_UNIT);
    }

    public void cardNotPresent() {
	if (cardWasRemoved) {
	    // yes we know that the card was removed.... waiting for it to be inserted again
	    return;
	}
	cardWasRemoved = true;
	cardStatusHandler.onCardLost();
    }

    private void cardIsPresent() {
	if (cardWasRemoved) {
	    // Card is back on we should do something about it
	    cardWasRemoved = false;
	    cardStatusHandler.onCardReconnect();
	}
    }

    private void readerNotPresent() {
	if (readerWasRemoved) {
	    // Ok we know it has been removed waiting for it to be connected again
	    return;
	}
	readerWasRemoved = true;
	readerStatusHandler.onReaderLost();
    }

    private void readerPresent() {
	if (readerWasRemoved) {
	    // Reader is connected again we should do something about it
	    readerWasRemoved = false;
	    readerStatusHandler.onReaderReconnect();
	}
    }
}