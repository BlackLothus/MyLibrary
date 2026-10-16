package it.unisa.mylibrary.service;

import it.unisa.mylibrary.exception.LibraryException;
import it.unisa.mylibrary.model.Archive;
import it.unisa.mylibrary.model.Reader;
import java.util.List;
import java.util.Optional;
 //Service per la gestione dell'anagrafica lettori e applica le regole di dominio prima di toccare l'archivo
public class ReaderService {
    private final Archive archive;
    private final LoanService loanService;

    public ReaderService(Archive archive, LoanService loanService) {
        this.archive = archive;
        this.loanService = loanService;
    }
    //Registra un nuovo utente: verifica campi obbligatori, regex email e univocità matricola (RD04b)
    public void addReader(Reader reader) throws LibraryException {
        // TODO guarda sopra commento
    }

    public void updateReader(Reader reader) throws LibraryException {
        // TODO: verifica esistenza del lettore e rivalidazione dei campi modificati
    }
    //Eliminazione sicura (RD07): vietata se l'utente ha libri in carico o uno storico concluso
    public void removeReader(String studentId) throws LibraryException {
        // TODO
    }

    public Optional<Reader> findByStudentId(String studentId) { //https://docs.oracle.com/javase/8/docs/api/java/util/Optional.html provo ad usarlo per la prima volta, mi ricorda che devo gestire il null assolutamente
        return Optional.empty();
    }
    //elenco completo per la tabella
    public List<Reader> getAllSortedByName() {
        return null;
    }

    public List<Reader> search(String q) {
        // TODO
        return null;
    }
}