package it.unisa.mylibrary.service;

import it.unisa.mylibrary.exception.LibraryException;
import it.unisa.mylibrary.model.Archive;
import it.unisa.mylibrary.model.Reader;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

 //Service per la gestione dell'anagrafica lettori e applica le regole di dominio prima di toccare l'archivo
public class ReaderService {
    //ED03
    private static final Pattern FORMATO_EMAIL = Pattern.compile("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$"); //email istituzionale supportata
    private final Archive archive;
    private final LoanService loanService;

    public ReaderService(Archive arc, LoanService lS) {
        if(arc == null) {
            throw new LibraryException("L'archivio non può essere nullo");
        }
        this.archive = arc;
        if (lS == null) {
            throw new LibraryException("Il LoanService non può essere nullo");
        }
        this.loanService = lS;
    }
    public void addReader(Reader reader) {
        validate(reader);
        // [RD04b] unicità matricola
        if (archive.getReaders().containsKey(reader.getStudentId())) {
            throw new LibraryException("Esiste già un lettore con matricola:" + reader.getStudentId());
        }
        archive.getReaders().put(reader.getStudentId(), reader);
    }

    public void updateReader(Reader reader) {
        validate(reader);
        if (!archive.getReaders().containsKey(reader.getStudentId())) {
            throw new LibraryException("Nessun lettore trovato con matricola " + reader.getStudentId() + ".");
        }
        archive.getReaders().put(reader.getStudentId(), reader);
    }
    //Eliminazione sicura (RD07): vietata se l'utente ha libri in carico o uno storico concluso
    public void removeReader(String studentId) throws LibraryException {
        if (studentId == null || !archive.getReaders().containsKey(studentId)) {
            throw new LibraryException("Attenzione, Nessun lettore trovato con matricola " + studentId);
        }
        int prestitiAttivi = loanService.getActiveLoansOfReader(studentId).size();
        if (prestitiAttivi > 0) {
            throw new LibraryException("Impossibile eliminare: ha " + prestitiAttivi + " prestiti attivi");
        }
        // controllo se ci sono prestiti storicizzati
        int storicoPrestiti = loanService.getReturnedLoansOfReader(studentId).size();
        if (storicoPrestiti > 0) {
            throw new LibraryException("Impossibile eliminare: ha uno storico di " + storicoPrestiti + " prestiti conclusi");
        }

        archive.getReaders().remove(studentId);
    }

    public Optional<Reader> findByStudentId(String studentId) { //https://docs.oracle.com/javase/8/docs/api/java/util/Optional.html provo ad usarlo per la prima volta, mi ricorda che devo gestire il null assolutamente
        if (studentId ==null){
        return Optional.empty();
        }
        return Optional.ofNullable(archive.getReaders().get(studentId.trim()));
    }
    //elenco completo per la tabella
    public List<Reader> getAllSortedByName() {
        List<Reader> result = new ArrayList<>(archive.getReaders().values());
        result.sort(byName());
        return result;
    }

    public List<Reader> search(String q) {
        if (q == null || q.isBlank()) {
            return getAllSortedByName();
        }
        String filtro = q.trim().toLowerCase(Locale.ROOT);
        return archive.getReaders().values().stream()
                .filter(r -> matches(r, filtro))
                .sorted(byName())
                .collect(Collectors.toList());
    }
    //helper
     private static boolean matches(Reader r, String searchedText) {
         boolean matchLastName = r.getLastName() != null && r.getLastName().toLowerCase(Locale.ROOT).contains(searchedText);
         boolean matchMatricola = r.getStudentId() != null && r.getStudentId().toLowerCase(Locale.ROOT).contains(searchedText);
         return matchLastName || matchMatricola;
     }

     private static Comparator<Reader> byName() {
         return Comparator.comparing((Reader r)-> r.getLastName().toLowerCase(Locale.ROOT))
                 .thenComparing(r-> r.getFirstName().toLowerCase(Locale.ROOT));
     }

     //ED04
     private static void validate(Reader reader) {
         if (reader == null) {
             throw new LibraryException("Il lettore non può essere nullo");
         }
         if (reader.getStudentId() == null || reader.getStudentId().isBlank()) {
             throw new LibraryException("Matricola non può essere vuota");
         }
         if (reader.getFirstName() == null || reader.getFirstName().isBlank()) {
             throw new LibraryException("Il nome non può essere vuoto");
         }
         if (reader.getLastName() == null || reader.getLastName().isBlank()) {
             throw new LibraryException("Il cognome non può essere vuoto");
         }
         if (reader.getEmail() == null || reader.getEmail().isBlank() || !FORMATO_EMAIL.matcher(reader.getEmail().trim()).matches()) {
             throw new LibraryException("Indirizzo e-mail istituzionale non valido");
         }
     }
}