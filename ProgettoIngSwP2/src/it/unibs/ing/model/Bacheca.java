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

    public void aggiungiPropostaAperta(Proposta p) {
        if (p.getStato() == StatoProposta.APERTA) {
            String nomeCat = p.getCategoria().getNome();
            propostePerCategoria.putIfAbsent(nomeCat, new ArrayList<>());
            propostePerCategoria.get(nomeCat).add(p);
        } else {
            throw new IllegalArgumentException("Si possono aggiungere in bacheca solo proposte in stato APERTA.");
        }
    }

    public List<Proposta> getProposteApertePerCategoria(String nomeCategoria) {
        return propostePerCategoria.getOrDefault(nomeCategoria, new ArrayList<>());
    }

    public Map<String, List<Proposta>> getTutteLeProposte() {
        return propostePerCategoria;
    }
}
