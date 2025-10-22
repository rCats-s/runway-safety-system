package org.example.server;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ServerView {
    public enum LogLevel {
        DEBUG, INFO, WARNING, ERROR
    }
    
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private boolean debugEnabled = false;
    
    public void log(String msg) {
        logWithLevel(LogLevel.INFO, msg);
    }
    

    public void logErr(String msg) {
        logWithLevel(LogLevel.ERROR, msg);
    }
    
    public void logWarning(String msg) {
        logWithLevel(LogLevel.WARNING, msg);
    }
    
    public void logDebug(String msg) {
        if (debugEnabled) {
            logWithLevel(LogLevel.DEBUG, msg);
        }
    }

    public void setDebugEnabled(boolean enabled) {
        this.debugEnabled = enabled;
    }
    

    public String formatDatabaseError(String operation, String entity, Exception e) {
        return String.format("Database error during %s of %s: %s", 
                operation, entity, sanitizeErrorMessage(e.getMessage()));
    }
    
    private void logWithLevel(LogLevel level, String msg) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        String formattedMsg = String.format("[%s] [%s] %s", timestamp, level, msg);
        
        switch (level) {
            case ERROR:
                System.err.println(formattedMsg);
                break;
            default:
                System.out.println(formattedMsg);
                break;
        }
    }
    
    public String sanitizeErrorMessage(String errorMsg) {
        if (errorMsg == null) {
            return "Unknown error";
        }
        
        if (errorMsg.contains("SQLITE_CONSTRAINT")) {
            return "A database constraint was violated";
        }
        
        if (errorMsg.length() > 100) {
            return errorMsg.substring(0, 97) + "...";
        }
        
        return errorMsg;
    }
}
