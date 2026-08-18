package it.unibs.ing.model;

/**
 * Strategia concreta che filtra le proposte in base allo StatoProposta richiesto.
 */
public class FiltroPerStatoStrategy implements FiltroProposteStrategy {
    private final StatoProposta statoTarget;

    public FiltroPerStatoStrategy(StatoProposta statoTarget) {
        this.statoTarget = statoTarget;
    }

    @Override
    public boolean soddisfaCriterio(Proposta proposta) {
        if (proposta == null || statoTarget == null) {
            return false;
        }
        return proposta.getStato() == statoTarget;
    }
}
