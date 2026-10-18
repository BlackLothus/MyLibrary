package it.unisa.mylibrary.ui;

import it.unisa.mylibrary.model.Reader;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class ReaderFormDialog extends Dialog<Reader> {

    private final TextField studentIdField = new TextField();
    private final TextField lastNameField = new TextField();
    private final TextField firstNameField = new TextField();
    private final TextField emailField = new TextField();

    private final Reader initial;
    private boolean saving = false;

    public ReaderFormDialog(Reader initial, boolean editMode) {
        this.initial = initial;
        setTitle(editMode
                ? "Modifica lettore"
                : "Nuovo lettore");
        setHeaderText(editMode
                ? "Modifica i dati del lettore"
                : "inserisci i dati del nuovo lettore");

        if (initial != null) {
            studentIdField.setText(initial.getStudentId());
            lastNameField.setText(initial.getLastName());
            firstNameField.setText(initial.getFirstName());
            emailField.setText(initial.getEmail());
        }

        studentIdField.setDisable(editMode);
        studentIdField.setPromptText("Matricola");
        lastNameField.setPromptText("Cognome");
        firstNameField.setPromptText("Nome");
        emailField.setPromptText("E-mail");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15, 10, 5, 10));

        ColumnConstraints labels = new ColumnConstraints();
        ColumnConstraints fields = new ColumnConstraints(260);
        fields.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(labels, fields);

        grid.addRow(0, new Label("Matricola:"), studentIdField);
        grid.addRow(1, new Label("Cognome:"), lastNameField);
        grid.addRow(2, new Label("Nome:"), firstNameField);
        grid.addRow(3, new Label("E-mail:"), emailField);

        getDialogPane().setContent(grid);

        ButtonType saveType = new ButtonType("Salva", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType("Annulla", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(saveType, cancelType);

        Button saveButton = (Button) getDialogPane().lookupButton(saveType);
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            saving = true;
        });

        setOnCloseRequest(event -> {
            if (!saving && hasUnsavedChanges() && !Alerts.showConfirm(UiUtils.UNSAVED_CHANGES_TITLE, UiUtils.UNSAVED_CHANGES_MSG)) {
                event.consume();
            }
        });

        setResultConverter(button -> {
            if (button == saveType) {
                return new Reader(
                        firstNameField.getText().trim(),
                        lastNameField.getText().trim(),
                        studentIdField.getText().trim(),
                        emailField.getText().trim()
                );
            }
            return null;
        });
    }

    private boolean hasUnsavedChanges() {
        if (initial == null) {
            return !studentIdField.getText().trim().isEmpty() ||
                    !lastNameField.getText().trim().isEmpty() ||
                    !firstNameField.getText().trim().isEmpty() ||
                    !emailField.getText().trim().isEmpty();
        } else {
            return !studentIdField.getText().trim().equals(initial.getStudentId()) ||
                    !lastNameField.getText().trim().equals(initial.getLastName()) ||
                    !firstNameField.getText().trim().equals(initial.getFirstName()) ||
                    !emailField.getText().trim().equals(initial.getEmail());
        }
    }
}