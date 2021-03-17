package org.workcraft.plugins.son.elements;

import org.workcraft.dom.Node;

public interface Probability extends Node {
    String PROPERTY_PROBABILITY = "Probability";


    void setProbability(double value);
    double getProbability();
}
