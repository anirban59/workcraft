package org.workcraft.plugins.son.commands;

import org.workcraft.commands.Command;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.SONSettings;
import org.workcraft.plugins.son.algorithm.TimeAlg;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import org.workcraft.plugins.son.algorithm.ProbabilityCalcAlg; // Ani: REMOVE AFTER TESTING


public class ProbabilityValueDisablerCommand implements Command {

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, SON.class);
    }

    @Override
    public String getSection() { return "Probability analysis";
    }

    @Override
    public String getDisplayName() {
        return "Enable/Disable probability values";
    }

    @Override
    public void run(WorkspaceEntry we) {
        SON net = WorkspaceUtils.getAs(we, SON.class);
        SONSettings.setProbabilityVisibility(!SONSettings.getProbabilityVisibility());
        /***
         if (SONSettings.getProbabilityVisibility()) {
         TimeAlg.setProperties(net);
         }
         else {
         TimeAlg.removeProperties(net);
         }
         ***/

        /** Ani: the following is a hack to test my methods, which must be removed after the methods can be invoked
         * from my menu items.
         **/
        ProbabilityCalcAlg pca = new ProbabilityCalcAlg(net);
        pca.CalcBaseLevelONsProbability();
    }

}
