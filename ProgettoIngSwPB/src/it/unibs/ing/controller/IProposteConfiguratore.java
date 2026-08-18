package it.unibs.ing.controller;

import it.unibs.ing.model.Bacheca;
import it.unibs.ing.model.Proposta;
import java.util.Map;

public interface IProposteConfiguratore {
    Bacheca getBacheca();
    Map<String, Proposta> getProposteValide();
    void pubblicaProposta(Proposta proposta);
    void controllaScadenze(GestoreSessione sessione);
}
