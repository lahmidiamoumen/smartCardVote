package numidia.model.ui;




/**
 * Perfom some operations over Attribute enum.
 * 
 * @author Ricardo Espírito Santo - Linkare TI
 * 
 */
public class AttributeUtils {

    /**
     * @param name
     *            the name of the attribute we are looking for
     * @return the Attribute with the given name or null if not found
     */
    public static Attribute getAttributeNamed(final String name) {
	for (final Attribute attrib : Attribute.values()) {
	    if (attrib.getName().equals(name)) {
		return attrib;
	    }
	}
	System.out.println("Couldn't find an attribute named: " + name);
	return null;
    }
}
