import java.io.*;
import java.util.*;
import java.nio.charset.Charset;

/**
 * Workspace Manager - Core Engine (No External Dependencies)
 * Hack OHI/O Starter Code
 * 
 * What this does:
 * - Captures currently running applications
 * - Saves workspace state to file
 * - Restores workspace by launching saved apps
 * 
 * NO DEPENDENCIES NEEDED - Pure Java
 * 
 * TODO for hackathon:
 * - Add browser tab support (Chrome/Firefox CLI)
 * - Add window position/size capture
 * - Add hotkey listener (JNativeHook library recommended)
 * - Error handling for apps that fail to launch
 */

public class WorkspaceManager {
    
    private static final String WORKSPACE_DIR = "workspaces/";
    
    public WorkspaceManager() {
        new File(WORKSPACE_DIR).mkdirs();
    }
    
    // ==================== CORE CLASSES ====================
    
    /**
     * Represents a single application in a workspace
     */
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
    
    // ==================== CAPTURE FUNCTIONALITY ====================
    
    /**
     * Captures currently running processes (Windows-specific)
     * Returns list of running application names
     */
    public List<String> captureRunningProcesses() {
        List<String> processes = new ArrayList<>();
        
        try {
            // Windows: use tasklist via cmd to ensure shell features are available
            ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "tasklist");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            // Use system default charset (Windows OEM/ANSI may vary); this avoids mangled output
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), Charset.defaultCharset())
            );
            
            String line;
            while ((line = reader.readLine()) != null) {
                // Parse tasklist output
                // Format: "ImageName    PID SessionName  Session# MemUsage"
                String[] parts = line.split("\\s+");
                if (parts.length > 0 && parts[0].endsWith(".exe")) {
                    processes.add(parts[0]);
                }
            }
            
            // wait for command to finish and then close reader
            try {
                process.waitFor();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
            reader.close();
            
        } catch (IOException e) {
            System.err.println("Error capturing processes: " + e.getMessage());
        }
        
        return processes;
    }
    
    /**
     * Creates a workspace from specified apps
     * For hackathon: manually specify important apps
     */
    public Workspace createWorkspace(String workspaceName, List<App> apps) {
        Workspace workspace = new Workspace(workspaceName);
        workspace.apps = apps;
        return workspace;
    }
    
    // ==================== SAVE/LOAD FUNCTIONALITY ====================
    
    /**
     * Save workspace using Java serialization
     */
    public boolean saveWorkspace(Workspace workspace) {
        try {
            String filename = WORKSPACE_DIR + workspace.name + ".dat";
            FileOutputStream fileOut = new FileOutputStream(filename);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(workspace);
            out.close();
            fileOut.close();
            
            System.out.println("Workspace saved: " + filename);
            return true;
            
        } catch (IOException e) {
            System.err.println("Error saving workspace: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Load workspace from file
     */
    public Workspace loadWorkspace(String workspaceName) {
        try {
            String filename = WORKSPACE_DIR + workspaceName + ".dat";
            FileInputStream fileIn = new FileInputStream(filename);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            Workspace workspace = (Workspace) in.readObject();
            in.close();
            fileIn.close();
            
            System.out.println("Workspace loaded: " + filename);
            return workspace;
            
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading workspace: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * List all saved workspaces
     */
    public List<String> listWorkspaces() {
        List<String> workspaces = new ArrayList<>();
        File dir = new File(WORKSPACE_DIR);
        
        File[] files = dir.listFiles((d, name) -> name.endsWith(".dat"));
        if (files != null) {
            for (File file : files) {
                workspaces.add(file.getName().replace(".dat", ""));
            }
        }
        
        return workspaces;
    }
    
    /**
     * Delete a workspace
     */
    public boolean deleteWorkspace(String workspaceName) {
        File file = new File(WORKSPACE_DIR + workspaceName + ".dat");
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }
    
    // ==================== RESTORE FUNCTIONALITY ====================
    
    /**
     * Restore workspace by launching all apps
     */
    public void restoreWorkspace(Workspace workspace) {
        System.out.println("Restoring workspace: " + workspace.name);
        
        for (App app : workspace.apps) {
            launchApp(app);
            // Small delay between launches to avoid overwhelming system
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        System.out.println("Workspace restored successfully!");
    }
    
    /**
     * Launch a single application
     */
    private void launchApp(App app) {
        try {
            ProcessBuilder builder = new ProcessBuilder();
            
            // Build command with arguments
            List<String> command = new ArrayList<>();
            command.add(app.executablePath);
            if (app.args != null && !app.args.isEmpty()) {
                command.addAll(app.args);
            }
            
            builder.command(command);
            builder.start();
            
            System.out.println("✓ Launched: " + app.name);
            
        } catch (IOException e) {
            System.err.println("✗ Error launching " + app.name + ": " + e.getMessage());
        }
    }
    
    // ==================== BROWSER TAB SUPPORT ====================
    
    /**
     * Open Chrome with specific tabs
     * Chrome needs to be in PATH or use full path
     */
    public void openChromeTabs(List<String> urls) {
        try {
            List<String> command = new ArrayList<>();
            // Try common Chrome locations
            String[] chromePaths = {
                "chrome",
                "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe",
                "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe"
            };
            
            command.add(chromePaths[0]); // Start with PATH version
            command.addAll(urls);
            
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.start();
            
            System.out.println("✓ Opened " + urls.size() + " Chrome tabs");
            
        } catch (IOException e) {
            System.err.println("✗ Error opening Chrome tabs: " + e.getMessage());
        }
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Check if an app is currently running
     */
    public boolean isAppRunning(String appName) {
        List<String> processes = captureRunningProcesses();
        return processes.stream()
            .anyMatch(p -> p.equalsIgnoreCase(appName));
    }
    
    /**
     * Kill a running process by name (Windows)
     */
    public boolean killProcess(String processName) {
        try {
            Runtime.getRuntime().exec("taskkill /F /IM " + processName);
            System.out.println("✓ Killed process: " + processName);
            return true;
        } catch (IOException e) {
            System.err.println("✗ Error killing process: " + e.getMessage());
            return false;
        }
    }
    
    // ==================== DEMO/TEST CODE ====================
    
    public static void main(String[] args) {
        WorkspaceManager manager = new WorkspaceManager();
        
        // Example 1: Create and save a "Homework" workspace
        System.out.println("=== Creating Homework Workspace ===");
        
        List<App> homeworkApps = new ArrayList<>();
        
        // Add Notepad as a test (works on all Windows machines)
        homeworkApps.add(new App(
            "Notepad",
            "notepad.exe"
        ));
        
        // Add Calculator
        homeworkApps.add(new App(
            "Calculator",
            "calc.exe"
        ));
        
        // Add Chrome with homework tabs
        App chrome = new App(
            "Chrome - Homework",
            "chrome"
        );
        chrome.args.add("https://canvas.osu.edu");
        chrome.args.add("https://stackoverflow.com");
        homeworkApps.add(chrome);
        
        Workspace homework = manager.createWorkspace("homework", homeworkApps);
        manager.saveWorkspace(homework);
        
        
        // Example 2: Create and save a "Gaming" workspace
        System.out.println("\n=== Creating Gaming Workspace ===");
        
        List<App> gamingApps = new ArrayList<>();
        
        // Add Notepad (as placeholder - replace with your actual gaming apps)
        gamingApps.add(new App(
            "Notepad - Gaming",
            "notepad.exe"
        ));
        
        // Add Chrome with gaming tabs
        App gamingChrome = new App(
            "Chrome - Gaming",
            "chrome"
        );
        gamingChrome.args.add("https://www.twitch.tv");
        gamingChrome.args.add("https://www.youtube.com/gaming");
        gamingApps.add(gamingChrome);
        
        Workspace gaming = manager.createWorkspace("gaming", gamingApps);
        manager.saveWorkspace(gaming);
        
        
        // Example 3: List all workspaces
        System.out.println("\n=== Saved Workspaces ===");
        List<String> workspaces = manager.listWorkspaces();
        for (String ws : workspaces) {
            System.out.println("- " + ws);
        }
        
        
        // Example 4: Load and display workspace info
        System.out.println("\n=== Loading Homework Workspace ===");
        Workspace loaded = manager.loadWorkspace("homework");
        if (loaded != null) {
            System.out.println("Loaded: " + loaded);
            System.out.println("Apps in workspace:");
            for (App app : loaded.apps) {
                System.out.println("  - " + app);
            }
        }
        
        
        // Example 5: Test restore (commented out - uncomment to actually launch apps)
        /*
        System.out.println("\n=== RESTORING WORKSPACE (in 3 seconds...) ===");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {}
        
        manager.restoreWorkspace(loaded);
        */
        
        
        // Example 6: Show currently running processes
        System.out.println("\n=== Currently Running Processes (first 10) ===");
        List<String> processes = manager.captureRunningProcesses();
        for (int i = 0; i < Math.min(10, processes.size()); i++) {
            System.out.println("- " + processes.get(i));
        }
    }
}