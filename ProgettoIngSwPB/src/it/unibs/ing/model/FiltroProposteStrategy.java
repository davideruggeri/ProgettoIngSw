package it.unibs.ing.model;

/**
 * Interfaccia Strategy (GoF) per incapsulare algoritmi di filtraggio e ricerca delle proposte.
 */
public interface FiltroProposteStrategy {
    /**
     * Valuta se una proposta soddisfa il criterio di filtraggio definito dalla strategia.
     * 
     * @param proposta la proposta da valutare
     * @return true se la proposta rispetta i criteri della strategia, false altrimenti
     */
    boolean soddisfaCriterio(Proposta proposta);
}
