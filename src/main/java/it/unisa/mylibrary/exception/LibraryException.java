package it.unisa.mylibrary.exception;


/**
 * Eccezione unchecked usata per segnalare violazioni delle regole di dominio
 * o errori di persistenza all'interno dell'applicazione (RNF05, IU22).
 */
public class LibraryException extends RuntimeException {

    private static final long serialVersionUID = 1L; // Fissa la versione della classe per la serializzazione, mantenendo la retrocompatibilità con i file già salvati

    /**
     * Crea un'eccezione con il messaggio di errore specificato.
     *
     * @param message descrizione dell'errore destinata all'utente
     */
    public LibraryException(String message) {
        super(message); //usato per lanciare un errore con un messaggio descrittivo
    }

    /**
     * Crea un'eccezione incapsulando anche la causa tecnica originale.
     *
     * @param message descrizione dell'errore
     * @param cause   eccezione di basso livello che ha generato il problema
     */
    public LibraryException(String message, Throwable cause) {
        super(message, cause); //qui oltre al messaggio, incapsula l'errore tecnico
    }
}