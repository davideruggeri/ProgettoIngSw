package it.unibs.ing.model;

/**
 * Strategia concreta che filtra le proposte in stato APERTA che hanno ancora posti disponibili per l'iscrizione.
 */
public class FiltroPerPostiDisponibiliStrategy implements FiltroProposteStrategy {

    @Override
    public boolean soddisfaCriterio(Proposta proposta) {
        if (proposta == null) {
            return false;
        }
        return proposta.getStato() == StatoProposta.APERTA && proposta.puoIscrivere();
    }
}
