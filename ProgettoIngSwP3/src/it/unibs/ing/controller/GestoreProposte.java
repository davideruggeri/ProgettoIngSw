package it.unibs.ing.controller;

import it.unibs.ing.model.Bacheca;
import it.unibs.ing.model.Proposta;

/**
 * Controller per la gestione logica delle proposte di iniziativa
 * e dell'interazione con la Bacheca e Archivio.
 */
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

    /**
     * Tenta di validare una proposta. Se non è valida, lancia eccezione.
     * Altrimenti lo stato interno della proposta verrà settato a VALIDA.
     */
    public boolean validaProposta(Proposta proposta) {
        return proposta.verificaValidita();
    }

    /**
     * Pubblica una proposta valida in bacheca.
     */
    public void pubblicaProposta(Proposta proposta) {
        try {
            proposta.pubblica(); // Passa a stato APERTA, throws exception se non valida
            bacheca.aggiungiPropostaAperta(proposta);
        } catch (Exception e) {
            throw new IllegalArgumentException("Errore durante la pubblicazione: " + e.getMessage());
        }
    }
}
