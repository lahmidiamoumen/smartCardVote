package numidia.model.ui;

import java.io.IOException;
import java.text.ParseException;
import javax.swing.plaf.synth.SynthLookAndFeel;

/**
 *
 * @author bnazare
 */
public class CCCustomLookAndFeel extends SynthLookAndFeel {
    
    private static final String SYNTH_LAF_FILE = "ccLAF.xml";
    
    public CCCustomLookAndFeel() throws IOException, ParseException {
        load(getClass().getClassLoader().getResource(SYNTH_LAF_FILE));
    }
    
}
