package org.workcraft.plugins.son.util;

import org.workcraft.dom.math.MathNode;
import org.workcraft.plugins.son.SON;

import java.util.*;

public class MixedStepSequenceNode extends MathNode {

    protected enum MSSNodeType {MARKING, STEP};

    protected String            id = ""; // System-generated identifier of this MixedStepSequenceNode object
    protected HashMap<String, Double>       bprobability = new HashMap<>(); // Bayesian probability of this object's marking/step relative to its previous step/marking.
    protected HashSet<String>   scenarioids = new HashSet<>(); // Set of identifiers of the scenarios that contain this object's marking/step
    protected MSSNodeType       sort = null; // Indicates whether this object denotes a marking or a step
    protected HashMap<String, MathNode>     sonbasenodes = new HashMap<>(); // Identifiers of the SON base level PlaceNodes or Event nodes of this object
    protected HashMap<String, MathNode>     sonhighernodes = new HashMap<>(); // Identifiers of the SON higher level PlaceNodes or Event nodes of this object
    protected HashMap<String, MixedStepSequenceNode>    preMSSNodes = new HashMap<>(); // Parent objects of this object in the MixedStepSequenceLattice
    protected HashMap<String, MixedStepSequenceNode>    postMSSNodes = new HashMap<>(); // Child objects of this object in the MixedStepSequenceLattice

    public MixedStepSequenceNode(String id) { this.id = id; }

    public String getMSSNodeID() { return id; }

    public void initialise(SON net, HashMap<String, MixedStepSequenceNode> allMSSNodes) { // Initialises the data structures of this pre-existing MSS node object.
        sonbasenodes.keySet().forEach(str -> sonbasenodes.put(str, net.getNodeByReference(str)));

        sonhighernodes.keySet().forEach(str -> sonhighernodes.put(str, net.getNodeByReference(str)));

        preMSSNodes.keySet().forEach(str -> preMSSNodes.put(str, allMSSNodes.get(str)));

        postMSSNodes.keySet().forEach(str -> postMSSNodes.put(str, allMSSNodes.get(str)));
    }

}
