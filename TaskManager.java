import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class TaskManager {

    private ObservableList<Task> tasks = FXCollections.observableArrayList();

    public ObservableList<Task> getTasks() {
        return tasks;
    }

    public void addTask(Task task) {
        tasks.add(task);
    }

    public void removeTask(Task task) {
        tasks.remove(task);
    }
}