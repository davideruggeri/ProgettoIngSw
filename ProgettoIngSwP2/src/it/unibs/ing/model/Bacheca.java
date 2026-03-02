package it.unibs.ing.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gestisce l'archivio delle proposte e la bacheca.
 * La bacheca contiene solo proposte in stato APERTA, mentre
 * l'archivio conterrà storicamente anche le altre.
 */
public class Bacheca {

    // Salviamo tutte le proposte, indicizzate per categoria per comodità
    private Map<String, List<Proposta>> propostePerCategoria;

    public Bacheca() {
        this.propostePerCategoria = new HashMap<>();
    }

    /**
     * Aggiunge una proposta alla bacheca, ma solo se è aperta.
     */
    public void aggiungiPropostaAperta(Proposta p) {
        if (p.getStato() == StatoProposta.APERTA) {
            String nomeCat = p.getCategoria().getNome();
            propostePerCategoria.putIfAbsent(nomeCat, new ArrayList<>());
            propostePerCategoria.get(nomeCat).add(p);
        } else {
            throw new IllegalArgumentException("Si possono aggiungere in bacheca solo proposte in stato APERTA.");
        }
    }

    /**
     * Ritorna tutte le proposte attualmente in bacheca per una data categoria.
     */
    public List<Proposta> getProposteApertePerCategoria(String nomeCategoria) {
        return propostePerCategoria.getOrDefault(nomeCategoria, new ArrayList<>());
    }

    public Map<String, List<Proposta>> getTutteLeProposte() {
        return propostePerCategoria;
    }
}
