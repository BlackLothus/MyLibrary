package it.unisa.mylibrary;

import it.unisa.mylibrary.model.Archive;
import it.unisa.mylibrary.persistence.ArchiveStorage;
import it.unisa.mylibrary.service.LibraryStorage;
import it.unisa.mylibrary.service.ArchiveManager;
import it.unisa.mylibrary.service.BookService;
import it.unisa.mylibrary.service.LoanService;
import it.unisa.mylibrary.service.ReaderService;
import it.unisa.mylibrary.ui.Alerts;
import it.unisa.mylibrary.ui.BooksView;
import it.unisa.mylibrary.ui.DashboardView;
import it.unisa.mylibrary.ui.LoansView;
import it.unisa.mylibrary.ui.ReadersView;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;
import java.time.Clock;
import java.util.Locale;

/**
 * Punto di ingresso dell'applicazione JavaFX: istanzia archivio, servizi e viste e gestisce il menu laterale e la persistenza (IU01-IU04, RF26, RF27, RNF05).
 */

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
        // gli errori inattesi li mostro in un alert, non solo in console
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            throwable.printStackTrace();
            Platform.runLater(() -> {
                String message = throwable.getMessage();
                if (message == null || message.trim().isEmpty()) {
                    message = throwable.getClass().getSimpleName();
                }
                Alerts.showError("Errore Imprevisto", "Si è verificato un errore di sistema inatteso:\n" + message);
            });
        });

        // ripristino l'autosave; se non c'è (primo avvio) carico i dati demo
        try {
            boolean autosaveLoaded = archiveManager.loadAutosave();
            if (!autosaveLoaded) {
                DemoDataSeeder.seedIfEmpty(archive, bookService, readerService);
            }
        } catch (Exception ex) {
            Alerts.showError("Avviso di sistema", ex.getMessage());
        }

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
        btnLoad.setOnAction(e -> loadArchive(primaryStage, dashboardView, booksView, readersView, loansView));

        Button btnSave = new Button("Salva con nome...");
        btnSave.setMaxWidth(Double.MAX_VALUE);
        btnSave.setOnAction(e -> saveArchive(primaryStage));

        sidebar.getChildren().addAll(btnDashboard, new Separator(), btnBooks, btnReaders, btnLoans, new Separator(), btnLoad, btnSave);

        root.setLeft(sidebar);
        root.setCenter(dashboardView);

        // autosave alla chiusura
        primaryStage.setOnCloseRequest(e -> archiveManager.executeAutosave());

        Scene scene = new Scene(root, 1280, 720);
        primaryStage.setTitle("MyLibrary ~ Sistema Gestione Biblioteca");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void loadArchive(Stage stage, DashboardView dashboardView, BooksView booksView, ReadersView readersView, LoansView loansView) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Carica Archivio Biblioteca");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("File DAT", "*.dat"));
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            if (!Alerts.showConfirm("Conferma Caricamento", "Caricando un nuovo archivio, i dati attualmente in memoria andranno sovrascritti. Vuoi procedere?")) {
                return;
            }

            try {
                archiveManager.loadFromFile(file.toPath());
                // aggiorno tutte le viste col nuovo contenuto
                dashboardView.refresh();
                booksView.refresh();
                readersView.refresh();
                loansView.refresh();
                Alerts.showInfo("Caricamento completato", "Archivio caricato da " + file.getName());
            } catch (Exception ex) {
                Alerts.showError("Errore di caricamento", ex.getMessage());
            }
        }
    }

    private void saveArchive(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salva Archivio Biblioteca");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("File DAT", "*.dat"));
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            // forzo l'estensione .dat
            String path = file.getAbsolutePath();
            if (!path.toLowerCase(Locale.ROOT).endsWith(".dat")) {
                path += ".dat";
            }

            try {
                archiveManager.saveToFile(Path.of(path));
                Alerts.showInfo("Salvataggio Completato", "Archivio salvato con successo in:\n" + new File(path).getName());
            } catch (Exception ex) {
                Alerts.showError("Errore di Salvataggio",  ex.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}