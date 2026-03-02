package it.unibs.ing.model;

/**
 * Rappresenta l'utente Configuratore.
 * Questo ruolo ha i permessi per creare e gestire le Categorie di eventi.
 */
public class Configuratore extends Utente {
    private static final long serialVersionUID = 1L;

    /**
     * Crea un nuovo Configuratore.
     * 
     * @param nomeUtente Nome utente.
     * @param password   Password.
     */
    public Configuratore(String nomeUtente, String password) {
        super(nomeUtente, password);
    }
}
