package org.workcraft.plugins.son.util;

import org.workcraft.dom.math.MathNode;

import java.util.*;

public class MixedStepSeqNode extends MathNode {

    protected   static  HashMap<HashSet<String>, MixedStepSeqNode>  allMarkings = new HashMap<>();
    protected   static  HashMap<HashSet<String>, MixedStepSeqNode>  allSteps = new HashMap<>();
    protected   static  MixedStepSeqNode  mssstartnode = null;

    protected   HashSet<String>           nodenames = new HashSet<>(); // set of names of place/transition nodes
    protected   double                    probability = 0.0; // probability of this node
    protected   HashSet<String>           scenarionames = new HashSet<>(); // set of names of scenarios of this node
    protected   HashSet<MixedStepSeqNode> preNodes = new HashSet<>(); // set of parent objects
    protected   HashSet<MixedStepSeqNode> postNodes = new HashSet<>(); // set of child objects

    protected   enum Sort {MARKING, STEP;}

    protected   Sort sort; // this node is either a MARKING or a STEP

    public double getProbability() { return this.probability; }

    public void setProbability(double value) { this.probability = value; }

}
