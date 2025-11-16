package com.assignment12.api;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

public class MainFX extends Application {
    private final ObservableList<Country> data = FXCollections.observableArrayList();
    private final APIClient apiClient = new APIClient();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Countries — API / DB Explorer (JavaFX)");

        // Top controls
        Button fetchBtn = new Button("Fetch from API");
        Button refreshBtn = new Button("Refresh (DB)");
        TextField searchField = new TextField();
        searchField.setPromptText("Search...");
        Button searchBtn = new Button("Search");
        Button exportBtn = new Button("Export CSV");

        HBox top = new HBox(8, fetchBtn, refreshBtn, new Label("Search:"), searchField, searchBtn, exportBtn);
        top.setPadding(new Insets(8));

        // Table
        TableView<Country> table = new TableView<>(data);
        TableColumn<Country, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(300);
        TableColumn<Country, Long> popCol = new TableColumn<>("Population");
        popCol.setCellValueFactory(new PropertyValueFactory<>("population"));
        popCol.setPrefWidth(150);
        TableColumn<Country, String> regionCol = new TableColumn<>("Region");
        regionCol.setCellValueFactory(new PropertyValueFactory<>("region"));
        regionCol.setPrefWidth(200);
        table.getColumns().addAll(nameCol, popCol, regionCol);

        Label status = new Label("Ready");

        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setCenter(table);
        root.setBottom(status);

        // Handlers
        fetchBtn.setOnAction(e -> {
            fetchBtn.setDisable(true);
            status.setText("Fetching from API...");
            Task<List<Country>> task = new Task<>() {
                @Override
                protected List<Country> call() throws Exception {
                    return apiClient.fetchAllCountries();
                }
            };
            task.setOnSucceeded(ev -> {
                List<Country> res = task.getValue();
                status.setText("Saving " + res.size() + " countries to DB...");
                Task<Void> saveTask = new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        APIProgram.getDbManager().saveCountries(res);
                        return null;
                    }
                };
                saveTask.setOnSucceeded(ev2 -> {
                    data.setAll(res);
                    status.setText("Fetched and saved " + res.size() + " countries.");
                    fetchBtn.setDisable(false);
                });
                saveTask.setOnFailed(ev2 -> {
                    status.setText("DB error: " + saveTask.getException().getMessage());
                    saveTask.getException().printStackTrace();
                    fetchBtn.setDisable(false);
                });
                new Thread(saveTask, "save-task").start();
            });
            task.setOnFailed(ev -> {
                status.setText("API error: " + task.getException().getMessage());
                task.getException().printStackTrace();
                fetchBtn.setDisable(false);
            });
            new Thread(task, "fetch-task").start();
        });

        refreshBtn.setOnAction(e -> loadFromDb(status));

        searchBtn.setOnAction(e -> {
            String q = searchField.getText().trim();
            if (q.isEmpty()) { loadFromDb(status); return; }
            status.setText("Searching DB for '" + q + "'...");
            Task<List<Country>> task = new Task<>() {
                @Override
                protected List<Country> call() throws Exception {
                    return APIProgram.getDbManager().searchByName(q);
                }
            };
            task.setOnSucceeded(ev -> {
                data.setAll(task.getValue());
                status.setText("Found " + task.getValue().size() + " rows for '" + q + "'.");
            });
            task.setOnFailed(ev -> {
                status.setText("DB error: " + task.getException().getMessage());
                task.getException().printStackTrace();
            });
            new Thread(task, "search-task").start();
        });

        exportBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setInitialFileName("countries.csv");
            File out = fc.showSaveDialog(primaryStage);
            if (out == null) return;
            status.setText("Exporting to " + out.getAbsolutePath());
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    CSVExporter.export(out, List.copyOf(data));
                    return null;
                }
            };
            task.setOnSucceeded(ev -> status.setText("Export complete: " + out.getAbsolutePath()));
            task.setOnFailed(ev -> {
                status.setText("Export failed: " + task.getException().getMessage());
                task.getException().printStackTrace();
            });
            new Thread(task, "export-task").start();
        });

        // Initial load
        loadFromDb(status);

        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();
    }

    private void loadFromDb(Label status) {
        status.setText("Loading from DB...");
        Task<List<Country>> task = new Task<>() {
            @Override
            protected List<Country> call() throws Exception {
                return APIProgram.getDbManager().listAll();
            }
        };
        task.setOnSucceeded(ev -> {
            data.setAll(task.getValue());
            status.setText("Loaded " + task.getValue().size() + " rows from DB.");
        });
        task.setOnFailed(ev -> {
            status.setText("DB error: " + task.getException().getMessage());
            task.getException().printStackTrace();
        });
        new Thread(task, "db-load-task").start();
    }

    @Override
    public void stop() throws Exception {
        // ensure DB closed when FX exits
        try {
            DBManager db = APIProgram.getDbManager();
            if (db != null) db.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        Platform.exit();
    }
}
