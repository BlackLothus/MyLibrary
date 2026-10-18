package it.unisa.mylibrary.ui;

import it.unisa.mylibrary.exception.LibraryException;
import it.unisa.mylibrary.model.Book;
import it.unisa.mylibrary.model.Loan;
import it.unisa.mylibrary.service.BookService;
import it.unisa.mylibrary.service.LoanService;
import it.unisa.mylibrary.service.ReaderService;

import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class UiUtils {

    public static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final String OVERDUE_STYLE = "-fx-background-color: #ffd6d6;";
    public static final String DOMAIN_ERROR_TITLE = "Errore dominio";

    public static final String UNSAVED_CHANGES_TITLE = "Avviso di Sicurezza";
    public static final String UNSAVED_CHANGES_MSG = "Hai inserito o modificato dei dati non ancora salvati.\nSei sicuro di voler scartare le modifiche e abbandonare la pagina?";

    private UiUtils() {
        throw new AssertionError("Classe di utility! Non va istanziata ^.^");
    }

    public static StringConverter<LocalDate> createDateConverter() {
        return new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return DATE_FMT.format(date);
                }
                return "";
            }
            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.trim().isEmpty()) {
                    try {
                        return LocalDate.parse(string, DATE_FMT);
                    } catch (Exception e) {
                        return null;
                    }
                }
                return null;
            }
        };
    }
    public static void addInfoRow(GridPane g, int row, String label, String value) {
        Label l = new Label(label + ":");
        l.setFont(Font.font(null, FontWeight.BOLD, -1));
        Label v = new Label(value == null ? "" : value);
        v.setWrapText(true);
        g.add(l, 0, row);
        g.add(v, 1, row);
    }

    public static void setupCloseableReadOnlyDialog(Dialog<?> dialog) {
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        Node closeButton = dialog.getDialogPane().lookupButton(ButtonType.CLOSE);
        if (closeButton != null) { //stackOverflow (https://stackoverflow.com/questions/60102226/how-to-create-a-custom-dialog-in-javafx-without-using-any-buttontype-controls)
            closeButton.setVisible(false);
            closeButton.setManaged(false);
        }
    }

    public static String getReaderName(String readerId, ReaderService readerService) {
        return readerService.findByStudentId(readerId)
                .map(r -> r.getLastName() + " " + r.getFirstName())
                .orElse(readerId);
    }

    public static String getBookTitle(String isbn, BookService bookService) {
        return bookService.findByIsbn(isbn)
                .map(Book::getTitle)
                .orElse(isbn);
    }

    public static boolean executeReturnLoan(Loan loan, LoanService loanService, String bookTitle) {
        if (Alerts.showConfirm("Restituzione", "Registrare la restituzione di \"" + bookTitle + "\"?")) {
            try {
                loanService.returnLoan(loan.getId());
                return true;
            } catch (LibraryException ex) {
                Alerts.showError(DOMAIN_ERROR_TITLE, ex.getMessage());
            }
        }
        return false;
    }
}