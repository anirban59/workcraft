package org.workcraft.plugins.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.serialisation.xml.BasicXMLSerialiser;

public class BooleanSerialiser implements BasicXMLSerialiser<Boolean> {

    @Override
    public String getClassName() {
        return boolean.class.getName();
    }

    @Override
    public void serialise(Element element, Boolean object) throws SerialisationException {
        element.setAttribute("value", Boolean.toString(object));
    }

}
