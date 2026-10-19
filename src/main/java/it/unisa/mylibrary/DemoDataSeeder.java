package it.unisa.mylibrary;

import it.unisa.mylibrary.model.Archive;
import it.unisa.mylibrary.model.Book;
import it.unisa.mylibrary.model.Loan;
import it.unisa.mylibrary.model.Reader;
import it.unisa.mylibrary.service.BookService;
import it.unisa.mylibrary.service.ReaderService;

import java.time.LocalDate;
import java.util.List;

/**
 * Classe di supporto che popola l'archivio con dati dimostrativi al primo avvio se non esiste un autosalvataggio.
 */
// popola l'archivio con dati dimostrativi, solo se è vuoto
public final class DemoDataSeeder {

    private DemoDataSeeder() {
        // classe di utility, non si istanzia
    }

    public static void seedIfEmpty(Archive archive, BookService bookService, ReaderService readerService) {
        if (archive.getBooks().isEmpty() && archive.getReaders().isEmpty()) {

            // 1. Libri (autori nel formato "Cognome Nome")
            bookService.addBook(new Book("9788845292613", "La Compagnia dell'Anello", List.of("Tolkien J.R.R."), 1954, 3));
            bookService.addBook(new Book("9788845292620", "Le due Torri", List.of("Tolkien J.R.R."), 1954, 2));
            bookService.addBook(new Book("9788845292637", "Il ritorno del Re", List.of("Tolkien J.R.R."), 1955, 2));
            bookService.addBook(new Book("9788845268342", "Lo Hobbit", List.of("Tolkien J.R.R."), 1937, 4));
            bookService.addBook(new Book("9788804668238", "1984", List.of("Orwell George"), 1949, 5));
            bookService.addBook(new Book("9788804672464", "Il mondo nuovo", List.of("Huxley Aldous"), 1932, 2));
            bookService.addBook(new Book("9788807900137", "Il fu Mattia Pascal", List.of("Pirandello Luigi"), 1904, 3));
            bookService.addBook(new Book("9788807900113", "Uno, nessuno e centomila", List.of("Pirandello Luigi"), 1926, 2));
            bookService.addBook(new Book("9788804667927", "Il barone rampante", List.of("Calvino Italo"), 1957, 4));
            bookService.addBook(new Book("9788804668344", "Le città invisibili", List.of("Calvino Italo"), 1972, 3));
            bookService.addBook(new Book("9788806207206", "Delitto e castigo", List.of("Dostoevskij Fëdor"), 1866, 2));
            bookService.addBook(new Book("9788806214815", "I fratelli Karamazov", List.of("Dostoevskij Fëdor"), 1880, 1));
            bookService.addBook(new Book("9788806218776", "Guerra e pace", List.of("Tolstoj Lev"), 1869, 2));
            bookService.addBook(new Book("9788806225644", "Anna Karenina", List.of("Tolstoj Lev"), 1877, 3));

            // 2. Lettori
            readerService.addReader(new Reader("Kevin", "Esposito", "0612700011", "k.esposito@studenti.unisa.it"));
            readerService.addReader(new Reader("Fausto", "Calogero", "0612700021", "f.calogero@studenti.unisa.it"));
            readerService.addReader(new Reader("Carmeluzza", "Cuda", "0612700032", "c.cuda@studenti.unisa.it"));
            readerService.addReader(new Reader("Foca", "Bellocco", "0612700042", "f.bellocco@studenti.unisa.it"));

            // 3. Prestiti
            LocalDate today = LocalDate.now();

            // i prestiti li aggiungo direttamente all'archivio saltando LoanService:
            // il service blocca date di inizio/scadenza nel passato, quindi senza questa
            // forzatura non potrei simulare ritardi e storico
            archive.getLoans().add(new Loan("0612700011", "9788804668238", today.minusDays(2), today.plusDays(13)));
            archive.getLoans().add(new Loan("0612700021", "9788845268342", today.minusDays(40), today.minusDays(25)));
            archive.getLoans().add(new Loan("0612700032", "9788806207206", today.minusDays(20), today.minusDays(5)));

            Loan closedLoan = new Loan("0612700042", "9788806218776", today.minusDays(30), today.minusDays(15));
            closedLoan.markReturned(today.minusDays(16));
            archive.getLoans().add(closedLoan);

            Loan oldClosedLoan = new Loan("0612700011", "9788804667927", today.minusDays(60), today.minusDays(45));
            oldClosedLoan.markReturned(today.minusDays(46));
            archive.getLoans().add(oldClosedLoan);
        }
    }
}