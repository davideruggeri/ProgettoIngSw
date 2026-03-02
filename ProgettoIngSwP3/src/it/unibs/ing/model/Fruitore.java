package it.unibs.ing.model;

/**
 * Rappresenta l'utente Fruitore (utilizzatore finale).
 * Questo ruolo ha i permessi per visualizzare le Categorie e partecipare.
 */
public class Fruitore extends Utente {
    private static final long serialVersionUID = 1L;

    /**
     * Crea un nuovo Fruitore.
     * 
     * @param nomeUtente Nome utente.
     * @param password   Password.
     */
    public Fruitore(String nomeUtente, String password) {
        super(nomeUtente, password);
    }
}
