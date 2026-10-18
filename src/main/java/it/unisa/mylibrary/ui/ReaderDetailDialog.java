package it.unisa.mylibrary.ui;

import it.unisa.mylibrary.exception.LibraryException;
import it.unisa.mylibrary.model.Loan;
import it.unisa.mylibrary.model.Reader;
import it.unisa.mylibrary.service.BookService;
import it.unisa.mylibrary.service.LoanService;
import it.unisa.mylibrary.service.ReaderService;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;
import java.util.Optional;

public class ReaderDetailDialog extends Dialog<Boolean> {

    private final Reader reader;
    private final BookService bookService;
    private final ReaderService readerService;
    private final LoanService loanService;

    private VBox loansContainer;
    private VBox historyContainer;

    public ReaderDetailDialog(Reader reader, BookService bookService, ReaderService readerService, LoanService loanService) {
        this.reader = reader;
        this.bookService = bookService;
        this.readerService = readerService;
        this.loanService = loanService;

        setTitle("Scheda lettore");
        setResizable(true);

        UiUtils.setupCloseableReadOnlyDialog(this);
        setResultConverter(dialogButton -> false);

        getDialogPane().setContent(buildContent());
        getDialogPane().setPrefWidth(620);
    }

    private VBox buildContent() {
        loansContainer = new VBox();
        historyContainer = new VBox();
        reloadUI();

        VBox root = new VBox(14, buildInfoGrid(), buildActionButtons(), loansContainer, historyContainer);
        root.setPadding(new Insets(10));
        return root;
    }

    private void reloadUI() {
        reloadLoansUI();
        reloadHistoryUI();
    }

    private GridPane buildInfoGrid() {
        GridPane g = new GridPane();
        g.setHgap(12);
        g.setVgap(6);
        int r = 0;
        UiUtils.addInfoRow(g, r++, "Matricola", reader.getStudentId());
        UiUtils.addInfoRow(g, r++, "Nome", reader.getFirstName());
        UiUtils.addInfoRow(g, r++, "Cognome", reader.getLastName());
        UiUtils.addInfoRow(g, r++, "Email", reader.getEmail());
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
        Reader current = reader;
        boolean success = false;

        while (!success) {
            ReaderFormDialog dialog = new ReaderFormDialog(current, true);
            Optional<Reader> result = dialog.showAndWait();
            if (result.isEmpty()) {
                return;
            }

            try {
                readerService.updateReader(result.get());
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
        String message = "Eliminare il lettore " + reader.getLastName() + " " + reader.getFirstName() + " (matricola " + reader.getStudentId() + ")?";
        if (!Alerts.showConfirm("Conferma Eliminazione", message)) {
            return;
        }
        try {
            readerService.removeReader(reader.getStudentId());
            setResult(true);
            close();
        } catch (LibraryException ex) {
            Alerts.showError(UiUtils.DOMAIN_ERROR_TITLE, ex.getMessage());
        }
    }

    private void reloadLoansUI() {
        loansContainer.getChildren().clear();

        Label title = new Label("Prestiti attivi (Max " + LoanService.MAX_ACTIVE_LOANS + ")");
        title.setFont(Font.font(null, FontWeight.BOLD, 14));

        VBox tableBox = new VBox();
        tableBox.setStyle("-fx-border-color: #ccc; -fx-background-color: white;");

        HBox header = new HBox(10);
        header.setPadding(new Insets(8));
        header.setStyle("-fx-background-color: #e0e0e0; -fx-border-color: #ccc; -fx-border-width: 0 0 1 0;");

        Label hTitle = new Label("Titolo"); hTitle.setPrefWidth(220); hTitle.setFont(Font.font(null, FontWeight.BOLD, 12));
        Label hStart = new Label("Inizio"); hStart.setPrefWidth(90); hStart.setFont(Font.font(null, FontWeight.BOLD, 12));
        Label hDue = new Label("Scadenza"); hDue.setPrefWidth(90); hDue.setFont(Font.font(null, FontWeight.BOLD, 12));
        Label hAction = new Label("Azione"); hAction.setPrefWidth(80); hAction.setFont(Font.font(null, FontWeight.BOLD, 12));
        header.getChildren().addAll(hTitle, hStart, hDue, hAction);
        tableBox.getChildren().add(header);

        List<Loan> activeLoans = loanService.getActiveLoansOfReader(reader.getStudentId());

        for (int i = 0; i < LoanService.MAX_ACTIVE_LOANS; i++) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(8));
            row.setStyle("-fx-border-color: #eee; -fx-border-width: 0 0 1 0;");

            if (i < activeLoans.size()) {
                Loan loan = activeLoans.get(i);
                String bookTitle = UiUtils.getBookTitle(loan.getIsbn(), bookService);

                Label lblTitle = new Label(bookTitle);
                lblTitle.setPrefWidth(220);

                Label lblStart = new Label(loan.getStartDate().format(UiUtils.DATE_FMT));
                lblStart.setPrefWidth(90);

                Label lblDue = new Label(loan.getDueDate().format(UiUtils.DATE_FMT));
                lblDue.setPrefWidth(90);

                if (loanService.isOverdue(loan)) {
                    row.setStyle(row.getStyle() + UiUtils.OVERDUE_STYLE);
                }

                Button btnReturn = new Button("Restituisci");
                btnReturn.setPrefWidth(80);
                btnReturn.setOnAction(e -> onReturn(loan));

                row.getChildren().addAll(lblTitle, lblStart, lblDue, btnReturn);
            } else {
                Label lblEmpty = new Label("-~- Slot disponibile -~-");
                lblEmpty.setPrefWidth(420);
                lblEmpty.setAlignment(Pos.CENTER);
                lblEmpty.setStyle("-fx-text-fill: #999; -fx-font-style: italic;");

                Button btnAdd = new Button("+");
                btnAdd.setPrefWidth(80);
                btnAdd.setStyle("-fx-font-weight: bold; -fx-text-fill: #2e7d32;");
                // LoanFormDialog da collegare

                row.getChildren().addAll(lblEmpty, btnAdd);
            }
            tableBox.getChildren().add(row);
        }

        loansContainer.getChildren().addAll(title, tableBox);
    }

    private void reloadHistoryUI() {
        historyContainer.getChildren().clear();

        List<Loan> historyLoans = loanService.getReturnedLoansOfReader(reader.getStudentId());

        Label title = new Label("Storico prestiti conclusi (" + historyLoans.size() + ")");
        title.setFont(Font.font(null, FontWeight.BOLD, 14));

        TableView<Loan> table = new TableView<>();

        TableColumn<Loan, String> cTitle = new TableColumn<>("Libro");
        cTitle.setCellValueFactory(c -> new SimpleStringProperty(UiUtils.getBookTitle(c.getValue().getIsbn(), bookService)));
        cTitle.setSortable(false);

        TableColumn<Loan, String> cStart = new TableColumn<>("Inizio");
        cStart.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStartDate().format(UiUtils.DATE_FMT)));
        cStart.setSortable(false);

        TableColumn<Loan, String> cDue = new TableColumn<>("Scadenza");
        cDue.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDueDate().format(UiUtils.DATE_FMT)));
        cDue.setSortable(false);

        TableColumn<Loan, String> cReturn = new TableColumn<>("Restituito");
        cReturn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getReturnDate().format(UiUtils.DATE_FMT)));
        cReturn.setSortable(false);

        table.getColumns().addAll(java.util.List.of(cTitle, cStart, cDue, cReturn));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("Nessun prestito concluso"));
        table.setPrefHeight(150);

        table.getItems().setAll(historyLoans);

        historyContainer.getChildren().addAll(title, table);
        historyContainer.setSpacing(8);
        historyContainer.setPadding(new Insets(10, 0, 0, 0));
    }

    private void onReturn(Loan loan) {
        String title = UiUtils.getBookTitle(loan.getIsbn(), bookService);
        if (UiUtils.executeReturnLoan(loan, loanService, title)) {
            reloadUI();
        }
    }
}