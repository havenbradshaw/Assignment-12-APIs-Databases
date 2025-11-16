package com.assignment12.api;

import java.io.File;
import java.sql.SQLException;
import javafx.application.Application;

public class APIProgram {
    private static DBManager dbManager;

    public static DBManager getDbManager() {
        return dbManager;
    }

    public static void main(String[] args) {
        // DB file in working directory
        File dbFile = new File("countries.db");
        try {
            dbManager = new DBManager(dbFile.getAbsolutePath());
        } catch (SQLException ex) {
            System.err.println("Failed to open DB: " + ex.getMessage());
            System.exit(1);
        }

        // Ensure DB is closed on JVM exit
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try { if (dbManager != null) dbManager.close(); } catch (SQLException e) { /* best-effort close */ }
        }));

        // Launch JavaFX application
        Application.launch(MainFX.class, args);
    }
}
