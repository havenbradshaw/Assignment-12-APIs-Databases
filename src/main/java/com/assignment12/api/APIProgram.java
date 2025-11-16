package com.assignment12.api;

import javax.swing.*;
import java.io.File;
import java.sql.SQLException;

public class APIProgram {
    public static void main(String[] args) {
        // DB file in working directory
        File dbFile = new File("countries.db");
        final DBManager db;
        try {
            db = new DBManager(dbFile.getAbsolutePath());
        } catch (SQLException ex) {
            System.err.println("Failed to open DB: " + ex.getMessage());
            ex.printStackTrace();
            System.exit(1);
            return;
        }

        // Ensure DB is closed on JVM exit
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try { db.close(); } catch (SQLException e) { /* best-effort close */ }
        }));

        SwingUtilities.invokeLater(() -> {
            MainGUI gui = new MainGUI(db);
            gui.setVisible(true);
        });
    }
}
