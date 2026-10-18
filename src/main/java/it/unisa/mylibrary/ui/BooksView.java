package it.unisa.mylibrary.ui;

import it.unisa.mylibrary.exception.LibraryException;
import it.unisa.mylibrary.model.Book;
import it.unisa.mylibrary.service.BookService;
import it.unisa.mylibrary.service.LoanService;
import it.unisa.mylibrary.service.ReaderService;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;
import java.util.Optional;

public class BooksView extends VBox {
    private enum SortMode { TITLE, AUTHOR }

    private final BookService bookService;
    private final ReaderService readerService;
    private final LoanService loanService;

    private final TableView<Book> table = new TableView<>();
    private final TextField searchField = new TextField();

    private SortMode currentSort = SortMode.TITLE;
    public BooksView(BookService bookService, ReaderService readerService, LoanService loanService) {
        this.bookService = bookService;
        this.readerService = readerService;
        this.loanService= loanService;

        setSpacing(10);
        setPadding(new Insets(15));

        setupToolbar();
        setupTable();
        refresh();
    }

    private void setupToolbar() {
        searchField.setPromptText("Cerca per titolo, Autore o ISBN...");
        searchField.textProperty().addListener((obs, oldV, newV) -> refresh());

        Button btnSortTitle = new Button("Ordina Titolo");
        btnSortTitle.setOnAction(e -> {
            currentSort = SortMode.TITLE;
            refresh();
        });
        Button btnSortAuthor = new Button("Ordina Autore");
        btnSortAuthor.setOnAction(e -> {
            currentSort = SortMode.AUTHOR;
            refresh();
        });
        Button btnDetail = new Button("Scheda Libro");
        btnDetail.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        btnDetail.setOnAction(e -> openDetail());

        table.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && table.getSelectionModel().getSelectedItem() != null) {
                openDetail();
            }
        });
        Button btnAdd = new Button("Nuovo Libro");
        btnAdd.setOnAction(e -> openFormForNew());

        HBox toolbar = new HBox(10, searchField, btnSortTitle, btnSortAuthor, new Separator(), btnDetail, btnAdd);
        getChildren().add(toolbar);
    }

    private void setupTable() {
        TableColumn<Book, String> colIsbn = new TableColumn<>("ISBN");
        colIsbn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getIsbn()));
        colIsbn.setSortable(false); // L'ordinamento è gestito dai bottoni della toolbar

        TableColumn<Book, String> colTitle = new TableColumn<>("Titolo");
        colTitle.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitle()));
        colTitle.setSortable(false);

        TableColumn<Book, String> colAuthors = new TableColumn<>("Autori");
        colAuthors.setCellValueFactory(c -> new SimpleStringProperty(String.join(", ", c.getValue().getAuthors())));
        colAuthors.setSortable(false);

        TableColumn<Book, Number> colYear = new TableColumn<>("Anno");
        colYear.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getYear()));
        colYear.setSortable(false);

        TableColumn<Book, Number> colCopies = new TableColumn<>("Copie Totali");
        colCopies.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getTotalCopies()));
        colCopies.setSortable(false);

        TableColumn<Book, Number> colAvailable = new TableColumn<>("Disponibili");
        // Calcolo dinamico delle giacenze a runtime
        colAvailable.setCellValueFactory(c -> new SimpleIntegerProperty(loanService.availableCopies(c.getValue().getIsbn())));
        colAvailable.setSortable(false);

        table.getColumns().addAll(colIsbn, colTitle, colAuthors, colYear, colCopies, colAvailable);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("Nessun risultato"));

        VBox.setVgrow(table, Priority.ALWAYS);
        getChildren().add(table);
    }

    public void refresh() {
        Book selected = table.getSelectionModel().getSelectedItem();
        String selectedIsbn = selected != null ? selected.getIsbn() : null;

        String filtro = searchField.getText() == null ? "" : searchField.getText().trim();
        List<Book> items;

        if (filtro.isEmpty()) {
            items = (currentSort == SortMode.AUTHOR) ? bookService.getAllSortedByAuthor() : bookService.getAllSortedByTitle();
        } else {
            items = (currentSort == SortMode.AUTHOR) ? bookService.searchSortedByAuthor(filtro) : bookService.search(filtro);
        }

        table.setItems(FXCollections.observableArrayList(items));

        if (selectedIsbn != null) {
            for (Book b : table.getItems()) {
                if (selectedIsbn.equals(b.getIsbn())) {
                    table.getSelectionModel().select(b);
                    break;
                }
            }
        }
    }
    private void openDetail() {
        Book b = table.getSelectionModel().getSelectedItem();
        if (b != null) {
            BookDetailDialog dialog = new BookDetailDialog(b, bookService, loanService, readerService);
            if (getScene() != null) dialog.initOwner(getScene().getWindow());
            dialog.showAndWait().ifPresent(actionTaken -> {
                if (actionTaken) refresh();
            });
        }
    }

    private void openFormForNew() {
        Book current = null;
        boolean success = false;

        while (!success) {
            BookFormDialog dialog = new BookFormDialog(current, false);
            if (getScene() != null) dialog.initOwner(getScene().getWindow());
            Optional<Book> result = dialog.showAndWait();

            if (result.isEmpty()) {
                return;
            }

            try {
                bookService.addBook(result.get());
                refresh();
                success = true;
            } catch (LibraryException e) {
                Alerts.showError(UiUtils.DOMAIN_ERROR_TITLE, e.getMessage());
                current = result.get();
            }
        }
    }
}