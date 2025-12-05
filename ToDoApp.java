
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class ToDoApp extends Application {

    private TextField titleField;
    private TextArea descriptionArea;
    private ChoiceBox<String> priorityChoice;
    private Button addButton;
    private Button deleteButton;

    private ListView<Task> taskListView;

    private TaskManager taskManager;

    @Override
    public void start(Stage primaryStage) {
        taskManager = new TaskManager();
        initializeComponents();

        VBox mainLayout = createLayout();
        setupEventHandlers();

        Scene scene = new Scene(mainLayout, 500, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("To-Do List Manager");
        primaryStage.show();
    }

    private void initializeComponents() {
        titleField = new TextField();
        titleField.setPromptText("Task title");
        titleField.setPrefWidth(300);

        descriptionArea = new TextArea();
        descriptionArea.setPromptText("Task description");
        descriptionArea.setPrefRowCount(3);
        descriptionArea.setPrefWidth(300);

        priorityChoice = new ChoiceBox<>();
        priorityChoice.getItems().addAll("Low", "Medium", "High");
        priorityChoice.setPrefWidth(300);
        priorityChoice.setValue("Medium");

        addButton = new Button("Add Task");
        addButton.setPrefWidth(120);

        deleteButton = new Button("Delete Selected");
        deleteButton.setPrefWidth(120);

        taskListView = new ListView<>();
        taskListView.setItems(taskManager.getTasks());
        taskListView.setPrefHeight(250);
    }

    private VBox createLayout() {
        VBox main = new VBox(15);
        main.setPadding(new Insets(20));

        Label titleLabel = new Label("To-Do List");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        HBox titleRow = new HBox(10, new Label("Title:"), titleField);
        HBox priorityRow = new HBox(10, new Label("Priority:"), priorityChoice);

        VBox descriptionBox = new VBox(5, new Label("Description:"), descriptionArea);

        HBox buttonRow = new HBox(20, addButton, deleteButton);
        buttonRow.setAlignment(Pos.CENTER);

        main.getChildren().addAll(
                titleLabel,
                new Separator(),
                titleRow,
                priorityRow,
                descriptionBox,
                new Label("Tasks:"),
                taskListView,
                buttonRow
        );

        return main;
    }

    private void setupEventHandlers() {
        addButton.setOnAction(e -> handleAddTask());
        deleteButton.setOnAction(e -> handleDeleteTask());
    }

    private void handleAddTask() {
        String title = titleField.getText();
        String description = descriptionArea.getText();
        String priority = priorityChoice.getValue();

        if (title.isEmpty()) {
            showAlert("Title required");
            return;
        }

        Task task = new Task(title, description, priority);
        taskManager.addTask(task);

        titleField.clear();
        descriptionArea.clear();
        priorityChoice.setValue("Medium");
    }

    private void handleDeleteTask() {
        Task selected = taskListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Select a task to delete");
            return;
        }
        taskManager.removeTask(selected);
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
