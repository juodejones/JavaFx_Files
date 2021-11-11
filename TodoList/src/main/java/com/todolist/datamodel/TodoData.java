package com.todolist.datamodel;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;

public class TodoData {

    private static TodoData instance = new TodoData();
    private static String filename = "TodoListItems.txt";

    private ObservableList<TodoItem> todoItems;
    private DateTimeFormatter formatter;

    public static TodoData getInstance() {
        return instance;
    }

    public TodoData() {
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    }

    public ObservableList<TodoItem> getTodoItems() {
        return todoItems;
    }

    public void addTodoItem(TodoItem item) {
        if (item != null)
            todoItems.add(item);
    }

    public void setTodoItems(List<TodoItem> todoItems) {
        this.todoItems.setAll(todoItems);
    }

    public void loadTodoItems() throws IOException {

        todoItems = FXCollections.observableArrayList();
        Path path = Paths.get(filename);
        BufferedReader br = Files.newBufferedReader(path);

        String input;

        try {
            while ((input = br.readLine()) != null) {
                String[] itemPieces = input.split("\t");

                String shortDescription = itemPieces[0];
                String details = itemPieces[1];
                String dateString = itemPieces[2];

                LocalDate date = LocalDate.parse(dateString, formatter);
                TodoItem item = new TodoItem(shortDescription, details, date);
                todoItems.add(item);
            }
        } finally {
            if (br != null)
                br.close();
        }

    }

    public void storeTodoItems() throws IOException {

        Path path = Paths.get(filename);
        BufferedWriter bw = Files.newBufferedWriter( path);

        try {
            Iterator<TodoItem> iterator = todoItems.iterator();
            while (iterator.hasNext()) {
                TodoItem item = iterator.next();
                bw.write(String.format("%s\t%s\t%s",
                        item.getShortDescription(),
                        item.getDetails(),
                        item.getDeadLine().format(formatter)));
                bw.newLine();
            }
        } finally {
            if (bw != null)
                bw.close();
        }
    }

    public void deleteTodoItemsFile() throws IOException {

        Path path = Paths.get(filename);
        todoItems.clear();

        try {
            boolean result = Files.deleteIfExists(path);
            if (result)
                System.out.println("File is deleted");
            else
                System.out.println("Not deleted");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void deleteItem(TodoItem item) {
        todoItems.remove(item);
    }
}
