import java.util.*;
import java.io.*;

public class WorkspaceManager {
    private static final String WORKSPACE_DIR = "workspaces/";
    // Added a comment to test git

    public WorkspaceManager() {
        new File(WORKSPACE_DIR).mkdirs();
    } 
}
