package it.unisa.mylibrary.ui;

import it.unisa.mylibrary.model.Book;
import it.unisa.mylibrary.service.BookService;
import it.unisa.mylibrary.service.LoanService;
import it.unisa.mylibrary.service.ReaderService;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class BooksView extends VBox {

    private final BookService bookService;
    private final ReaderService readerService;
    private final LoanService loanService;

    private final TableView<Book> table = new TableView<>();
    private final TextField searchField = new TextField();

    public BooksView(BookService bookService, ReaderService readerService, LoanService loanService) {
        this.bookService = bookService;
        this.readerService = readerService;
        this.loanService= loanService;

        setSpacing(10);
        setPadding(new Insets(15));

        setupToolbar();
        setupTable();
    }

    private void setupToolbar() {
        searchField.setPromptText("Cerca per titolo, Autore o ISBN...");

        Button btnSortTitle = new Button("Ordina Titolo");
        Button btnSortAuthor = new Button("Ordina Autore");
        Button btnDetail = new Button("Scheda Libro");
        Button btnAdd = new Button("Nuovo Libro");

        //i setOnAction verranno aggiunti con i Dialog

        HBox toolbar = new HBox(10, searchField, btnSortTitle, btnSortAuthor, new Separator(), btnDetail, btnAdd);
        getChildren().add(toolbar);
    }

    private void setupTable() {
        TableColumn<Book, String> colIsbn = new TableColumn<>("ISBN");
        TableColumn<Book, String> colTitle = new TableColumn<>("Titolo");
        TableColumn<Book, String> colAuthors = new TableColumn<>("Autori");
        TableColumn<Book, Number> colYear = new TableColumn<>("Anno");
        TableColumn<Book, Number> colCopies = new TableColumn<>("Copie Totali");
        TableColumn<Book, Number> colAvailable = new TableColumn<>("Disponibili");

        table.getColumns().addAll(colIsbn, colTitle, colAuthors, colYear, colCopies, colAvailable);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        VBox.setVgrow(table, Priority.ALWAYS);
        getChildren().add(table);
    }

    public void refresh() {
        // aggiornamento tabella da implementare
    }
}