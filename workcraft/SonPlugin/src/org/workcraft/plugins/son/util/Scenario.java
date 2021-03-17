package org.workcraft.plugins.son.util;

import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.math.MathNode;

public class Scenario extends MathNode {

    private String elements;
    private double probability = 1.0; // probability of this scenario

    public String getScenario() {
        return this.elements;
    }

    public void setScenario(String elements) {
        this.elements = NamespaceHelper.convertLegacyHierarchySeparators(elements);
    }

    public double getProbability() { return this.probability; }

    public void setProbability(double value) { this.probability = value; }

}
