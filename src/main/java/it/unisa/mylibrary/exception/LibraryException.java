package it.unisa.mylibrary.exception;

import java.io.Serializable;

public class LibraryException extends RuntimeException implements Serializable {

    private static final long serialVersionUID = 1L; // Fissa la versione della classe per la serializzazione, mantenendo la retrocompatibilità con i file già salvati

    public LibraryException(String message) {

        super(message); //usato per lanciare un errore con un messaggio descrittivo
    }

    public LibraryException(String message, Throwable cause) {

        super(message, cause); //qui oltre al messaggio, incapsula l'errore tecnico
    }
}