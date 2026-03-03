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
     * Aggiunge una proposta all'archivio generale.
     */
    public void aggiungiProposta(Proposta p) {
        String nomeCat = p.getCategoria().getNome();
        propostePerCategoria.putIfAbsent(nomeCat, new ArrayList<>());
        propostePerCategoria.get(nomeCat).add(p);
    }

    /**
     * Deprecato / di transizione per retrocompatibilità con V2.
     */
    public void aggiungiPropostaAperta(Proposta p) {
        if (p.getStato() == StatoProposta.APERTA || p.getStato() == StatoProposta.VALIDA) {
            if (p.getStato() == StatoProposta.VALIDA) {
                p.setStato(StatoProposta.APERTA);
            }
            aggiungiProposta(p);
        } else {
            // In V3 permettiamo il caricamento di tutte per l'archivio.
            aggiungiProposta(p);
        }
    }

    /**
     * Ritorna tutte le proposte attualmente in bacheca (APERTE) per una data
     * categoria.
     */
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
