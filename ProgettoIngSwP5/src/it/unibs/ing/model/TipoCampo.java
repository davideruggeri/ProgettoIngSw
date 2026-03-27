package it.unibs.ing.model;

/**
 * Enumerazione che definisce i tipi di dato ammessi per i campi.
 */
public enum TipoCampo {
    STRINGA, // Testo libero
    INTERO, // Numeri interi
    BOOLEANO, // Vero/Falso
    DATA, // Data (senza orario)
    ORA, // Orario
    DOUBLE // Numero decimale
}
