package it.unibs.ing.controller;

import it.unibs.ing.model.Bacheca;
import it.unibs.ing.model.Proposta;

public class GestoreProposte {
    private Bacheca bacheca;

    public GestoreProposte(Bacheca bacheca) {
        if (bacheca == null) {
            this.bacheca = new Bacheca();
        } else {
            this.bacheca = bacheca;
        }
    }

    public GestoreProposte() {
        this.bacheca = new Bacheca();
    }

    public Bacheca getBacheca() {
        return bacheca;
    }

    public boolean validaProposta(Proposta proposta) {
        return proposta.verificaValidita();
    }

    public void pubblicaProposta(Proposta proposta) {
        try {
            proposta.pubblica();
            bacheca.aggiungiPropostaAperta(proposta);
        } catch (Exception e) {
            throw new IllegalArgumentException("Errore durante la pubblicazione: " + e.getMessage());
        }
    }
}
