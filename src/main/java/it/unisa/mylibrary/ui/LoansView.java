package it.unisa.mylibrary.ui;

import it.unisa.mylibrary.model.Loan;
import it.unisa.mylibrary.service.BookService;
import it.unisa.mylibrary.service.LoanService;
import it.unisa.mylibrary.service.ReaderService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class LoansView extends BorderPane {

    private final LoanService loanService;
    private final BookService bookService;
    private final ReaderService readerService;

    private final TableView<Loan> table = new TableView<>();
    private final ComboBox<String> sortCombo = new ComboBox<>();

    public LoansView(LoanService loanService, BookService bookService, ReaderService readerService) {
        this.loanService = loanService;
        this.bookService = bookService;
        this.readerService = readerService;

        buildTable();

        sortCombo.getItems().addAll("Data di scadenza", "Data di inizio");
        sortCombo.setValue("Data di scadenza");

        Button detailButton = new Button("Dettaglio");
        Button returnButton = new Button("Restituisci");

        HBox top = new HBox(10, new Label("Ordina per:"), sortCombo);
        top.setAlignment(Pos.CENTER_LEFT);
        top.setPadding(new Insets(10));

        HBox bottom = new HBox(10, detailButton, returnButton);
        bottom.setAlignment(Pos.CENTER_RIGHT);
        bottom.setPadding(new Insets(10));

        setTop(top);
        setCenter(table);
        setBottom(bottom);
    }

    private void buildTable() {
        TableColumn<Loan, String> readerCol = new TableColumn<>("Lettore");
        TableColumn<Loan, String> titleCol = new TableColumn<>("Libro");
        TableColumn<Loan, String> startCol = new TableColumn<>("Inizio");
        TableColumn<Loan, String> dueCol = new TableColumn<>("Scadenza");

        table.getColumns().addAll(readerCol, titleCol, startCol, dueCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    public void refresh() {
        // aggiornamento tabella da implementare
    }
}