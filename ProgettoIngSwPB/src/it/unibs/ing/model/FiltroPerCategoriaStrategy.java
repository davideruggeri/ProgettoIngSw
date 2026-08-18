package it.unibs.ing.model;

/**
 * Strategia concreta che filtra le proposte in base al nome della Categoria.
 */
public class FiltroPerCategoriaStrategy implements FiltroProposteStrategy {
    private final String nomeCategoria;

    public FiltroPerCategoriaStrategy(String nomeCategoria) {
        this.nomeCategoria = nomeCategoria;
    }

    @Override
    public boolean soddisfaCriterio(Proposta proposta) {
        if (proposta == null || proposta.getCategoria() == null || nomeCategoria == null) {
            return false;
        }
        return proposta.getCategoria().getNome().equalsIgnoreCase(nomeCategoria);
    }
}
