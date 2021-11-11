package com.todolist;

import com.todolist.datamodel.TodoData;
import com.todolist.datamodel.TodoItem;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.LocalDate;

public class DialogController {

    @FXML
    private TextField shortDescriptionField;

    @FXML
    private TextArea detailsArea;

    @FXML
    private DatePicker deadLinePicker;

    public TodoItem processResults() {
        String shortDescription = shortDescriptionField.getText().trim();
        String details = detailsArea.getText().trim();
        LocalDate date = deadLinePicker.getValue();
        TodoItem newItem;

        if (shortDescription.isEmpty() || details.isEmpty() || date == null) {
            //Do nothing
            newItem = null;
        } else {
            newItem = new TodoItem(shortDescription, details, date);
            TodoData.getInstance().addTodoItem(newItem);
        }
        return newItem;
    }

    public void editItem(TodoItem item) {
        shortDescriptionField.setText(item.getShortDescription());
        detailsArea.setText(item.getDetails());
        deadLinePicker.setValue(item.getDeadLine());
    }
}
