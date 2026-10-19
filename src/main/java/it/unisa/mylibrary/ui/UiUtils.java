package it.unisa.mylibrary.ui;

import it.unisa.mylibrary.exception.LibraryException;
import it.unisa.mylibrary.model.Book;
import it.unisa.mylibrary.model.Loan;
import it.unisa.mylibrary.service.BookService;
import it.unisa.mylibrary.service.LoanService;
import it.unisa.mylibrary.service.ReaderService;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

/**
 * Funzioni di utilità grafica condivise tra le viste (formattazione date, colonne tabelle, guardia modifiche non salvate e flusso di restituzione) (IU16, IU18-IU21).
 */

public final class UiUtils {

    public static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final String OVERDUE_STYLE = "-fx-background-color: #ffd6d6;";
    public static final String DOMAIN_ERROR_TITLE = "Errore dominio";

    public static final String UNSAVED_CHANGES_TITLE = "Avviso di Sicurezza";
    public static final String UNSAVED_CHANGES_MSG = "Hai inserito o modificato dei dati non ancora salvati.\nSei sicuro di voler scartare le modifiche e abbandonare la pagina?";

    private UiUtils() {
        throw new AssertionError("Classe di utility! Non va istanziata");
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

    /**
     * Configura una Dialog di sola consultazione aggiungendo un pulsante di chiusura nascosto,
     * necessario a JavaFX per abilitare la chiusura tramite la "X" della finestra.
     */
    public static void setupCloseableReadOnlyDialog(Dialog<?> dialog) {
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        Node closeButton = dialog.getDialogPane().lookupButton(ButtonType.CLOSE);
        if (closeButton != null) { //stackOverflow (https://stackoverflow.com/questions/60102226/how-to-create-a-custom-dialog-in-javafx-without-using-any-buttontype-controls)
            closeButton.setVisible(false);
            closeButton.setManaged(false);
        }
    }
    /**
     * Intercetta la chiusura del form (tasto Annulla o "X") e chiede conferma all'utente
     * se ci sono modifiche non salvate (IU19, IU20, IU21), impostando anche il focus iniziale.
     */
    public static void installUnsavedChangesGuard(Dialog<?> dialog, ButtonType cancelType,
                                                  BooleanSupplier hasDirtyChanges, Node initialFocus) {
        // Annulla: chiedo conferma solo se c'è qualcosa di non salvato
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(cancelType);
        if (cancelButton != null) {
            cancelButton.addEventFilter(ActionEvent.ACTION, event -> {
                if (hasDirtyChanges.getAsBoolean() && !Alerts.showConfirm(UNSAVED_CHANGES_TITLE, UNSAVED_CHANGES_MSG)) {
                    event.consume();
                }
            });
        }

        // stessa cosa per la "X" della finestra, più il focus iniziale
        Platform.runLater(() -> {
            if (dialog.getDialogPane().getScene() != null) {
                Window window = dialog.getDialogPane().getScene().getWindow();
                if (window != null) {
                    window.addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, event -> {
                        if (hasDirtyChanges.getAsBoolean() && !Alerts.showConfirm(UNSAVED_CHANGES_TITLE, UNSAVED_CHANGES_MSG)) {
                            event.consume();
                        }
                    });
                }
            }
            if (initialFocus != null) {
                initialFocus.requestFocus();
            }
        });
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
    /**
     * Centralizza il flusso di restituzione di un libro (principio DRY): chiede conferma con un Alert
     * e delega la chiusura al {@link LoanService} mostrando eventuali errori di dominio (RF23, IU16, IU22).
     *
     * @return {@code true} se l'utente conferma e la restituzione va a buon fine, {@code false} altrimenti
     */
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
    public static void setOwner(Dialog<?> dialog, Node node) {
        if (node != null && node.getScene() != null) {
            dialog.initOwner(node.getScene().getWindow());
        }
    }
    public static <S> TableColumn<S, String> textColumn(String title, Function<S, String> getter) {
        TableColumn<S, String> col = new TableColumn<>(title);
        col.setCellValueFactory(c -> new SimpleStringProperty(getter.apply(c.getValue())));
        col.setSortable(false);
        return col;
    }
    public static <S> TableColumn<S, String> dateColumn(String title, Function<S, LocalDate> getter) {
        return textColumn(title, s -> {
         LocalDate d = getter.apply(s);
         return d != null ? d.format(DATE_FMT) : "";
        });
    }
}