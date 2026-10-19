package it.unisa.mylibrary.ui;

import it.unisa.mylibrary.exception.LibraryException;
import it.unisa.mylibrary.model.Book;
import it.unisa.mylibrary.model.Loan;
import it.unisa.mylibrary.service.BookService;
import it.unisa.mylibrary.service.LoanService;
import it.unisa.mylibrary.service.ReaderService;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Optional;

/**
 * Finestra di dettaglio di un libro con azioni di modifica/eliminazione ed elenco dei prestiti attivi (RF04, IU13, IU13a, IU13b).
 */
public class BookDetailDialog extends Dialog<Boolean> {

    private final Book book;
    private final BookService bookService;
    private final LoanService loanService;
    private final ReaderService readerService;

    public BookDetailDialog(Book book, BookService bookService, LoanService loanService, ReaderService readerService) {
        this.book = book;
        this.bookService = bookService;
        this.loanService = loanService;
        this.readerService = readerService;

        setTitle("Scheda libro");
        setResizable(true);
        UiUtils.setupCloseableReadOnlyDialog(this);
        setResultConverter(dialogButton -> false);

        getDialogPane().setContent(buildContent());
        getDialogPane().setPrefWidth(600);
    }

    private VBox buildContent() {
        VBox root = new VBox(14, buildInfoGrid(), buildActionButtons(), buildLoansSection());
        root.setPadding(new Insets(10));
        return root;
    }

    private GridPane buildInfoGrid() {
        GridPane g = new GridPane();
        g.setHgap(12);
        g.setVgap(6);
        int r = 0;
        UiUtils.addInfoRow(g, r++, "ISBN", book.getIsbn());
        UiUtils.addInfoRow(g, r++, "Titolo", book.getTitle());
        UiUtils.addInfoRow(g, r++, "Autori", String.join(", ", book.getAuthors()));
        UiUtils.addInfoRow(g, r++, "Anno", String.valueOf(book.getYear()));
        UiUtils.addInfoRow(g, r++, "Copie totali", String.valueOf(book.getTotalCopies()));
        UiUtils.addInfoRow(g, r++, "Copie disponibili", String.valueOf(loanService.availableCopies(book.getIsbn())));
        return g;
    }

    private HBox buildActionButtons() {
        Button btnEdit = new Button("Modifica");
        Button btnDelete = new Button("Elimina");

        btnEdit.setOnAction(e -> onEdit());
        btnDelete.setOnAction(e -> onDelete());

        HBox box = new HBox(10, btnEdit, btnDelete);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private void onEdit() {
        Book current = book;
        boolean success = false;

        while (!success) {
            BookFormDialog dialog = new BookFormDialog(current, true);
            UiUtils.setOwner(dialog, getDialogPane());
            Optional<Book> result = dialog.showAndWait();

            if (result.isEmpty()) {
                return;
            }

            try {
                bookService.updateBook(result.get());
                setResult(true);
                close();
                success = true;
            } catch (LibraryException ex) {
                Alerts.showError(UiUtils.DOMAIN_ERROR_TITLE, ex.getMessage());
                current = result.get();
            }
        }
    }

    private void onDelete() {
        if (!Alerts.showConfirm("Conferma Eliminazione", "Eliminare il libro \"" + book.getTitle() + "\"?")) {
            return;
        }
        try {
            bookService.removeBook(book.getIsbn());
            setResult(true);
            close();
        } catch (LibraryException ex) {
            Alerts.showError(UiUtils.DOMAIN_ERROR_TITLE, ex.getMessage());
        }
    }

    private VBox buildLoansSection() {
        Label title = new Label("Prestiti attivi");
        title.setFont(Font.font(null, FontWeight.BOLD, 14));

        TableView<Loan> table = new TableView<>();

        TableColumn<Loan, String> cReader = UiUtils.textColumn("Lettore", l -> UiUtils.getReaderName(l.getReaderId(), readerService));

        TableColumn<Loan, String> cStart = UiUtils.dateColumn("Inizio", Loan::getStartDate);

        TableColumn<Loan, String> cDue = UiUtils.dateColumn("Scadenza", Loan::getDueDate);

        table.getColumns().addAll(cReader, cStart, cDue);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("Nessun prestito attivo"));
        table.setPrefHeight(220);

        table.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Loan loan, boolean empty) {
                super.updateItem(loan, empty);
                if (empty || loan == null || !loanService.isOverdue(loan)) {
                    setStyle("");
                } else {
                    setStyle(UiUtils.OVERDUE_STYLE);
                }
            }
        });

        table.getItems().setAll(loanService.getActiveLoansOfBook(book.getIsbn()));
        VBox.setVgrow(table, Priority.ALWAYS);

        return new VBox(8, title, table);
    }
}