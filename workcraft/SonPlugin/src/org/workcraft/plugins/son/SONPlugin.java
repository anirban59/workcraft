package org.workcraft.plugins.son;

import org.workcraft.plugins.CompatibilityManager;
import org.workcraft.Framework;
import org.workcraft.plugins.Plugin;
import org.workcraft.plugins.PluginManager;
import org.workcraft.Version;
import org.workcraft.plugins.son.commands.*;

@SuppressWarnings("unused")
public class SONPlugin implements Plugin {

    @Override
    public String getDescription() {
        return "Structured Occurrence Nets plugin";
    }

    @Override
    public void init() {
        initPluginManager();
        initCompatibilityManager();
    }

    private void initPluginManager() {
        final Framework framework = Framework.getInstance();
        PluginManager pm = framework.getPluginManager();

        pm.registerModelDescriptor(SONDescriptor.class);
        pm.registerSettings(SONSettings.class);

        // Verification
        pm.registerCommand(StructurePropertyCheckerCommand.class);
        pm.registerCommand(ReachabilityCommand.class);
        // Custom tools
        pm.registerCommand(ColorResetCommand.class);
        pm.registerCommand(ClearMarkingCommand.class);
        // Error tracing
        pm.registerCommand(ErrTracingResetCommand.class);
        pm.registerCommand(ErrTracingDisableCommand.class);
        // Time analysis
        pm.registerCommand(TimeValueSetterCommand.class);
        pm.registerCommand(TimeValueDisablerCommand.class);
        //pm.registerTool(TimeValueEstimator.class);
        pm.registerCommand(TimeConsistencyCheckerCommand.class);
        // Probability analysis
        pm.registerCommand(ProbabilityValueDisablerCommand.class);
    }

    private void initCompatibilityManager() {
        final Framework framework = Framework.getInstance();
        final CompatibilityManager cm = framework.getCompatibilityManager();
        Version v310 = new Version(3, 1, 0, Version.Status.RELEASE);

        cm.registerMetaReplacement(v310,
                "<descriptor class=\"org.workcraft.plugins.son.SONModelDescriptor\"/>",
                "<descriptor class=\"org.workcraft.plugins.son.SONDescriptor\"/>");

        cm.registerGlobalReplacement(v310, VisualSON.class.getName(),
                "<VisualONGroup mathGroup=",
                "<VisualONGroup ref=");

        // PNLINE
        cm.registerContextualReplacement(v310, VisualSON.class.getName(), "VisualSONConnection",
                "<graphic class=\"org.workcraft.dom.visual.connections.Polyline\" ref=\"(.*?)\"/>",
                "<property class=\"org.workcraft.plugins.son.connections.SONConnection\\$Semantics\" enum-class=\"org.workcraft.plugins.son.connections.SONConnection\\$Semantics\" name=\"semantics\" value=\"PNLINE\"/>");

        // SYNCLINE
        cm.registerContextualReplacement(v310, VisualSON.class.getName(), "VisualSONConnection",
                "<graphic class=\"org.workcraft.plugins.son.connections.SyncLine\" ref=\"(.*?)\"/>",
                "<property class=\"org.workcraft.plugins.son.connections.SONConnection\\$Semantics\" enum-class=\"org.workcraft.plugins.son.connections.SONConnection\\$Semantics\" name=\"semantics\" value=\"SYNCLINE\"/>");

        cm.registerContextualReplacement(v310, VisualSON.class.getName(), "VisualConnection",
                "<graphic class=\"org.workcraft.plugins.son.connections.SyncLine\" ref=\"(.*?)\"/>",
                "<graphic class=\"org.workcraft.dom.visual.connections.Polyline\" ref=\"$1\"/>");

        // ASYNLINE
        cm.registerContextualReplacement(v310, VisualSON.class.getName(), "VisualSONConnection",
                "<graphic class=\"org.workcraft.plugins.son.connections.AsynLine\" ref=\"(.*?)\"/>",
                "<property class=\"org.workcraft.plugins.son.connections.SONConnection\\$Semantics\" enum-class=\"org.workcraft.plugins.son.connections.SONConnection\\$Semantics\" name=\"semantics\" value=\"ASYNLINE\"/>");

        cm.registerContextualReplacement(v310, VisualSON.class.getName(), "VisualConnection",
                "<graphic class=\"org.workcraft.plugins.son.connections.AsynLine\" ref=\"(.*?)\"/>",
                "<graphic class=\"org.workcraft.dom.visual.connections.Polyline\" ref=\"$1\"/>");

        // BHVLINE
        cm.registerContextualReplacement(v310, VisualSON.class.getName(), "VisualSONConnection",
                "<graphic class=\"org.workcraft.plugins.son.connections.BhvLine\" ref=\"(.*?)\"/>",
                "<property class=\"org.workcraft.plugins.son.connections.SONConnection\\$Semantics\" enum-class=\"org.workcraft.plugins.son.connections.SONConnection\\$Semantics\" name=\"semantics\" value=\"BHVLINE\"/>");

        cm.registerContextualReplacement(v310, VisualSON.class.getName(), "VisualConnection",
                "<graphic class=\"org.workcraft.plugins.son.connections.BhvLine\" ref=\"(.*?)\"/>",
                "<graphic class=\"org.workcraft.dom.visual.connections.Polyline\" ref=\"$1\"/>");
    }

}
