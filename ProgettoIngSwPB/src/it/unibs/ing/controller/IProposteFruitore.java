package it.unibs.ing.controller;

import it.unibs.ing.model.Bacheca;
import it.unibs.ing.model.FiltroProposteStrategy;
import it.unibs.ing.model.Proposta;
import java.util.List;
import java.util.Map;

public interface IProposteFruitore {
    Bacheca getBacheca();
    Map<String, Proposta> getProposteValide();
    boolean validaProposta(Proposta proposta);
    boolean ritiraProposta(Proposta proposta, GestoreSessione sessione);
    List<Proposta> cercaProposte(FiltroProposteStrategy strategy);
}
