package org.workcraft.plugins.cpog.commands;

import org.workcraft.commands.Command;
import org.workcraft.plugins.cpog.VisualCpog;
import org.workcraft.utils.CommandUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public abstract class AbstractPGMinerCommand implements Command {

    public static final String SECTION_TITLE = CommandUtils.makePromotedSectionTitle("Process Mining", 5);

    @Override
    public String getSection() {
        return SECTION_TITLE;
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualCpog.class);
    }

}
