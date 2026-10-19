package it.unisa.mylibrary.ui;

import it.unisa.mylibrary.model.Loan;
import it.unisa.mylibrary.service.BookService;
import it.unisa.mylibrary.service.LoanService;
import it.unisa.mylibrary.service.ReaderService;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Finestra di dettaglio di un prestito con dati in sola lettura e pulsante per la restituzione (RF23, IU14, IU14a, IU14c).
 */

public class LoanDetailDialog extends Dialog<Void> {

    private final Loan loan;
    private final LoanService loanService;
    private boolean actionTaken;

    public LoanDetailDialog(Loan loan, BookService bookService, ReaderService readerService, LoanService loanService) {
        this.loan = loan;
        this.loanService = loanService;
        this.actionTaken = false;

        setTitle("Dettaglio Prestito");
        UiUtils.setupCloseableReadOnlyDialog(this);

        String readerName = UiUtils.getReaderName(loan.getReaderId(), readerService);
        String bookTitle = UiUtils.getBookTitle(loan.getIsbn(), bookService);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(6);

        int row = 0;
        UiUtils.addInfoRow(grid, row++, "Lettore", readerName);
        UiUtils.addInfoRow(grid, row++, "Libro", bookTitle);
        UiUtils.addInfoRow(grid, row++, "ISBN", loan.getIsbn());
        UiUtils.addInfoRow(grid, row++, "Inizio prestito",loan.getStartDate().format(UiUtils.DATE_FMT));
        UiUtils.addInfoRow(grid, row++, "Scadenza prevista", loan.getDueDate().format(UiUtils.DATE_FMT));

        if (loan.isActive()) {
            UiUtils.addInfoRow(grid, row++, "Data odierna", loanService.today().format(UiUtils.DATE_FMT));
        } else {
            if (loan.getReturnDate() != null) {
                UiUtils.addInfoRow(grid, row++, "Data restituzione",loan.getReturnDate().format(UiUtils.DATE_FMT));
            }
        }

        Button returnBtn = new Button("Restituisci");
        if (!loan.isActive()) {
            returnBtn.setDisable(true);
        }

        returnBtn.setOnAction(e -> {
            boolean success = UiUtils.executeReturnLoan(loan, loanService, bookTitle);
            if (success) {
                actionTaken = true;
                close();
            }
        });

        HBox buttonBox = new HBox(returnBtn);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(15, 0, 0, 0));

        VBox root = new VBox(10, grid, buttonBox);
        root.setPadding(new Insets(15));
        getDialogPane().setContent(root);
    }

    public boolean isActionTaken() {
        return actionTaken;
    }
}