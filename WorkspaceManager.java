import java.util.*;import java.util.*;

import java.io.*;import java.io.*;





public class WorkspaceManager {public class WorkspaceManager {

        

    private static final String WORKSPACE_DIR = "workspaces/";    private static final String WORKSPACE_DIR = "workspaces/";



    public WorkspaceManager() {    public WorkspaceManager{

        new File(WORKSPACE_DIR).mkdirs();        new File(WORKSPACE_DIR).mkdirs();

    }    }

}




}
