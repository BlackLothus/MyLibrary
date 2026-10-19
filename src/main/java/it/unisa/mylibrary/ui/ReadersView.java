package it.unisa.mylibrary.ui;

import it.unisa.mylibrary.exception.LibraryException;
import it.unisa.mylibrary.model.Reader;
import it.unisa.mylibrary.service.BookService;
import it.unisa.mylibrary.service.LoanService;
import it.unisa.mylibrary.service.ReaderService;

import java.util.List;
import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * Vista dell'anagrafica lettori con elenco ordinato per cognome/nome e ricerca parziale (RF09, RF16, IU17).
 */

public class ReadersView extends BorderPane {

    private final ReaderService readerService;
    private final BookService bookService;
    private final LoanService loanService;

    private final TextField searchField = new TextField();
    private final ObservableList<Reader> items = FXCollections.observableArrayList();
    private final TableView<Reader> table = new TableView<>(items);

    private final Button detailBtn = new Button("Scheda Lettore");
    private final Button newButton = new Button("Nuovo");

    public ReadersView(ReaderService readerService, BookService bookService, LoanService loanService) {
        this.readerService = readerService;
        this.bookService = bookService;
        this.loanService = loanService;
        buildUi();
        refresh();
    }

    public void refresh() {
        Reader selected = table.getSelectionModel().getSelectedItem();
        String selectedId = selected != null ? selected.getStudentId() : null;

        try {
            String query = searchField.getText() == null ? "" : searchField.getText().trim();
            List<Reader> readers = query.isEmpty() ? readerService.getAllSortedByName() : readerService.search(query);
            items.setAll(readers);
        } catch (LibraryException e) {
            Alerts.showError("Errore di lettura", e.getMessage());
            return;
        }

        if (selectedId != null) {
            for (Reader r : items) {
                if (selectedId.equals(r.getStudentId())) {
                    table.getSelectionModel().select(r);
                    break;
                }
            }
        }
    }

    private void buildUi() {
        setPadding(new Insets(12));

        searchField.setPromptText("Cerca per cognome o matricola...");
        searchField.textProperty().addListener((obs, oldValue, newValue) -> refresh());
        HBox.setHgrow(searchField, Priority.ALWAYS);
        HBox searchBox = new HBox(searchField);
        searchBox.setPadding(new Insets(0, 0, 10, 0));

        TableColumn<Reader, String> idCol = UiUtils.textColumn("Matricola", Reader::getStudentId);

        TableColumn<Reader, String> lastNameCol = UiUtils.textColumn("Cognome", Reader::getLastName);

        TableColumn<Reader, String> firstNameCol = UiUtils.textColumn("Nome", Reader::getFirstName);

        TableColumn<Reader, String> emailCol = UiUtils.textColumn("Email", Reader::getEmail);

        table.getColumns().addAll(idCol, lastNameCol, firstNameCol, emailCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("Nessun lettore trovato"));

        detailBtn.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());

        detailBtn.setOnAction(e -> openDetail());
        newButton.setOnAction(e -> onNew());

        table.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && table.getSelectionModel().getSelectedItem() != null) {
                openDetail();
            }
        });

        HBox buttons = new HBox(10, detailBtn, newButton);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        buttons.setPadding(new Insets(10, 0, 0, 0));

        setTop(searchBox);
        setCenter(table);
        setBottom(buttons);
    }

    private void openDetail() {
        Reader r = table.getSelectionModel().getSelectedItem();
        if (r == null) {
            return;
        }
        ReaderDetailDialog dialog = new ReaderDetailDialog(r, bookService, readerService, loanService);
        UiUtils.setOwner(dialog, this);
        dialog.showAndWait().ifPresent(actionTaken -> {
            if (actionTaken) {
                refresh();
            }
        });
    }

    private void onNew() {
        Reader current = null;
        boolean success = false;
        while (!success) {
            ReaderFormDialog dialog = new ReaderFormDialog(current, false);
            UiUtils.setOwner(dialog, this);
            Optional<Reader> result = dialog.showAndWait();
            if (result.isEmpty()) {
                return;
            }
            Reader reader = result.get();
            try {
                readerService.addReader(reader);
                refresh();
                success = true;
            } catch (LibraryException e) {
                Alerts.showError(UiUtils.DOMAIN_ERROR_TITLE, e.getMessage());
                current = reader;
            }
        }
    }
}