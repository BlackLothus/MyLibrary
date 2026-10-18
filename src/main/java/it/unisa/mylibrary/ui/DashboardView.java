package it.unisa.mylibrary.ui;

import it.unisa.mylibrary.service.BookService;
import it.unisa.mylibrary.service.LoanService;
import it.unisa.mylibrary.service.ReaderService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class DashboardView extends VBox {

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
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(20);
        grid.setVgap(20);

        grid.add(createCard("Prestiti Attivi", activeLoansLabel), 0, 0);
        grid.add(createCard("Prestiti in Ritardo", overdueLoansLabel), 1, 0);
        grid.add(createCard("Lettori Registrati", readersLabel), 0, 1);
        grid.add(createCard("Volumi a Catalogo", booksLabel), 1, 1);

        getChildren().addAll(title, grid);
    }

    private VBox createCard(String title, Label valueLabel) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-background-color: white;");
        card.setPrefSize(260, 160);

        card.getChildren().addAll(new Label(title), valueLabel);
        return card;
    }

    public void refresh() {
        // provvisorio:da implementare con i contatori reali dei service
    }
}