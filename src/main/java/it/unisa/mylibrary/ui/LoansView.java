package it.unisa.mylibrary.ui;

import it.unisa.mylibrary.model.Loan;
import it.unisa.mylibrary.service.BookService;
import it.unisa.mylibrary.service.LoanService;
import it.unisa.mylibrary.service.ReaderService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

import java.util.List;

/**
 * Vista generale dei prestiti attivi con ordinamento per scadenza o data di inizio ed evidenziazione dei ritardi (RF17, RF18, IU17, IU18).
 */

public class LoansView extends BorderPane {

    public enum SortMode {
        DUE_DATE("Scadenza"),
        START_DATE("Inizio");

        private final String label;
        SortMode(String label) {
            this.label = label;
        }
        @Override
        public String toString() {
            return label;
        }
    }

    public static class LoanRow {
        private final Loan loan;
        private final String readerName;
        private final String bookTitle;

        public LoanRow(Loan loan, String readerName, String bookTitle) {
            this.loan = loan;
            this.readerName = readerName;
            this.bookTitle = bookTitle;
        }

        public Loan getLoan() { return loan; }
        public String getReaderName() { return readerName; }
        public String getBookTitle() { return bookTitle; }
    }

    private final LoanService loanService;
    private final BookService bookService;
    private final ReaderService readerService;

    private final TableView<LoanRow> table = new TableView<>();
    private final ComboBox<SortMode> sortCombo = new ComboBox<>();

    private final Button detailBtn = new Button("Dettaglio");
    private final Button returnBtn = new Button("Restituisci");

    public LoansView(LoanService loanService, BookService bookService, ReaderService readerService) {
        this.loanService = loanService;
        this.bookService = bookService;
        this.readerService = readerService;

        buildTable();

        sortCombo.getItems().addAll(SortMode.DUE_DATE, SortMode.START_DATE);
        sortCombo.setValue(SortMode.DUE_DATE);
        sortCombo.setOnAction(e -> refresh());

        detailBtn.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        detailBtn.setOnAction(e -> openDetail());

        returnBtn.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        returnBtn.setOnAction(e -> onReturn());

        table.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && table.getSelectionModel().getSelectedItem() != null) {
                openDetail();
            }
        });

        HBox topBar = new HBox(10, new Label("Ordina per:"), sortCombo);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10));

        HBox bottomBar = new HBox(10, detailBtn, returnBtn);
        bottomBar.setAlignment(Pos.CENTER_RIGHT);
        bottomBar.setPadding(new Insets(10));

        setTop(topBar);
        setCenter(table);
        setBottom(bottomBar);

        refresh();
    }

    private void buildTable() {
        TableColumn<LoanRow, String> readerCol = UiUtils.textColumn("Lettore", LoanRow::getReaderName);
        readerCol.setPrefWidth(200);

        TableColumn<LoanRow, String> titleCol = UiUtils.textColumn("Libro", LoanRow::getBookTitle);
        titleCol.setPrefWidth(300);

        TableColumn<LoanRow, String> startCol = UiUtils.dateColumn("Inizio", r -> r.getLoan().getStartDate());
        startCol.setPrefWidth(110);

        TableColumn<LoanRow, String> dueCol = UiUtils.dateColumn("Scadenza", r -> r.getLoan().getDueDate());
        dueCol.setPrefWidth(110);

        table.getColumns().addAll(readerCol, titleCol, startCol, dueCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("Nessun prestito attivo"));

        table.setRowFactory(tv -> new TableRow<>() {
            {
                selectedProperty().addListener((obs, was, is) -> updateRowStyle());
            }
            @Override
            protected void updateItem(LoanRow item, boolean empty) {
                super.updateItem(item, empty);
                updateRowStyle();
            }
            private void updateRowStyle() {
                LoanRow rowItem = getItem();
                if (!isEmpty() && rowItem != null) {
                    if (loanService.isOverdue(rowItem.getLoan()) && !isSelected()) {
                        setStyle(UiUtils.OVERDUE_STYLE);
                    } else {
                        setStyle("");
                    }
                } else {
                    setStyle("");
                }
            }
        });
    }

    public void refresh() {
        List<Loan> activeLoans;
        if (sortCombo.getValue() == SortMode.START_DATE) {
            activeLoans = loanService.getActiveLoansSortedByStartDate();
        } else {
            activeLoans = loanService.getActiveLoansSortedByDueDate();
        }

        ObservableList<LoanRow> rows = FXCollections.observableArrayList();
        for (Loan loan : activeLoans) {
            String readerName = UiUtils.getReaderName(loan.getReaderId(), readerService);
            String bookTitle = UiUtils.getBookTitle(loan.getIsbn(), bookService);
            rows.add(new LoanRow(loan, readerName, bookTitle));
        }
        table.setItems(rows);
    }

    private void openDetail() {
        LoanRow selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            LoanDetailDialog dialog = new LoanDetailDialog(selected.getLoan(), bookService, readerService, loanService);
            UiUtils.setOwner(dialog, this);
            dialog.showAndWait();
            if (dialog.isActionTaken()) {
                refresh();
            }
        }
    }

    private void onReturn() {
        LoanRow selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            boolean success = UiUtils.executeReturnLoan(selected.getLoan(), loanService, selected.getBookTitle());
            if (success) {
                refresh();
            }
        }
    }
}