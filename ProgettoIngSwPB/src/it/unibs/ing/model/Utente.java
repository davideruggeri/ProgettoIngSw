package it.unibs.ing.model;

import java.io.Serializable;

public abstract class Utente implements Serializable {
    private static final long serialVersionUID = 1L;

    protected String nomeUtente;
    protected String password;

    public Utente(String nomeUtente, String password) {
        assert nomeUtente != null && !nomeUtente.isBlank();
        assert password != null && !password.isBlank();
        this.nomeUtente = nomeUtente;
        this.password = password;
    }

    public boolean controllaPassword(String password) {
        return this.password.equals(password);
    }

    public String getPassword() {
        return password;
    }

    public String getNomeUtente() {
        return nomeUtente;
    }

    public void setPassword(String nuovaPassword) {
        assert nuovaPassword != null && !nuovaPassword.isBlank();
        this.password = nuovaPassword;
    }
}
