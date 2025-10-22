package org.example.server;

import java.sql.SQLException;

public class ServerApplication {
    private static final boolean DEVELOPMENT_MODE = true; // Set to false for production

    public static void main(String[] args) {
        ServerView view = new ServerView();
        
        // Enable debug logging in development mode
        if (DEVELOPMENT_MODE) {
            view.setDebugEnabled(true);
            view.logDebug("Debug logging enabled - DEVELOPMENT MODE");
        }
        
        view.log("Initializing server on port 12345");
        ServerController server = new ServerController(12345, view);
        
        // Initialize database connection
        try {
            DatabaseManager.getInstance();
            view.log("Database connection established successfully");
        } catch (SQLException e) {
            view.logErr("Failed to connect to database: " + view.sanitizeErrorMessage(e.getMessage()));
            throw new RuntimeException(e);
        }
        
        view.log("Server starting on port 12345");
        server.startSocketServer();
    }
}
