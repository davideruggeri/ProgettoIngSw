package it.unibs.ing.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Bacheca {

    private Map<String, List<Proposta>> propostePerCategoria;

    public Bacheca() {
        this.propostePerCategoria = new HashMap<>();
    }

    /**
     * Metodo Creator (GRASP) per creare una nuova Proposta associata a una Categoria.
     * 
     * @param categoria la Categoria di riferimento per la Proposta
     * @return la nuova istanza di Proposta
     */
    public Proposta creaProposta(Categoria categoria) {
        return new Proposta(categoria);
    }

    public void aggiungiProposta(Proposta p) {
        String nomeCat = p.getCategoria().getNome();
        propostePerCategoria.putIfAbsent(nomeCat, new ArrayList<>());
        propostePerCategoria.get(nomeCat).add(p);
    }

    public void aggiungiPropostaAperta(Proposta p) {
        if (p.getStato() == StatoProposta.APERTA || p.getStato() == StatoProposta.VALIDA) {
            if (p.getStato() == StatoProposta.VALIDA) {
                p.setStato(StatoProposta.APERTA);
            }
            aggiungiProposta(p);
        } else {

            aggiungiProposta(p);
        }
    }

    public List<Proposta> getProposteApertePerCategoria(String nomeCategoria) {
        List<Proposta> aperte = new ArrayList<>();
        List<Proposta> tutte = propostePerCategoria.getOrDefault(nomeCategoria, new ArrayList<>());
        for (Proposta p : tutte) {
            if (p.getStato() == StatoProposta.APERTA) {
                aperte.add(p);
            }
        }
        return aperte;
    }

    public Map<String, List<Proposta>> getTutteLeProposte() {
        return propostePerCategoria;
    }

    /**
     * Esegue la ricerca filtrata delle proposte applicando il pattern Strategy (GoF).
     * 
     * @param strategy la strategia di filtraggio da applicare
     * @return la lista di proposte che soddisfano i criteri della strategia
     */
    public List<Proposta> cercaProposte(FiltroProposteStrategy strategy) {
        List<Proposta> risultato = new ArrayList<>();
        if (strategy == null) {
            return risultato;
        }
        for (List<Proposta> lista : propostePerCategoria.values()) {
            for (Proposta p : lista) {
                if (strategy.soddisfaCriterio(p)) {
                    risultato.add(p);
                }
            }
        }
        return risultato;
    }
}
