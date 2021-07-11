package org.workcraft.plugins.son.util;

import org.workcraft.dom.math.MathNode;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.elements.Event;
import org.workcraft.plugins.son.elements.PlaceNode;
import org.workcraft.plugins.son.elements.Probability;

import java.util.*;

public class MixedStepSequenceLattice extends MathNode {

    private static final long serialVersionUID = 1L;

    private static final String MSSNodeIDprefix = "MSSNode";
    private static final String MSSNodeIDInitial = "MSSNode0";
    private long MSSNodeIDindex = 1;

    private SON net;

    protected HashMap<String, HashSet<String>>    sonnode_MSSNodes = new HashMap<>();  // For a given SON node ID, the IDs of its MixedStepSequenceNode objects
    protected HashMap<String, Double>             scn_probability = new HashMap<>(); // For a given scenario ID, its probability
    protected HashMap<String, ArrayList<String>>  scn_MSSNodes = new HashMap<>(); // For a given scenario ID, its path through this MixedStepSequenceLattice object

    protected HashMap<String, MixedStepSequenceNode>  allMSSNodes     = new HashMap<>(); // The set of MixedStepSequenceNode objects of the SON
    protected HashMap<String, MixedStepSequenceNode>  allMSSMarkings  = new HashMap<>(); // The set of MixedStepSequenceNode objects of markings of the SON
    protected HashMap<String, MixedStepSequenceNode>  allMSSSteps     = new HashMap<>(); // The set of MixedStepSequenceNode objects of steps of the SON
    protected MixedStepSequenceNode     initialMSSNode = null; // The initial MixedStepSequenceNode object of the initial marking of the SON
    protected double                    probabilityinitialMSSNode = 1.0; // Change this value to the user-input value following implementation of that facility!

    public MixedStepSequenceLattice(SON net) { this.net = net; }

    public SON getNet() { return net; }

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
        for (MixedStepSequenceNode MSSNode : allMSSNodes.values())
            MSSNode.initialise(net, allMSSNodes);
    } // End of initialise() method.

    public void addFirstMSS(String scenarioID, ArrayList<HashSet<? extends MathNode>> ScenarioPath) {
        initialMSSNode = new MixedStepSequenceNode(MSSNodeIDInitial);
        initialMSSNode.bprobability.put(MSSNodeIDInitial, probabilityinitialMSSNode);
        initialMSSNode.scenarioids.add(scenarioID);
        initialMSSNode.sort = MixedStepSequenceNode.MSSNodeType.MARKING;

        /* Updating initialMSSNode.sonbasenodes and sonnode_MSSNodes. */
        AddtoSONnodesMSSNode(ScenarioPath.get(0), initialMSSNode);

        for (MathNode SONnode : ScenarioPath.get(0))
            if (SONnode instanceof Event || SONnode instanceof PlaceNode)
                ((Probability) SONnode).setProbability(probabilityinitialMSSNode);

        allMSSNodes.put(initialMSSNode.id, initialMSSNode);
        allMSSMarkings.put(initialMSSNode.id, initialMSSNode);
        ArrayList<String> scnMSSNodeIDs = new ArrayList<>();
        scnMSSNodeIDs.add(initialMSSNode.id);

        MixedStepSequenceNode preMSSNode = initialMSSNode;
        for (int i = 1; i < ScenarioPath.size(); i++) {
            MixedStepSequenceNode postMSSNode = new MixedStepSequenceNode(makeMSSNodeID());
            preMSSNode.postMSSNodes.put(postMSSNode.id, postMSSNode);
            postMSSNode.preMSSNodes.put(preMSSNode.id, preMSSNode);
            postMSSNode.bprobability.put(preMSSNode.id, 1.0);
            postMSSNode.scenarioids.add(scenarioID);

            if (preMSSNode.sort == MixedStepSequenceNode.MSSNodeType.MARKING) {
                postMSSNode.sort = MixedStepSequenceNode.MSSNodeType.STEP;
                allMSSSteps.put(postMSSNode.id, postMSSNode);
            }
            else {
                postMSSNode.sort = MixedStepSequenceNode.MSSNodeType.MARKING;
                allMSSMarkings.put(postMSSNode.id, postMSSNode);
            }
            allMSSNodes.put(postMSSNode.id, postMSSNode);

            /* Updating postMSSNode.sonbasenodes and sonnode_MSSNodes. */
            AddtoSONnodesMSSNode(ScenarioPath.get(i), postMSSNode);

            for (MathNode SONnode : ScenarioPath.get(i))
                if (SONnode instanceof Event || SONnode instanceof PlaceNode)
                    ((Probability) SONnode).setProbability(probabilityinitialMSSNode);

            scnMSSNodeIDs.add(postMSSNode.id);

            preMSSNode = postMSSNode;
        }

        scn_probability.put(scenarioID, probabilityinitialMSSNode);
        scn_MSSNodes.put(scenarioID, scnMSSNodeIDs);
    } // End of addFirstMSS() method.

    public void addMSS(String scenarioID, ArrayList<HashSet<? extends MathNode>> ScenarioPath) {
        // Checking the initial marking has NOT changed from a previously saved scenario.
        HashSet<MathNode> initialMSSNodeSONnodes = new HashSet<>();
        initialMSSNodeSONnodes.addAll(initialMSSNode.sonbasenodes.values());
        if (!initialMSSNodeSONnodes.equals(ScenarioPath.get(0)))
            throw new RuntimeException("ERROR: Current initial marking is different from the initial marking of a previously saved scenario.");

        initialMSSNode.scenarioids.add(scenarioID);
        HashSet<String> changedScenariosIDs = new HashSet<>();
        changedScenariosIDs.add(scenarioID);
        ArrayList<String> scnMSSNodeIDs = new ArrayList<>();
        scnMSSNodeIDs.add(initialMSSNode.id);

        MixedStepSequenceNode markingMSSNode = initialMSSNode;
        Boolean preexistingmarkingMSSNode = true;
        Boolean preexistingstepMSSNode = null;
        for (int i = 0; i < (ScenarioPath.size() - 1) / 2; i++) { // Alternately processing the steps and markings of the new scenario.
            MixedStepSequenceNode stepMSSNode = FindMSSNode(ScenarioPath.get(2 * i + 1), MixedStepSequenceNode.MSSNodeType.STEP);
            if (stepMSSNode != null)
                preexistingstepMSSNode = true;
            else
                preexistingstepMSSNode = false;

            if (preexistingmarkingMSSNode && preexistingstepMSSNode) { // Both marking/step MSS nodes exist from a previous scenario.
                stepMSSNode.scenarioids.add(scenarioID);
                if (!stepMSSNode.preMSSNodes.containsKey(markingMSSNode.id)) {
                    markingMSSNode.postMSSNodes.put(stepMSSNode.id, stepMSSNode);
                    stepMSSNode.preMSSNodes.put(markingMSSNode.id, markingMSSNode);
                    /* Updating Bayesian probabilities of peer MSS step objects - including this MSS step object. */
                    changedScenariosIDs.addAll(UpdStepMSSNodesProbabilities(markingMSSNode.id, CalcStepBProbabilities(markingMSSNode)));
                }
            }
            else if (preexistingmarkingMSSNode && !preexistingstepMSSNode) { // Creating/processing a new step MSS node for an existing marking MSS node.
                    stepMSSNode = new MixedStepSequenceNode(makeMSSNodeID());
                    markingMSSNode.postMSSNodes.put(stepMSSNode.id, stepMSSNode);
                    stepMSSNode.preMSSNodes.put(markingMSSNode.id, markingMSSNode);
                    stepMSSNode.scenarioids.add(scenarioID);
                    stepMSSNode.sort = MixedStepSequenceNode.MSSNodeType.STEP;
                    allMSSSteps.put(stepMSSNode.id, stepMSSNode);
                    allMSSNodes.put(stepMSSNode.id, stepMSSNode);
                    /* Updating stepMSSNode.sonbasenodes and sonnode_MSSNodes. */
                    AddtoSONnodesMSSNode(ScenarioPath.get(2 * i + 1), stepMSSNode);
                    /* Updating Bayesian probabilities of peer MSS step objects - including this MSS step object. */
                    changedScenariosIDs.addAll(UpdStepMSSNodesProbabilities(markingMSSNode.id, CalcStepBProbabilities(markingMSSNode)));
            }
            else if (!preexistingmarkingMSSNode && preexistingstepMSSNode){ // The step MSS node exists in another scenario but the marking MSS node is new.
                stepMSSNode.bprobability.put(markingMSSNode.id, 1.0);
                stepMSSNode.scenarioids.add(scenarioID);
                markingMSSNode.postMSSNodes.put(stepMSSNode.id, stepMSSNode);
                stepMSSNode.preMSSNodes.put(markingMSSNode.id, markingMSSNode);
            }
            else if (!preexistingmarkingMSSNode && !preexistingstepMSSNode) { // Both the marking AND the step MSS nodes are new.
                stepMSSNode = new MixedStepSequenceNode(makeMSSNodeID());
                markingMSSNode.postMSSNodes.put(stepMSSNode.id, stepMSSNode);
                stepMSSNode.preMSSNodes.put(markingMSSNode.id, markingMSSNode);
                stepMSSNode.bprobability.put(markingMSSNode.id, 1.0);
                stepMSSNode.scenarioids.add(scenarioID);
                stepMSSNode.sort = MixedStepSequenceNode.MSSNodeType.STEP;
                allMSSSteps.put(stepMSSNode.id, stepMSSNode);
                allMSSNodes.put(stepMSSNode.id, stepMSSNode);
                /* Updating stepMSSNode.sonbasenodes and sonnode_MSSNodes. */
                AddtoSONnodesMSSNode(ScenarioPath.get(2 * i + 1), stepMSSNode);
            }

            scnMSSNodeIDs.add(stepMSSNode.id);

            markingMSSNode = FindMSSNode(ScenarioPath.get(2 * i + 2), MixedStepSequenceNode.MSSNodeType.MARKING);
            if (markingMSSNode != null)
                preexistingmarkingMSSNode = true;
            else
                preexistingmarkingMSSNode = false;

            if (!preexistingstepMSSNode || !preexistingmarkingMSSNode) { // Either the step OR the following marking MSS node is new.
                if (!preexistingmarkingMSSNode) { // The marking following the step MSS node is new.
                    markingMSSNode = new MixedStepSequenceNode(makeMSSNodeID());
                    markingMSSNode.sort = MixedStepSequenceNode.MSSNodeType.MARKING;
                    allMSSMarkings.put(markingMSSNode.id, markingMSSNode);
                    allMSSNodes.put(markingMSSNode.id, markingMSSNode);
                    /* Updating markingMSSNode.sonbasenodes and sonnode_MSSNodes. */
                    AddtoSONnodesMSSNode(ScenarioPath.get(2 * i + 2), markingMSSNode);
                }
                markingMSSNode.bprobability.put(stepMSSNode.id, 1.0);
                stepMSSNode.postMSSNodes.put(markingMSSNode.id, markingMSSNode);
                markingMSSNode.preMSSNodes.put(stepMSSNode.id, stepMSSNode);
            }
            else if (preexistingstepMSSNode && preexistingmarkingMSSNode) {
                if (!markingMSSNode.preMSSNodes.containsKey(stepMSSNode.id)) {
                    markingMSSNode.bprobability.put(stepMSSNode.id, 1.0);
                    stepMSSNode.postMSSNodes.put(markingMSSNode.id, markingMSSNode);
                    markingMSSNode.preMSSNodes.put(stepMSSNode.id, stepMSSNode);
                }
            }
            markingMSSNode.scenarioids.add(scenarioID);

            scnMSSNodeIDs.add(markingMSSNode.id);
        }

        scn_MSSNodes.put(scenarioID, scnMSSNodeIDs); // Saving the MSS of the new scenario.
        scn_probability.put(scenarioID, 0.0); // Enabling the probability of the new scenario/an existing changed scenario to be calculated similarly.
        for (String changedScenarioID : changedScenariosIDs) { // Calculating the probabilities of the new/changed scenarios.
            ArrayList<String> MSSNodeIDs = scn_MSSNodes.get(changedScenarioID); // Fetching the MSS of the scenario.
            int MSSlength = MSSNodeIDs.size();
            Double newprobabilityofscenario = initialMSSNode.bprobability.get(initialMSSNode.id);
            HashSet<MathNode> changedSONnodes = new HashSet<>();
            for (int i = 1; i < MSSlength; i++) { // Calculating the scenario probability and identifying its SON nodes.
                MixedStepSequenceNode MSSNode = allMSSNodes.get(MSSNodeIDs.get(i));
                newprobabilityofscenario *= MSSNode.bprobability.get(MSSNodeIDs.get(i-1));
                changedSONnodes.addAll(MSSNode.sonbasenodes.values());
            }
            Double oldprobabilityofscenario = scn_probability.get(changedScenarioID);
            scn_probability.put(changedScenarioID, newprobabilityofscenario);
            Double newSONnodeprobability;
            for (MathNode changedSONnode : changedSONnodes) {
                if (changedSONnode instanceof Event || changedSONnode instanceof PlaceNode) {
                    newSONnodeprobability = ((Probability) changedSONnode).getProbability() - oldprobabilityofscenario + newprobabilityofscenario;
                    ((Probability) changedSONnode).setProbability(newSONnodeprobability);
                }
            }
        }
    } // End of addMSS() method.

    public String ScenarioPathDuplicate(ArrayList<HashSet<? extends MathNode>> ScenarioPath) {
        if (ScenarioPath.isEmpty())
            throw new RuntimeException("ERROR: ScenarioPath is empty.");

        for (ArrayList<String> scnMSS : scn_MSSNodes.values()) { // Iterating over the different scenario paths of the MSS lattice.
            // Are the lengths of this lattice scenario path and the input scenario path different?
            if (scnMSS.size() != ScenarioPath.size())
                continue;

            for (int i = 0; i < scnMSS.size(); i++) { // Iterating over the different markings/steps of the lattice/input scenario paths.
                HashSet<MathNode> MSSNodeiSONnodes = new HashSet<>();
                MSSNodeiSONnodes.addAll(allMSSNodes.get(scnMSS.get(i)).sonbasenodes.values());
                HashSet<? extends MathNode> ScenarioPathSONnodesi = ScenarioPath.get(i);
                // Are the cardinalities of the corresponding markings/steps of the lattice/input scenario paths different?
                if (MSSNodeiSONnodes.size() != ScenarioPathSONnodesi.size())
                    break;
                // Are the SON nodes of the corresponding markings/steps of the lattice/input scenario paths different?
                if (!MSSNodeiSONnodes.equals(ScenarioPathSONnodesi))
                    break;

                if (i == scnMSS.size() - 1) // The SON nodes of this lattice/input scenario path are identical.
                    return (String) net.getKeyFromValue(scn_MSSNodes, scnMSS);
            }
        }

        return null;
    } // End of ScenarioPathDuplicate() method.

    private MixedStepSequenceNode FindMSSNode(HashSet<? extends MathNode> SONnodes, MixedStepSequenceNode.MSSNodeType sort) {
        if (sort == MixedStepSequenceNode.MSSNodeType.STEP) {
            for (MixedStepSequenceNode MSSNode : allMSSSteps.values()) {
                HashSet<MathNode> MSSNodeSONnodes = new HashSet<>();
                MSSNodeSONnodes.addAll(MSSNode.sonbasenodes.values());
                if (MSSNodeSONnodes.equals(SONnodes))
                    return MSSNode;
            }
        } else if (sort == MixedStepSequenceNode.MSSNodeType.MARKING) {
            for (MixedStepSequenceNode MSSNode : allMSSMarkings.values()) {
                HashSet<MathNode> MSSNodeSONnodes = new HashSet<>();
                MSSNodeSONnodes.addAll(MSSNode.sonbasenodes.values());
                if (MSSNodeSONnodes.equals(SONnodes))
                    return MSSNode;
            }
        } else {
            for (MixedStepSequenceNode MSSNode : allMSSNodes.values()) {
                HashSet<MathNode> MSSNodeSONnodes = new HashSet<>();
                MSSNodeSONnodes.addAll(MSSNode.sonbasenodes.values());
                if (MSSNodeSONnodes.equals(SONnodes))
                    return MSSNode;
            }
        }

        return null;
    } // End of FindMSSNode() method.

    private HashMap<String, Double> CalcStepBProbabilities(MixedStepSequenceNode markingMSSNode) {
        HashMap<String, Double> probabilitiesMSSNodes = new HashMap<>();
        double sumproductweights = 0.0;

        if (markingMSSNode == null) {
            throw new RuntimeException("ERROR: Input MixedStepSequenceNode parameter is null.");
        } else if (markingMSSNode.sort != MixedStepSequenceNode.MSSNodeType.MARKING) {
            throw new RuntimeException("ERROR: Input MixedStepSequenceNode parameter is not a MARKING.");
        }

        for (MixedStepSequenceNode stepMSSNode : markingMSSNode.postMSSNodes.values()) {
            double productweights = 1.0;
            for (MathNode event : stepMSSNode.sonbasenodes.values())
                if (event instanceof Event)
                    productweights *= ((Event) event).getWeight();

            probabilitiesMSSNodes.put(stepMSSNode.id, productweights);
            sumproductweights += productweights;
        }

        if (sumproductweights != 0.0) {
            for (String str : probabilitiesMSSNodes.keySet())
                // For each step MSS node, normalising the product of the weights of its events to produce a Bayesian probability.
                probabilitiesMSSNodes.put(str, probabilitiesMSSNodes.get(str) / sumproductweights);
        } else {
            for (String str : probabilitiesMSSNodes.keySet())
                probabilitiesMSSNodes.put(str, 0.0);
        }

        return probabilitiesMSSNodes;
    } // end of CalcStepBProbabilities() method.

    private HashSet<String> UpdStepMSSNodesProbabilities(String markingMSSNodeID, HashMap<String, Double> probabilitiesStepMSSNodes) {
        if (probabilitiesStepMSSNodes == null)
            throw new RuntimeException("ERROR: Input probabilitiesStepMSSNodes parameter is null.");

        for (String stepMSSNodeID : probabilitiesStepMSSNodes.keySet()) {
            MixedStepSequenceNode stepMSSNode = allMSSSteps.get(stepMSSNodeID);
            if (stepMSSNode != null)
                stepMSSNode.bprobability.put(markingMSSNodeID, probabilitiesStepMSSNodes.get(stepMSSNodeID));
        }

        HashSet<String> changedScenariosIDs = new HashSet<>();
        changedScenariosIDs.addAll(allMSSMarkings.get(markingMSSNodeID).scenarioids);
        return changedScenariosIDs;
    } // End of UpdStepMSSNodesProbabilities() method.

    private void AddtoSONnodesMSSNode(HashSet<? extends MathNode> SONnodes, MixedStepSequenceNode MSSnode) {
        if (SONnodes == null)
            throw new RuntimeException("ERROR: Input SONnodes parameter is null.");
        else if (MSSnode == null)
            throw new RuntimeException("ERROR: Input MSSnode parameter is null.");

        for (MathNode SONnode : SONnodes) {
            String SONnodeID = net.getNodeReference(SONnode);
            MSSnode.sonbasenodes.put(SONnodeID, SONnode); // Change this later to handle BSON nodes!

            HashSet<String> SONnodeMSSNodeIDs = sonnode_MSSNodes.get(SONnodeID);
            if (SONnodeMSSNodeIDs == null)
                SONnodeMSSNodeIDs = new HashSet<String>();
            SONnodeMSSNodeIDs.add(MSSnode.id);
            sonnode_MSSNodes.put(SONnodeID, SONnodeMSSNodeIDs);
        }
    }

    private String makeMSSNodeID() { return MSSNodeIDprefix + MSSNodeIDindex++; }

    public void TestPrintScenarioProbabilities() {
        scn_probability.keySet().forEach(scenarioID -> System.out.println("Probability(" + scenarioID + ") == " + scn_probability.get(scenarioID)));
        System.out.println();
    }

}
