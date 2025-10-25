import java.util.*;
import java.io.*;

public class WorkspaceManager {
    private static final String WORKSPACE_DIR = "workspaces/"; 

    public WorkspaceManager() {
        new File(WORKSPACE_DIR).mkdirs();
    } 

    // ==================== CORE CLASSES ====================

    static class App implements Serializable {
        private static final long serialVersionUID = 1L;
        
        String name;           // User-friendly name
        String executablePath; // Full path to .exe or command
        List<String> args;     // Command line arguments
        
        public App(String name, String executablePath) {
            this.name = name;
            this.executablePath = executablePath;
            this.args = new ArrayList<>();
        }
        
        public App(String name, String executablePath, List<String> args) {
            this.name = name;
            this.executablePath = executablePath;
            this.args = args;
        }
        
        @Override
        public String toString() {
            return name + " (" + executablePath + ")";
        }
    }
    
    /**
     * Represents a complete workspace state
     */
    static class Workspace implements Serializable {
        private static final long serialVersionUID = 1L;
        
        String name;
        List<App> apps;
        long timestamp;
        
        public Workspace(String name) {
            this.name = name;
            this.apps = new ArrayList<>();
            this.timestamp = System.currentTimeMillis();
        }
        
        @Override
        public String toString() {
            return name + " (" + apps.size() + " apps)";
        }
    }
}
