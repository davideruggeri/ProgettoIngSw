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
}
