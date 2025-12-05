package com.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class ToDoApp extends Application {

    private TaskManager taskManager;

    private ListView<Task> listView;
    private TextField titleField;
    private TextArea descArea;
    private ChoiceBox<String> priorityChoice;
    private Label detailTitle;
    private Label detailPriority;
    private TextArea detailDesc;

    @Override
    public void start(Stage primaryStage) {
        taskManager = new TaskManager();
        initializeComponents();

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(12));

        VBox left = new VBox(10, new Label("Tasks"), listView, createControlButtons());
        left.setPrefWidth(300);
        left.setPadding(new Insets(0,12,0,0));

        VBox right = createDetailPane();

        root.setLeft(left);
        root.setCenter(right);

        setupHandlers();
        refreshFromServer();

        Scene scene = new Scene(root, 800, 450);
        primaryStage.setScene(scene);
        primaryStage.setTitle("To-Do (client)");
        primaryStage.show();
    }

    private void initializeComponents() {
        listView = new ListView<>();
        listView.setItems(taskManager.getTasks());
        listView.setPrefHeight(300);

        titleField = new TextField();
        titleField.setPromptText("Title");

        descArea = new TextArea();
        descArea.setPromptText("Description");
        descArea.setPrefRowCount(3);

        priorityChoice = new ChoiceBox<>();
        priorityChoice.getItems().addAll("Low", "Medium", "High");
        priorityChoice.setValue("Medium");

        detailTitle = new Label();
        detailTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        detailPriority = new Label();
        detailDesc = new TextArea();
        detailDesc.setEditable(false);
        detailDesc.setWrapText(true);
        detailDesc.setPrefRowCount(6);
    }

    private VBox createDetailPane() {
        VBox v = new VBox(10);
        v.setPadding(new Insets(0,0,0,12));

        Label header = new Label("Task Details");
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        GridPane form = new GridPane();
        form.setVgap(8);
        form.setHgap(8);
        form.add(new Label("Title:"), 0, 0);
        form.add(titleField, 1, 0);
        form.add(new Label("Priority:"), 0, 1);
        form.add(priorityChoice, 1, 1);
        form.add(new Label("Description:"), 0, 2);
        form.add(descArea, 1, 2);

        HBox actionRow = new HBox(10);
        actionRow.setAlignment(Pos.CENTER);
        Button addBtn = new Button("Add");
        Button updateBtn = new Button("Update");
        Button deleteBtn = new Button("Delete");
        actionRow.getChildren().addAll(addBtn, updateBtn, deleteBtn);

        addBtn.setOnAction(e -> handleAdd());
        updateBtn.setOnAction(e -> handleUpdate());
        deleteBtn.setOnAction(e -> handleDelete());

        VBox detailsBox = new VBox(6,
                new Separator(),
                header,
                new Label("Selected:"),
                detailTitle,
                detailPriority,
                new Label("Description:"),
                detailDesc
        );
        detailsBox.setPadding(new Insets(8,0,0,0));

        v.getChildren().addAll(form, actionRow, detailsBox);
        return v;
    }

    private HBox createControlButtons() {
        Button refresh = new Button("Refresh");
        refresh.setOnAction(e -> refreshFromServer());
        HBox hb = new HBox(8, refresh);
        hb.setAlignment(Pos.CENTER);
        hb.setPadding(new Insets(8,0,0,0));
        return hb;
    }

    private void setupHandlers() {
        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            showDetails(newV);
            populateForm(newV);
        });
    }

    private void populateForm(Task t) {
        if (t == null) {
            titleField.clear();
            descArea.clear();
            priorityChoice.setValue("Medium");
            return;
        }
        titleField.setText(t.getTitle());
        descArea.setText(t.getDescription());
        priorityChoice.setValue(t.getPriority() == null ? "Medium" : t.getPriority());
    }

    private void showDetails(Task t) {
        if (t == null) {
            detailTitle.setText("");
            detailPriority.setText("");
            detailDesc.clear();
            return;
        }
        detailTitle.setText(t.getTitle());
        detailPriority.setText("Priority: " + (t.getPriority() == null ? "None" : t.getPriority()));
        detailDesc.setText(t.getDescription());
    }

    private void refreshFromServer() {
        try {
            taskManager.fetchAll();
        } catch (Exception ex) {
            showError("Failed to fetch tasks: " + ex.getMessage());
        }
    }

    private void handleAdd() {
        String title = titleField.getText().trim();
        if (title.isEmpty()) { showError("Title required"); return; }
        Task t = new Task(title, descArea.getText(), priorityChoice.getValue());
        try {
            Task created = taskManager.addTask(t);
            if (created != null) listView.getSelectionModel().select(created);
            clearForm();
        } catch (Exception ex) {
            showError("Add failed: " + ex.getMessage());
        }
    }

    private void handleUpdate() {
        Task selected = listView.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("Select task to update"); return; }
        selected = new Task(selected.getId(), titleField.getText(), descArea.getText(), priorityChoice.getValue());
        if (selected.getTitle() == null || selected.getTitle().trim().isEmpty()) { showError("Title required"); return; }
        try {
            Task updated = taskManager.updateTask(selected);
            if (updated != null) listView.getSelectionModel().select(updated);
        } catch (Exception ex) {
            showError("Update failed: " + ex.getMessage());
        }
    }

    private void handleDelete() {
        Task selected = listView.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("Select task to delete"); return; }
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Delete selected task?", ButtonType.YES, ButtonType.NO);
        a.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    taskManager.removeTask(selected);
                    clearForm();
                } catch (Exception ex) {
                    showError("Delete failed: " + ex.getMessage());
                }
            }
        });
    }

    private void clearForm() {
        titleField.clear();
        descArea.clear();
        priorityChoice.setValue("Medium");
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}