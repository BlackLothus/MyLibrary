package it.unisa.mylibrary.ui;

import it.unisa.mylibrary.model.Book;
import it.unisa.mylibrary.exception.LibraryException;
import it.unisa.mylibrary.model.Reader;
import it.unisa.mylibrary.service.BookService;
import it.unisa.mylibrary.service.LoanService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;

import java.time.LocalDate;
import java.util.List;

public class LoanFormDialog extends Dialog<Void> {

    private final Reader reader;
    private final BookService bookService;
    private final LoanService loanService;

    private final TextField searchField = new TextField();
    private final ListView<Book> bookList = new ListView<>();
    private final DatePicker dueDatePicker;

    private boolean loanRegistered;

    public LoanFormDialog(Reader reader, BookService bookService, LoanService loanService) {
        this.reader = reader;
        this.bookService = bookService;
        this.loanService = loanService;
        this.loanRegistered = false;

        this.dueDatePicker = new DatePicker(loanService.defaultDueDate());
        this.dueDatePicker.setConverter(UiUtils.createDateConverter());

        initModality(Modality.APPLICATION_MODAL);
        setTitle("Nuovo prestito");
        setHeaderText("Lettore: " + reader.getLastName() + " " + reader.getFirstName());

        ButtonType okType = new ButtonType("Registra", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType("Annulla", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(okType, cancelType);

        searchField.setPromptText("Cerca per titolo, autore o ISBN...");
        searchField.textProperty().addListener((obs, oldV, newV) -> refreshBookList(newV));

        bookList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Book book, boolean empty) {
                super.updateItem(book, empty);
                if (empty || book == null) {
                    setText(null);
                    setDisable(false);
                } else {
                    int availableCopies = loanService.availableCopies(book.getIsbn());
                    setText(book.getTitle() + "  (copie disponibili: " + availableCopies + ")");

                    if (availableCopies <= 0) {
                        setDisable(true);
                    } else {
                        setDisable(false);
                    }
                }
            }
        });
        bookList.setPrefHeight(280);
        bookList.setPrefWidth(460);

        VBox bookSelectionBox = new VBox(8, searchField, bookList);
        VBox.setVgrow(bookList, Priority.ALWAYS);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        grid.add(new Label("Scadenza:"), 0, 0);
        grid.add(dueDatePicker, 1, 0);
        grid.add(new Label("Libro:"), 0, 1);
        grid.add(bookSelectionBox, 1, 1);

        GridPane.setHgrow(bookSelectionBox, Priority.ALWAYS);
        getDialogPane().setContent(grid);

        Button okButton = (Button) getDialogPane().lookupButton(okType);

        bookList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            okButton.setDisable(newVal == null || loanService.availableCopies(newVal.getIsbn()) <= 0);
        });
        okButton.setDisable(true);

        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            String textDate = dueDatePicker.getEditor().getText();
            LocalDate parsedDate = dueDatePicker.getConverter().fromString(textDate);

            if (parsedDate == null) {
                event.consume();
                Alerts.showError("Errore Formato", "La data deve essere nel formato gg/mm/aaaa.");
                return;
            }

            dueDatePicker.setValue(parsedDate);

            Book selectedBook = bookList.getSelectionModel().getSelectedItem();
            LocalDate dueDate = dueDatePicker.getValue();

            try {
                loanService.registerLoan(reader.getStudentId(), selectedBook.getIsbn(), dueDate);
                loanRegistered = true;
            } catch (LibraryException ex) {
                event.consume();
                Alerts.showError(UiUtils.DOMAIN_ERROR_TITLE, ex.getMessage());
            }
        });

        refreshBookList("");
        Platform.runLater(() -> searchField.requestFocus());
    }

    private void refreshBookList(String query) {
        Book selected = bookList.getSelectionModel().getSelectedItem();
        String selectedIsbn = null;
        if (selected != null) {
            selectedIsbn = selected.getIsbn();
        }

        List<Book> items;
        if (query == null || query.trim().isEmpty()) {
            items = bookService.getAllSortedByTitle();
        } else {
            items = bookService.search(query);
        }

        bookList.getItems().setAll(items);

        if (selectedIsbn != null) {
            for (Book b : items) {
                if (selectedIsbn.equals(b.getIsbn())) {
                    bookList.getSelectionModel().select(b);
                    break;
                }
            }
        }
    }

    public boolean isLoanRegistered() {
        return loanRegistered;
    }
}