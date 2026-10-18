package it.unisa.mylibrary;

import it.unisa.mylibrary.model.Archive;
import it.unisa.mylibrary.persistence.ArchiveStorage;
import it.unisa.mylibrary.service.LibraryStorage;
import it.unisa.mylibrary.service.ArchiveManager;
import it.unisa.mylibrary.service.BookService;
import it.unisa.mylibrary.service.LoanService;
import it.unisa.mylibrary.service.ReaderService;
import it.unisa.mylibrary.ui.BooksView;
import it.unisa.mylibrary.ui.DashboardView;
import it.unisa.mylibrary.ui.LoansView;
import it.unisa.mylibrary.ui.ReadersView;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.time.Clock;

public class App extends Application {

    private static final Path AUTOSAVE_PATH = Path.of("library_autosave.dat");

    private final Archive archive = new Archive();
    private final LoanService loanService = new LoanService(archive, Clock.systemDefaultZone());
    private final BookService bookService = new BookService(archive, loanService);
    private final ReaderService readerService = new ReaderService(archive, loanService);

    private final LibraryStorage storage = new ArchiveStorage();
    private final ArchiveManager archiveManager = new ArchiveManager(archive, storage, AUTOSAVE_PATH);

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        DashboardView dashboardView = new DashboardView(bookService, readerService, loanService);
        BooksView booksView = new BooksView(bookService, readerService, loanService);
        ReadersView readersView = new ReadersView(readerService, bookService, loanService);
        LoansView loansView = new LoansView(loanService, bookService, readerService);

        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(15));
        sidebar.setStyle("-fx-background-color: #2b2b2b;");

        Button btnDashboard = new Button("Bacheca");
        btnDashboard.setMaxWidth(Double.MAX_VALUE);
        btnDashboard.setOnAction(e -> {
            dashboardView.refresh();
            root.setCenter(dashboardView);
        });

        Button btnBooks = new Button("Catalogo Libri");
        btnBooks.setMaxWidth(Double.MAX_VALUE);
        btnBooks.setOnAction(e -> {
            booksView.refresh();
            root.setCenter(booksView);
        });

        Button btnReaders = new Button("Anagrafica Lettori");
        btnReaders.setMaxWidth(Double.MAX_VALUE);
        btnReaders.setOnAction(e -> {
            readersView.refresh();
            root.setCenter(readersView);
        });

        Button btnLoans = new Button("Gestione Prestiti");
        btnLoans.setMaxWidth(Double.MAX_VALUE);
        btnLoans.setOnAction(e -> {
            loansView.refresh();
            root.setCenter(loansView);
        });

        Button btnLoad = new Button("Carica Archivio...");
        btnLoad.setMaxWidth(Double.MAX_VALUE);
        btnLoad.setDisable(true);

        Button btnSave = new Button("Salva con nome...");
        btnSave.setMaxWidth(Double.MAX_VALUE);
        btnSave.setDisable(true);
        // collegare entrambi al FileChooser

        sidebar.getChildren().addAll(btnDashboard, new Separator(), btnBooks, btnReaders, btnLoans, new Separator(), btnLoad, btnSave);

        root.setLeft(sidebar);
        root.setCenter(dashboardView);

        Scene scene = new Scene(root, 1280, 720);
        primaryStage.setTitle("MyLibrary ~ Sistema Gestione Biblioteca");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}