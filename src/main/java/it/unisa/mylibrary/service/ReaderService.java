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

/**
 * Gestisce l'anagrafica dei lettori applicando le validazioni e le regole di dominio (RF09, RF10, RF12, RF14-RF16).
 */
//Service per la gestione dell'anagrafica lettori e applica le regole di dominio prima di toccare l'archivo
public class ReaderService {
    //ED03
    private static final Pattern FORMATO_EMAIL = Pattern.compile("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$"); //email istituzionale supportata
    private final Archive archive;
    private final LoanService loanService;

    /**
     * Inizializza il servizio associandolo all'archivio e al servizio prestiti.
     *
     * @param archive stato globale della biblioteca
     * @param loanServiceS  servizio prestiti per verificare eventuali pendenze in fase di rimozione
     * @throws LibraryException se {@code archive} o {@code loanServiceS} sono nulli
     */
    public ReaderService(Archive archive, LoanService loanServiceS) {
        if(archive == null) {
            throw new LibraryException("L'archivio non può essere nullo");
        }
        this.archive = archive;
        if (loanServiceS == null) {
            throw new LibraryException("Il LoanService non può essere nullo");
        }
        this.loanService = loanServiceS;
    }

    /**
     * Registra un nuovo lettore in archivio (RF10).
     *
     * @param reader il lettore da inserire
     * @throws LibraryException se mancano dati obbligatori (ED04), l'email non è valida (ED03)
     *         o la matricola è già presente a sistema (RD04b)
     */
    public void addReader(Reader reader) {
        validate(reader);
        // [RD04b] unicità matricola
        if (archive.getReaders().containsKey(reader.getStudentId())) {
            throw new LibraryException("Esiste già un lettore con matricola: " + reader.getStudentId());
        }
        archive.getReaders().put(reader.getStudentId(), reader);
    }

    /**
     * Aggiorna i dati anagrafici di un lettore esistente (RF14).
     *
     * @param reader il lettore con i dati aggiornati
     * @throws LibraryException se i dati non sono validi (ED03, ED04) o la matricola non esiste
     */
    public void updateReader(Reader reader) {
        validate(reader);
        if (!archive.getReaders().containsKey(reader.getStudentId())) {
            throw new LibraryException("Nessun lettore trovato con matricola " + reader.getStudentId() + ".");
        }
        archive.getReaders().put(reader.getStudentId(), reader);
    }

    /**
     * Elimina un lettore dall'anagrafica (RF15).
     *
     * @param studentId matricola del lettore da rimuovere
     * @throws LibraryException se il lettore non esiste o se ha prestiti attivi o conclusi nello storico (RD07, RD15)
     */
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

    /**
     * Cerca un lettore tramite la sua matricola (RF12).
     *
     * @param studentId matricola da cercare
     * @return {@link Optional} col lettore trovato, o vuoto se non esiste
     */
    public Optional<Reader> findByStudentId(String studentId) { //https://docs.oracle.com/javase/8/docs/api/java/util/Optional.html provo ad usarlo per la prima volta, mi ricorda che devo gestire il null assolutamente
        if (studentId ==null){
            return Optional.empty();
        }
        return Optional.ofNullable(archive.getReaders().get(studentId.trim()));
    }

    /**
     * Restituisce tutti i lettori ordinati per cognome e nome (RF09).
     *
     * @return lista dei lettori ordinata alfabeticamente
     */
    //elenco completo per la tabella
    public List<Reader> getAllSortedByName() {
        List<Reader> result = new ArrayList<>(archive.getReaders().values());
        result.sort(byName());
        return result;
    }

    /**
     * Cerca lettori per corrispondenza parziale e case-insensitive su cognome o matricola (RF16).
     *
     * @param query testo da cercare; se nullo o vuoto restituisce l'intera lista ordinata
     * @return lista dei lettori corrispondenti ordinata per cognome e nome
     */
    public List<Reader> search(String query) {
        if (query == null || query.isBlank()) {
            return getAllSortedByName();
        }
        String filtro = query.trim().toLowerCase(Locale.ROOT);
        return archive.getReaders().values().stream()
                .filter(r -> matches(r, filtro))
                .sorted(byName())
                .collect(Collectors.toList());
    }

    //helper
     private static boolean matches(Reader reader, String searchedText) {
         boolean matchLastName = reader.getLastName() != null && reader.getLastName().toLowerCase(Locale.ROOT).contains(searchedText);
         boolean matchMatricola = reader.getStudentId() != null && reader.getStudentId().toLowerCase(Locale.ROOT).contains(searchedText);
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