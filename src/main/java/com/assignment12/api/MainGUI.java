package com.assignment12.api;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainGUI extends JFrame {
    private final CountryTableModel tableModel = new CountryTableModel();
    private final JTable table = new JTable(tableModel);
    private final JTextField searchField = new JTextField(20);
    private final JButton fetchBtn = new JButton("Fetch from API");
    private final JButton refreshBtn = new JButton("Refresh (DB)");
    private final JButton searchBtn = new JButton("Search");
    private final JButton exportBtn = new JButton("Export CSV");
    private final JLabel status = new JLabel("Ready");

    private final DBManager dbManager;
    private final APIClient apiClient = new APIClient();

    public MainGUI(DBManager dbManager) {
        super("Countries — API / DB Explorer");
        this.dbManager = dbManager;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800,600);
        setLocationRelativeTo(null);
        initLayout();
        attachHandlers();
    }

    private void initLayout() {
        JPanel top = new JPanel();
        top.add(fetchBtn);
        top.add(refreshBtn);
        top.add(new JLabel("Search:"));
        top.add(searchField);
        top.add(searchBtn);
        top.add(exportBtn);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
    }

    private void attachHandlers() {
        fetchBtn.addActionListener(this::onFetch);
        refreshBtn.addActionListener(e -> loadFromDb());
        searchBtn.addActionListener(this::onSearch);
        exportBtn.addActionListener(this::onExport);
    }

    private void setStatus(String s) {
        SwingUtilities.invokeLater(() -> status.setText(s));
    }

    private void onFetch(ActionEvent e) {
        fetchBtn.setEnabled(false);
        setStatus("Fetching from API...");
        new SwingWorker<List<Country>, Void>() {
            @Override
            protected List<Country> doInBackground() throws Exception {
                return apiClient.fetchAllCountries();
            }

            @Override
            protected void done() {
                fetchBtn.setEnabled(true);
                try {
                    List<Country> res = get();
                    setStatus("Saving " + res.size() + " countries to DB...");
                    try {
                        dbManager.saveCountries(res);
                        tableModel.setData(res);
                        setStatus("Fetched and saved " + res.size() + " countries.");
                    } catch (SQLException ex) {
                        setStatus("DB error: " + ex.getMessage());
                    }
                } catch (Exception ex) {
                    setStatus("API error: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void loadFromDb() {
        setStatus("Loading from DB...");
        new SwingWorker<List<Country>, Void>() {
            @Override
            protected List<Country> doInBackground() throws Exception {
                return dbManager.listAll();
            }

            @Override
            protected void done() {
                try {
                    List<Country> res = get();
                    tableModel.setData(res);
                    setStatus("Loaded " + res.size() + " rows from DB.");
                } catch (InterruptedException | ExecutionException ex) {
                    setStatus("DB error: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void onSearch(ActionEvent e) {
        String q = searchField.getText().trim();
        if (q.isEmpty()) { loadFromDb(); return; }
        setStatus("Searching DB for '" + q + "'...");
        new SwingWorker<List<Country>, Void>() {
            @Override
            protected List<Country> doInBackground() throws Exception {
                return dbManager.searchByName(q);
            }

            @Override
            protected void done() {
                try {
                    List<Country> res = get();
                    tableModel.setData(res);
                    setStatus("Found " + res.size() + " rows for '" + q + "'.");
                } catch (InterruptedException | ExecutionException ex) {
                    setStatus("DB error: " + ex.getMessage());
                }
            }
        }
        .execute();
    }

    private void onExport(ActionEvent e) {
        JFileChooser fc = new JFileChooser();
        int ret = fc.showSaveDialog(this);
        if (ret != JFileChooser.APPROVE_OPTION) return;
        File out = fc.getSelectedFile();
        setStatus("Exporting to " + out.getAbsolutePath());
                new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                CSVExporter.export(out, tableModel != null ? tableModel.getData() : List.of());
                return null;
            }

            @Override
            protected void done() {
                try { get(); setStatus("Export complete: " + out.getAbsolutePath()); }
                catch (InterruptedException | ExecutionException ex) 
                { setStatus("Export failed: " + ex.getMessage()); ex.printStackTrace(); }
            }
        }.execute();
    }
}
