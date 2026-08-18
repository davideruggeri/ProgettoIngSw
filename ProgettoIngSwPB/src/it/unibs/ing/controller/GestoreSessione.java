package it.unibs.ing.controller;

import it.unibs.ing.model.Utente;
import it.unibs.ing.model.Configuratore;
import it.unibs.ing.model.Fruitore;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GestoreSessione implements Serializable {
    private static final long serialVersionUID = 1L;

    private transient Utente utenteCorrente;
    private List<Utente> utenti;

    public GestoreSessione() {
        this.utenti = new ArrayList<>();

        creaConfiguratore("admin", "admin");
    }

    /**
     * Metodo Creator (GRASP) per creare e registrare un nuovo Fruitore.
     * 
     * @param nomeUtente username del Fruitore
     * @param password password del Fruitore
     * @return il Fruitore creato
     */
    public Fruitore creaFruitore(String nomeUtente, String password) {
        Fruitore f = new Fruitore(nomeUtente, password);
        this.utenti.add(f);
        return f;
    }

    /**
     * Metodo Creator (GRASP) per creare e registrare un nuovo Configuratore.
     * 
     * @param nomeUtente username del Configuratore
     * @param password password del Configuratore
     * @return il Configuratore creato
     */
    public Configuratore creaConfiguratore(String nomeUtente, String password) {
        Configuratore c = new Configuratore(nomeUtente, password);
        boolean giaEsistente = this.utenti.stream().anyMatch(u -> u.getNomeUtente().equals(nomeUtente));
        if (!giaEsistente) {
            this.utenti.add(c);
        }
        return c;
    }

    public boolean login(String nomeUtente, String password) {
        for (Utente utente : utenti) {
            if (utente.getNomeUtente().equals(nomeUtente) && utente.controllaPassword(password)) {
                this.utenteCorrente = utente;
                return true;
            }
        }
        return false;
    }

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

        boolean hasAdmin = this.utenti.stream().anyMatch(u -> u.getNomeUtente().equals("admin"));
        if (!hasAdmin) {
            creaConfiguratore("admin", "admin");
        }
    }

    public boolean isConfiguratore() {
        return utenteCorrente instanceof Configuratore;
    }

    public boolean isFruitore() {
        return utenteCorrente instanceof it.unibs.ing.model.Fruitore;
    }
}
