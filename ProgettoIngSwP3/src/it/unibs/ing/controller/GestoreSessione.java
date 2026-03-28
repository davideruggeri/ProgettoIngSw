package it.unibs.ing.controller;

import it.unibs.ing.model.Utente;
import it.unibs.ing.model.Configuratore;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GestoreSessione implements Serializable {
    private static final long serialVersionUID = 1L;

    private transient Utente utenteCorrente;
    private List<Utente> utenti;

    public GestoreSessione() {
        this.utenti = new ArrayList<>();

        utenti.add(new Configuratore("admin", "admin"));
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
            this.utenti.add(new Configuratore("admin", "admin"));
        }
    }

    public boolean isConfiguratore() {
        return utenteCorrente instanceof Configuratore;
    }

    public boolean isFruitore() {
        return utenteCorrente instanceof it.unibs.ing.model.Fruitore;
    }
}
