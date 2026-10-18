package it.unisa.mylibrary.ui;

import it.unisa.mylibrary.model.Reader;
import it.unisa.mylibrary.service.BookService;
import it.unisa.mylibrary.service.LoanService;
import it.unisa.mylibrary.service.ReaderService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class ReadersView extends BorderPane {

    private final ReaderService readerService;
    private final BookService bookService;
    private final LoanService loanService;

    private final TextField searchField = new TextField();
    private final TableView<Reader> table = new TableView<>();

    public ReadersView(ReaderService readerService, BookService bookService, LoanService loanService) {
        this.readerService = readerService;
        this.bookService = bookService;
        this.loanService = loanService;
        buildUi();
    }

    private void buildUi() {
        setPadding(new Insets(12));

        searchField.setPromptText("Cerca per cognome o matricola...");
        HBox.setHgrow(searchField, Priority.ALWAYS);
        HBox searchBox = new HBox(searchField);
        searchBox.setPadding(new Insets(0, 0, 10, 0));

        TableColumn<Reader, String> idCol = new TableColumn<>("Matricola");
        TableColumn<Reader, String> lastNameCol = new TableColumn<>("Cognome");
        TableColumn<Reader, String> firstNameCol = new TableColumn<>("Nome");
        TableColumn<Reader, String> emailCol = new TableColumn<>("E-mail");

        table.getColumns().addAll(idCol, lastNameCol, firstNameCol, emailCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Button detailBtn = new Button("Scheda Lettore");
        Button newButton = new Button("Nuovo");

        HBox buttons = new HBox(10, detailBtn, newButton);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        buttons.setPadding(new Insets(10, 0, 0, 0));

        setTop(searchBox);
        setCenter(table);
        setBottom(buttons);
    }

    public void refresh() {
        // aggiornamento tabella da implementare
    }
}