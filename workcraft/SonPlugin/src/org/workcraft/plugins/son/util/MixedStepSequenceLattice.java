package org.workcraft.plugins.son.util;

import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathNode;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.elements.PlaceNode;
import org.workcraft.plugins.son.util.MixedStepSequenceNode;

import javax.swing.*;
import java.awt.*;
import java.util.*;

public class MixedStepSequenceLattice extends MathNode {

    private static final long serialVersionUID = 1L;

    private static final String MSSNodeIDprefix = "MSSNode";
    private static final String MSSNodeIDInitial = "MSSNode0";

    private SON net;

    protected HashMap<String, HashSet<String>>    sonnode_MSSNodes = new HashMap<>();  // For a given SON node ID, the IDs of its MixedStepSequenceNode objects
    protected HashMap<String, Double>             scn_probability = new HashMap<>(); // For a given scenario ID, its probability
    protected HashMap<String, ArrayList<String>>  scn_MSSNodes = new HashMap<>(); // For a given scenario ID, its path through this MixedStepSequenceLattice object

    protected HashMap<String, MixedStepSequenceNode>  allMSSNodes     = new HashMap<>(); // The set of MixedStepSequenceNode objects of the SON
    protected HashMap<String, MixedStepSequenceNode>  allMSSMarkings  = new HashMap<>(); // The set of MixedStepSequenceNode objects of markings of the SON
    protected HashMap<String, MixedStepSequenceNode>  allMSSSteps     = new HashMap<>(); // The set of MixedStepSequenceNode objects of steps of the SON
    protected MixedStepSequenceNode   initialMSSNode = null; // The initial MixedStepSequenceNode object of the initial marking of the SON


    public SON getNet() { return net; }

    public void setNet(SON net) { this.net = net; }

    public void initialise() { // Initialises deeply the data structures of this pre-existing MSS lattice object.
        allMSSNodes.keySet().forEach(str -> allMSSNodes.put(str, (MixedStepSequenceNode) net.getNodeByReference(str)));

        for (String str : allMSSNodes.keySet()) {
            if (allMSSNodes.get(str).sort == MixedStepSequenceNode.MSSNodeType.MARKING) {
                allMSSMarkings.put(str, allMSSNodes.get(str));
            }
            else if (allMSSNodes.get(str).sort == MixedStepSequenceNode.MSSNodeType.STEP) {
                allMSSSteps.put(str, allMSSNodes.get(str));
            }
            else
                throw new RuntimeException("ERROR: Value of Key " + str + " in allMSSNodes HashMap is neither a marking nor a step.");
        }

        initialMSSNode = allMSSMarkings.get(MSSNodeIDInitial);
        for (MixedStepSequenceNode MSSnode : allMSSNodes.values()) {
            MSSnode.initialise(net, allMSSNodes);
        }
    }

}
