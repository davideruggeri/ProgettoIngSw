package it.unibs.ing.controller;

import it.unibs.ing.model.Utente;
import it.unibs.ing.model.Configuratore;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestisce la sessione utente corrente e l'autenticazione.
 * Mantiene un riferimento all'utente loggato.
 */
public class GestoreSessione implements Serializable {
    private static final long serialVersionUID = 1L;

    private transient Utente utenteCorrente;
    private List<Utente> utenti; // Lista predefinita per ora (mock database)

    public GestoreSessione() {
        this.utenti = new ArrayList<>();
        // Inizializza con un Configuratore predefinito per i test
        utenti.add(new Configuratore("admin", "admin"));
    }

    /**
     * Tenta il login con le credenziali fornite.
     * 
     * @param nomeUtente Nome utente.
     * @param password   Password.
     * @return true se il login ha successo, false altrimenti.
     */
    public boolean login(String nomeUtente, String password) {
        for (Utente utente : utenti) {
            if (utente.getNomeUtente().equals(nomeUtente) && utente.controllaPassword(password)) {
                this.utenteCorrente = utente;
                return true;
            }
        }
        return false;
    }

    /**
     * Effettua il logout dell'utente corrente.
     */
    public void logout() {
        this.utenteCorrente = null;
    }

    public Utente getUtenteCorrente() {
        return utenteCorrente;
    }

    public List<Utente> getUtenti() {
        return new ArrayList<>(utenti);
    }

    public Utente getUtente(String nomeUtente) {
        for (Utente u : utenti) {
            if (u.getNomeUtente().equals(nomeUtente)) {
                return u;
            }
        }
        return null;
    }

    public void setUtenti(List<Utente> utenti) {
        this.utenti = new ArrayList<>(utenti);
        // Garantisce che ci sia sempre almeno un configuratore di default
        boolean hasAdmin = this.utenti.stream().anyMatch(u -> u.getNomeUtente().equals("admin"));
        if (!hasAdmin) {
            this.utenti.add(new Configuratore("admin", "admin"));
        }
    }

    /**
     * Verifica se l'utente corrente è un Configuratore.
     * 
     * @return true se l'utente è un Configuratore, false altrimenti.
     */
    public boolean isConfiguratore() {
        return utenteCorrente instanceof Configuratore;
    }

    /**
     * Verifica se l'utente corrente è un Fruitore.
     * 
     * @return true se l'utente è un Fruitore, false altrimenti.
     */
    public boolean isFruitore() {
        return utenteCorrente instanceof it.unibs.ing.model.Fruitore;
    }
}
