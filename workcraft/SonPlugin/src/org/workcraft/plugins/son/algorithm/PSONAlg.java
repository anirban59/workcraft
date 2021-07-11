package org.workcraft.plugins.son.algorithm;

import org.workcraft.dom.math.MathNode;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.util.MixedStepSequenceLattice;

import java.util.*;

public class PSONAlg extends RelationAlgorithm {

    private ArrayList<HashSet<? extends MathNode>> ScenarioPath = new ArrayList<>(); // Path of sets of SON nodes constituting a potential scenario.

    public PSONAlg(SON net) {
        super(net);
    }

    public boolean ScenarioPathIsEmpty() { return ScenarioPath.isEmpty(); }

    public void AddScenarioPathNodes(HashSet<? extends MathNode> nodes) { ScenarioPath.add(nodes); }

    public void ResetScenarioPath() { ScenarioPath = new ArrayList<>(); }

    public boolean ScenarioPathIsMaximal() {
        if (ScenarioPath.isEmpty()) {
            return false;
        } else {
            for (MathNode startnode : ScenarioPath.get(0))
                if (!isInitial(startnode))
                    return false;

            for (MathNode finishnode : ScenarioPath.get(ScenarioPath.size() - 1))
                if (!isFinal(finishnode))
                    return false;

            return true;
        }
    }

    public MixedStepSequenceLattice AddScenarioPathToMSSLattice(String scenarioID, MixedStepSequenceLattice MSSLattice) {
        String duplicateScenarioID = null;

        if (ScenarioPath.isEmpty()) {
            throw new RuntimeException("ERROR: ScenarioPath is empty.");
        } else if (!ScenarioPathIsMaximal()) {
            throw new RuntimeException("ERROR: ScenarioPath is NOT maximal.");
        } else if (MSSLattice == null) {
            MSSLattice = new MixedStepSequenceLattice(net);
            MSSLattice.addFirstMSS(scenarioID, ScenarioPath);
            MSSLattice.TestPrintScenarioProbabilities(); // Remove after implementation of probabilitity display in the scenario table.
        } else if ((duplicateScenarioID = MSSLattice.ScenarioPathDuplicate(ScenarioPath)) != null) {
            throw new RuntimeException("ERROR: ScenarioPath is a duplicate of scenario " + duplicateScenarioID);
        } else {
            MSSLattice.addMSS(scenarioID, ScenarioPath);
            MSSLattice.TestPrintScenarioProbabilities(); // Remove after implementation of probabilitity display in the scenario table.
        }

        return MSSLattice;
    }

}
