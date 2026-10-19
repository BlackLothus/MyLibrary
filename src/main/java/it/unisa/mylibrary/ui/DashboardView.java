package it.unisa.mylibrary.ui;

import it.unisa.mylibrary.model.Book;
import it.unisa.mylibrary.model.Loan;
import it.unisa.mylibrary.service.BookService;
import it.unisa.mylibrary.service.LoanService;
import it.unisa.mylibrary.service.ReaderService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

/**
 * Schermata principale (bacheca) che mostra in tempo reale i quattro contatori sintetici della biblioteca (RF25, IU02).
 */

public class DashboardView extends VBox {

    private static final String COLOR_HEADER_TEXT = "#333333";
    private static final String COLOR_CARD_TITLE = "#777777";
    private static final String COLOR_ACTIVE_LOANS = "#2196F3";  // Blu
    private static final String COLOR_OVERDUE_LOANS = "#F44336"; // Rosso
    private static final String COLOR_READERS = "#4CAF50";       // Verde
    private static final String COLOR_BOOKS = "#FF9800";         // Arancione

    private final BookService bookService;
    private final ReaderService readerService;
    private final LoanService loanService;

    private final Label activeLoansLabel = new Label("0");
    private final Label overdueLoansLabel = new Label("0");
    private final Label readersLabel = new Label("0");
    private final Label booksLabel = new Label("0");

    public DashboardView(BookService bookService, ReaderService readerService, LoanService loanService) {
        this.bookService = bookService;
        this.readerService = readerService;
        this.loanService = loanService;

        setAlignment(Pos.CENTER);
        setSpacing(30);
        setPadding(new Insets(20));

        Label title = new Label("Panoramica Biblioteca");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web(COLOR_HEADER_TEXT));

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(20);
        grid.setVgap(20);

        // Creazione delle 4 card richieste dal requisito RF25
        grid.add(createCard("Prestiti Attivi", activeLoansLabel, COLOR_ACTIVE_LOANS), 0, 0);
        grid.add(createCard("Prestiti in Ritardo", overdueLoansLabel, COLOR_OVERDUE_LOANS), 1, 0);
        grid.add(createCard("Lettori Registrati", readersLabel, COLOR_READERS), 0, 1);
        grid.add(createCard("Volumi a Catalogo", booksLabel, COLOR_BOOKS), 1, 1);

        getChildren().addAll(title, grid);
        refresh();
    }

    private VBox createCard(String title, Label valueLabel, String colorHex) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        String cardStyle = String.format(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 8; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0); " +
                        "-fx-border-color: %s; " +
                        "-fx-border-width: 0 0 0 5; " +
                        "-fx-border-radius: 8;",
                colorHex
        );
        card.setStyle(cardStyle);
        card.setPrefSize(260, 160);

        Label titleLabel = new Label(title.toUpperCase());
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        titleLabel.setTextFill(Color.web(COLOR_CARD_TITLE));

        valueLabel.setFont(Font.font("System", FontWeight.BOLD, 48));
        valueLabel.setTextFill(Color.web(colorHex));

        card.getChildren().addAll(titleLabel, valueLabel);
        return card;
    }

    public void refresh() {
        // calcolo i prestiti attivi una volta sola e la riuso per conteggio e ritardi
        List<Loan> activeLoansList = loanService.getActiveLoansSortedByDueDate();
        long activeLoans = activeLoansList.size();
        long overdueLoans = activeLoansList.stream()
                .filter(loanService::isOverdue)
                .count();
        long readers = readerService.getAllSortedByName().size();
        long books = bookService.getAllSortedByTitle().stream()
                .mapToInt(Book::getTotalCopies)
                .sum();

        activeLoansLabel.setText(String.valueOf(activeLoans));
        overdueLoansLabel.setText(String.valueOf(overdueLoans));
        readersLabel.setText(String.valueOf(readers));
        booksLabel.setText(String.valueOf(books));
    }
}