package org.workcraft.workspace;
import java.util.EventListener;

public interface WorkspaceListener extends EventListener {
    void workspaceLoaded();
    void workspaceSaved();
    void entryAdded(WorkspaceEntry we);
    void entryRemoved(WorkspaceEntry we);
    void entryChanged(WorkspaceEntry we);
}
