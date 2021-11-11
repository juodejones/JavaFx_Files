package com.todolist;

import com.todolist.datamodel.TodoData;
import com.todolist.datamodel.TodoItem;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Callback;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;

public class Controller {

    @FXML
    private ListView<TodoItem> todoListView;

    @FXML
    private TextArea itemDetailsTextArea;

    @FXML
    private Label deadLineBoxText;

    @FXML
    private BorderPane mainBorderPane;

//    @FXML
//    private MenuItem exitMenu;

    @FXML
    private ContextMenu listContextMenu;

    @FXML
    private ToggleButton filterToggleButton;

    private FilteredList<TodoItem> filteredList;
    private Predicate<TodoItem> wantAll, wantTodays;

    public void initialize() {
        /*List<TodoItem> todoItems = new ArrayList<>();

        TodoItem item1 = new TodoItem("Mail birthday card", "Buy a 30th birthday card for John",
                LocalDate.of(2016, Month.APRIL, 25));
        TodoItem item2 = new TodoItem("Doctor's Appointment", "See Dr. Smith at 123 Main Street.  Bring paperwork",
                LocalDate.of(2016, Month.MAY, 23));
        TodoItem item3 = new TodoItem("Finish design proposal for client", "I promised Mike I'd email website mockups by Friday 22nd April",
                LocalDate.of(2021, Month.JUNE, 19));
        TodoItem item4 = new TodoItem("Pickup Doug at the train station", "Doug's arriving on March 23 on the 5:00 train",
                LocalDate.of(2021, Month.JUNE, 19));
        TodoItem item5 = new TodoItem("Pick up dry cleaning", "The clothes should be ready by Wednesday",
                LocalDate.of(2021, Month.JULY,01));

        todoItems.add(item1);
        todoItems.add(item2);
        todoItems.add(item3);
        todoItems.add(item4);
        todoItems.add(item5);

        TodoData.getInstance().setTodoItems(todoItems);*/

//        exitMenu. setOnAction(new EventHandler<ActionEvent>() {
//            public void handle(ActionEvent t) {
//                System. exit(0);
//            }
//        });

        listContextMenu = new ContextMenu();
        MenuItem deleteMenu = new MenuItem("Delete");
        deleteMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                TodoItem item = todoListView.getSelectionModel().getSelectedItem();
                delete(item);
            }
        });

        todoListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TodoItem>() {
            @Override
            public void changed(ObservableValue<? extends TodoItem> observableValue, TodoItem todoItem, TodoItem t1) {
                if (t1 != null) {
                    TodoItem item = todoListView.getSelectionModel().getSelectedItem();
                    itemDetailsTextArea.setText(item.getDetails());
                    DateTimeFormatter df = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
                    deadLineBoxText.setText("Due : " + df.format(item.getDeadLine()));
                }
            }
        });


        wantTodays = new Predicate<TodoItem>() {
            @Override
            public boolean test(TodoItem item) {
                return item.getDeadLine().equals(LocalDate.now());
            }
        };

        wantAll = new Predicate<TodoItem>() {
            @Override
            public boolean test(TodoItem item) {
                return true;
            }
        };

        filteredList = new FilteredList<>(TodoData.getInstance().getTodoItems(), wantAll);

        SortedList<TodoItem> sortedList = new SortedList<>(filteredList, new Comparator<TodoItem>() {
            @Override
            public int compare(TodoItem o1, TodoItem o2) {
                return o1.getDeadLine().compareTo(o2.getDeadLine());
            }
        });

        todoListView.setItems(sortedList);
        todoListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        todoListView.getSelectionModel().selectFirst();

        todoListView.setCellFactory(new Callback<ListView<TodoItem>, ListCell<TodoItem>>() {
            @Override
            public ListCell<TodoItem> call(ListView<TodoItem> todoItemListView) {
                ListCell<TodoItem> cell = new ListCell<>() {
                    @Override
                    protected void updateItem(TodoItem item, boolean b) {
                        super.updateItem(item, b);
                        if (b) {
                            setText(null);
                        } else {
                            setText(item.getShortDescription());
                            if (item.getDeadLine().isBefore(LocalDate.now()))
                                setTextFill(Color.RED);
                            else if (item.getDeadLine().equals(LocalDate.now()))
                                setTextFill(Color.GREEN);
                        }
                    }
                };
                cell.emptyProperty().addListener(
                        (obs,wasEmpty, isNowEmpty) -> {
                            if (isNowEmpty)
                                cell.setContextMenu(null);
                            else
                                cell.setContextMenu(listContextMenu);
                        }
                );

                return cell;
            }
        });
        listContextMenu.getItems().add(deleteMenu);
    }

    public void showNewItemDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        dialog.setTitle("Add a new todo item");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("todoItemDialog.fxml"));

        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e) {
            System.out.println("Cannot load the window");
            e.printStackTrace();
            return;
        }

        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            System.out.println("OK pressed");
            DialogController controller = fxmlLoader.getController();
            TodoItem item = controller.processResults();
//            todoListView.getItems().setAll(TodoData.getInstance().getTodoItems());
            if (item != null)
                todoListView.getSelectionModel().select(item);
            else
                System.out.println("Please fill all the boxes.No item is added!");
            todoListView.getSelectionModel().selectFirst();
        } else {
            System.out.println("Cancel pressed");
        }
        todoListView.refresh();
    }
    public void showEditItemDialog() {

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit a todo item");
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("todoItemDialog.fxml"));

        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.out.println("Window cannot be opened.");
            return;
        }

        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        TodoItem selectedItem = todoListView.getSelectionModel().getSelectedItem();
        DialogController controller = fxmlLoader.getController();
        controller.editItem(selectedItem);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            TodoItem item = controller.processResults();
            if (item != null) {
                TodoData.getInstance().deleteItem(selectedItem);
                todoListView.getSelectionModel().select(item);
            } else {
                System.out.println("All the fields are mandatory");
                todoListView.getSelectionModel().selectFirst();
            }
        }

    }

    public void delete(TodoItem item) {
        int index = TodoData.getInstance().getTodoItems().indexOf(item);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Todo Item");
        alert.setHeaderText("Delete item : " + item.getShortDescription());
        alert.setContentText("Are you sure? Press OK to confirm and Cancel to go back!");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            TodoData.getInstance().deleteItem(item);
        }

        if (index >= TodoData.getInstance().getTodoItems().size()) {
            todoListView.getSelectionModel().selectFirst();
        } else {
            todoListView.getSelectionModel().select(index);
        }
    }

    @FXML
    public void handleKeyPressed(KeyEvent keyEvent) {
        TodoItem selectedItem = todoListView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            if (keyEvent.getCode().equals(KeyCode.DELETE)) {
                delete(selectedItem);
            }
        }
    }

    @FXML
    public void handleFilterButton() {
        TodoItem selectedItem = todoListView.getSelectionModel().getSelectedItem();
        if (filterToggleButton.isSelected()) {
            filteredList.setPredicate(wantTodays);
        } else {
            filteredList.setPredicate(wantAll);
        }

        if (filteredList.isEmpty()) {
            itemDetailsTextArea.clear();
            deadLineBoxText.setText("");
        } else if (filteredList.contains(selectedItem)) {
            todoListView.getSelectionModel().select(selectedItem);
        } else {
            todoListView.getSelectionModel().selectFirst();
        }
    }

    @FXML
    public void refresh() {
        todoListView.getSelectionModel().selectFirst();
    }

    @FXML
    public void handleExit() {
        Platform.exit();
    }
}
