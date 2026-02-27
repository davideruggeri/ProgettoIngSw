package it.unibs.ing.model;

import java.io.Serializable;

/**
 * Classe astratta che rappresenta un Utente generico del sistema.
 * Contiene le credenziali di accesso (nome utente e password).
 */
public abstract class Utente implements Serializable {
    private static final long serialVersionUID = 1L;

    protected String nomeUtente;
    protected String password;

    /**
     * Costruttore per creare un nuovo utente.
     * 
     * @param nomeUtente Nome utente univoco.
     * @param password   Password per l'accesso.
     */
    public Utente(String nomeUtente, String password) {
        assert nomeUtente != null && !nomeUtente.isBlank();
        assert password != null && !password.isBlank();
        this.nomeUtente = nomeUtente;
        this.password = password;
    }

    /**
     * Verifica la correttezza della password fornita.
     * 
     * @param password Password da verificare.
     * @return true se la password corrisponde, false altrimenti.
     */
    public boolean controllaPassword(String password) {
        return this.password.equals(password);
    }

    public String getPassword() {
        return password;
    }

    public String getNomeUtente() {
        return nomeUtente;
    }

    /**
     * Imposta una nuova password per l'utente.
     * 
     * @param nuovaPassword La nuova password.
     */
    public void setPassword(String nuovaPassword) {
        assert nuovaPassword != null && !nuovaPassword.isBlank();
        this.password = nuovaPassword;
    }
}
