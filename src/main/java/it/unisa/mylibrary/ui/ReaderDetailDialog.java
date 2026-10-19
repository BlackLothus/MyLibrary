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

    private VBox activeLoansBox;
    private VBox historyBox;

    public ReaderDetailDialog(Reader reader, BookService bookService, ReaderService readerService, LoanService loanService) {
        this.reader = reader;
        this.bookService = bookService;
        this.readerService = readerService;
        this.loanService = loanService;

        setTitle("Scheda lettore");
        setResizable(true);

        UiUtils.setupCloseableReadOnlyDialog(this);
        setResultConverter(dialogButton -> false);

        getDialogPane().setContent(buildMainPanel());
        getDialogPane().setPrefWidth(620);
    }

    private VBox buildMainPanel() {
        activeLoansBox = new VBox();
        historyBox = new VBox();
        refreshViews();

        VBox root = new VBox(14, buildInfoGrid(), buildActionButtons(), activeLoansBox, historyBox);
        root.setPadding(new Insets(10));
        return root;
    }

    private void refreshViews() {
        loadActiveLoansTable();
        loadHistoryTable();
    }

    private GridPane buildInfoGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(6);
        int row = 0;
        UiUtils.addInfoRow(grid, row++, "Matricola", reader.getStudentId());
        UiUtils.addInfoRow(grid, row++, "Nome", reader.getFirstName());
        UiUtils.addInfoRow(grid, row++, "Cognome", reader.getLastName());
        UiUtils.addInfoRow(grid, row++, "Email", reader.getEmail());
        return grid;
    }

    private HBox buildActionButtons() {
        Button editBtn = new Button("Modifica");
        Button deleteBtn = new Button("Elimina");

        editBtn.setOnAction(e -> onEdit());
        deleteBtn.setOnAction(e -> onDelete());

        HBox box = new HBox(10, editBtn, deleteBtn);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private void onEdit() {
        Reader currentReader = reader;
        boolean isCompleted = false;

        while (!isCompleted) {
            ReaderFormDialog dialog = new ReaderFormDialog(currentReader, true);
            UiUtils.setOwner(dialog, getDialogPane());
            Optional<Reader> result = dialog.showAndWait();
            if (result.isEmpty()) {
                return;
            }

            try {
                readerService.updateReader(result.get());
                setResult(true);
                close();
                isCompleted = true;
            } catch (LibraryException ex) {
                Alerts.showError(UiUtils.DOMAIN_ERROR_TITLE, ex.getMessage());
                currentReader = result.get();
            }
        }
    }

    private void onDelete() {
        String msg = "Eliminare il lettore " + reader.getLastName() + " " + reader.getFirstName() + " (matricola " + reader.getStudentId() + ")?";
        if (!Alerts.showConfirm("Conferma Eliminazione", msg)) {
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

    private void loadActiveLoansTable() {
        activeLoansBox.getChildren().clear();

        Label title = new Label("Prestiti attivi (Max " + LoanService.MAX_ACTIVE_LOANS + ")");
        title.setFont(Font.font(null, FontWeight.BOLD, 14));

        VBox tableContainer = new VBox();
        tableContainer.setStyle("-fx-border-color: #ccc; -fx-background-color: white;");

        HBox header = new HBox(10);
        header.setPadding(new Insets(8));
        header.setStyle("-fx-background-color: #e0e0e0; -fx-border-color: #ccc; -fx-border-width: 0 0 1 0;");

        Label colTitle = new Label("Titolo"); colTitle.setPrefWidth(220); colTitle.setFont(Font.font(null, FontWeight.BOLD, 12));
        Label colStart = new Label("Inizio"); colStart.setPrefWidth(90); colStart.setFont(Font.font(null, FontWeight.BOLD, 12));
        Label colDue = new Label("Scadenza"); colDue.setPrefWidth(90); colDue.setFont(Font.font(null, FontWeight.BOLD, 12));
        Label colAction = new Label("Azione"); colAction.setPrefWidth(80); colAction.setFont(Font.font(null, FontWeight.BOLD, 12));
        header.getChildren().addAll(colTitle, colStart, colDue, colAction);
        tableContainer.getChildren().add(header);

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

                Button returnBtn = new Button("Restituisci");
                returnBtn.setPrefWidth(80);
                returnBtn.setOnAction(e -> {
                    if (UiUtils.executeReturnLoan(loan, loanService, bookTitle)) {
                        refreshViews();
                    }
                });

                row.getChildren().addAll(lblTitle, lblStart, lblDue, returnBtn);
            } else {
                Label emptyLbl = new Label("--- Slot disponibile ---");
                emptyLbl.setPrefWidth(420);
                emptyLbl.setAlignment(Pos.CENTER);
                emptyLbl.setStyle("-fx-text-fill: #999; -fx-font-style: italic;");

                Button newLoanBtn = new Button("+");
                newLoanBtn.setPrefWidth(80);
                newLoanBtn.setStyle("-fx-font-weight: bold; -fx-text-fill: #2e7d32;");
                newLoanBtn.setOnAction(e -> onNewLoan());

                row.getChildren().addAll(emptyLbl, newLoanBtn);
            }
            tableContainer.getChildren().add(row);
        }

        activeLoansBox.getChildren().addAll(title, tableContainer);
    }

    private void loadHistoryTable() {
        historyBox.getChildren().clear();

        List<Loan> returnedLoans = loanService.getReturnedLoansOfReader(reader.getStudentId());

        Label title = new Label("Storico prestiti conclusi (" + returnedLoans.size() + ")");
        title.setFont(Font.font(null, FontWeight.BOLD, 14));

        TableView<Loan> table = new TableView<>();

        TableColumn<Loan, String> cTitle = UiUtils.textColumn("Libro", l -> UiUtils.getBookTitle(l.getIsbn(), bookService));

        TableColumn<Loan, String> cStart = UiUtils.dateColumn("Inizio", Loan::getStartDate);

        TableColumn<Loan, String> cDue = UiUtils.dateColumn("Scadenza", Loan::getDueDate);

        TableColumn<Loan, String> cReturn = UiUtils.dateColumn("Restituito", Loan::getReturnDate);

        table.getColumns().addAll(cTitle, cStart, cDue, cReturn);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("Nessun prestito concluso in archivio"));
        table.setPrefHeight(150);

        table.getItems().setAll(returnedLoans);

        historyBox.getChildren().addAll(title, table);
        historyBox.setSpacing(8);
        historyBox.setPadding(new Insets(10, 0, 0, 0));
    }

    private void onNewLoan() {
        LoanFormDialog dialog = new LoanFormDialog(reader, bookService, loanService);
        UiUtils.setOwner(dialog, getDialogPane());
        dialog.showAndWait();

        if (dialog.isLoanRegistered()) {
            refreshViews();
        }
    }
}