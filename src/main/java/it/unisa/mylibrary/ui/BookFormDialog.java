package it.unisa.mylibrary.ui;

import it.unisa.mylibrary.model.Book;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BookFormDialog extends Dialog<Book> {

    private final TextField txtIsbn = new TextField();
    private final TextField txtTitle = new TextField();
    private final TextField txtAuthors = new TextField();
    private final TextField txtYear = new TextField();
    private final TextField txtCopies = new TextField();

    private Book resultBook = null;
    private final Book initial;

    public BookFormDialog(Book initial, boolean editMode) {
        this.initial = initial;
        setTitle(editMode ? "Modifica Libro" : "Aggiungi Libro");

        ButtonType btnSaveType = new ButtonType("Salva", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancelType = new ButtonType("Annulla", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(btnSaveType, btnCancelType);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        txtAuthors.setPromptText("Cognome Nome,...");

        if (initial != null) {
            txtIsbn.setText(initial.getIsbn());
            txtTitle.setText(initial.getTitle());
            txtAuthors.setText(initial.getAuthors() != null ? String.join(", ", initial.getAuthors()) : "");
            txtYear.setText(String.valueOf(initial.getYear()));
            txtCopies.setText(String.valueOf(initial.getTotalCopies()));
        }

        txtIsbn.setEditable(!editMode);

        grid.addRow(0, new Label("ISBN:"), txtIsbn);
        grid.addRow(1, new Label("Titolo:"), txtTitle);
        grid.addRow(2, new Label("Autori (separati da virgola):"), txtAuthors);
        grid.addRow(3, new Label("Anno:"), txtYear);
        grid.addRow(4, new Label("Copie Totali:"), txtCopies);

        getDialogPane().setContent(grid);

        Button saveButton = (Button) getDialogPane().lookupButton(btnSaveType);
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                int year = Integer.parseInt(txtYear.getText().trim());
                int copies = Integer.parseInt(txtCopies.getText().trim());
                List<String> authors = Arrays.stream(txtAuthors.getText().split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());

                resultBook = new Book(txtIsbn.getText().trim(), txtTitle.getText().trim(), authors, year, copies);
            } catch (NumberFormatException ex) {
                event.consume();
                Alerts.showError("Errore di Formato", "Anno e Copie devono essere valori numerici validi");
            }
        });

        setOnCloseRequest(event -> {
            if (hasUnsavedChanges() && !Alerts.showConfirm(UiUtils.UNSAVED_CHANGES_TITLE, UiUtils.UNSAVED_CHANGES_MSG)) {
                event.consume();
            }
        });

        setResultConverter(dialogButton -> {
            if (dialogButton == btnSaveType) {
                return resultBook;
            }
            return null;
        });
    }

    private boolean hasUnsavedChanges() {
        if (initial == null) {
            return !txtIsbn.getText().trim().isEmpty() ||
                    !txtTitle.getText().trim().isEmpty() ||
                    !txtAuthors.getText().trim().isEmpty() ||
                    !txtYear.getText().trim().isEmpty() ||
                    !txtCopies.getText().trim().isEmpty();
        } else {
            String originalAuthors = initial.getAuthors() != null ? String.join(", ", initial.getAuthors()) : "";
            return !txtIsbn.getText().trim().equals(initial.getIsbn()) ||
                    !txtTitle.getText().trim().equals(initial.getTitle()) ||
                    !txtAuthors.getText().trim().equals(originalAuthors) ||
                    !txtYear.getText().trim().equals(String.valueOf(initial.getYear())) ||
                    !txtCopies.getText().trim().equals(String.valueOf(initial.getTotalCopies()));
        }
    }
}