package it.unibs.ing.controller;

import it.unibs.ing.model.Campo;
import it.unibs.ing.model.Categoria;
import it.unibs.ing.model.TipoCampo;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GestoreCategorie implements Serializable {
    private static final long serialVersionUID = 1L;

    private Map<String, Categoria> categorie;

    private final List<Campo> campiBase;

    private List<Campo> campiComuni;

    public GestoreCategorie() {
        this.categorie = new HashMap<>();
        this.campiBase = inizializzaCampiBase();
        this.campiComuni = new ArrayList<>();
    }

    private List<Campo> inizializzaCampiBase() {
        List<Campo> base = new ArrayList<>();
        base.add(new Campo("Titolo", "Nome di fantasia dell'iniziativa", true, TipoCampo.STRINGA));
        base.add(new Campo("Numero di partecipanti", "Numero persone da coinvolgere", true, TipoCampo.INTERO));
        base.add(new Campo("Termine ultimo di iscrizione", "Ultimo giorno utile per iscriversi", true, TipoCampo.DATA));
        base.add(new Campo("Luogo", "Indirizzo o luogo di ritrovo", true, TipoCampo.STRINGA));
        base.add(new Campo("Data", "Data di svolgimento o inizio", true, TipoCampo.DATA));
        base.add(new Campo("Ora", "Ora di ritrovo", true, TipoCampo.ORA));
        base.add(new Campo("Quota individuale", "Spesa prevista per partecipante", true, TipoCampo.INTERO));
        base.add(new Campo("Data conclusiva", "Data di conclusione iniziativa", true, TipoCampo.DATA));
        return base;
    }

    public void aggiungiCategoria(Categoria categoria, String nomePadre) {
        assert categoria != null : "La categoria non può essere nulla";
        if (categorie.containsKey(categoria.getNome())) {
            throw new IllegalArgumentException("Categoria già esistente: " + categoria.getNome());
        }

        if (nomePadre == null || nomePadre.isBlank()) {
            for (Campo c : campiBase) {
                try {
                    categoria.aggiungiCampo(c);
                } catch (IllegalArgumentException e) {
                }
            }
            for (Campo c : campiComuni) {
                try {
                    categoria.aggiungiCampo(c);
                } catch (IllegalArgumentException e) {
                }
            }
        } else {
            Categoria padre = getCategoria(nomePadre);
            if (padre == null) {
                throw new IllegalArgumentException("Categoria padre non trovata: " + nomePadre);
            }
            padre.aggiungiSottocategoria(categoria);
        }

        categorie.put(categoria.getNome(), categoria);
    }

    public void aggiungiCategoria(Categoria categoria) {
        aggiungiCategoria(categoria, null);
    }

    public void aggiungiCampoComune(Campo campo) {
        campiComuni.add(campo);
        for (Categoria c : categorie.values()) {
            try {
                c.aggiungiCampo(campo);
            } catch (IllegalArgumentException e) {
                
            }
        }
    }

    public Categoria getCategoria(String nome) {
        return categorie.get(nome);
    }

    public void rimuoviCategoria(String nome) {
        Categoria daRimuovere = categorie.get(nome);
        if (daRimuovere != null) {
            if (daRimuovere.getPadre() != null) {
                daRimuovere.getPadre().rimuoviSottocategoria(daRimuovere);
            }
            rimuoviCategoriaRicorsivamente(daRimuovere);
        }
    }

    private void rimuoviCategoriaRicorsivamente(Categoria cat) {
        List<Categoria> subCats = new ArrayList<>(cat.getSottocategorie());
        for (Categoria sub : subCats) {
            rimuoviCategoriaRicorsivamente(sub);
        }
        categorie.remove(cat.getNome());
    }

    public Map<String, Categoria> getCategorie() {
        return new HashMap<>(categorie);
    }

    public List<Categoria> getCategorieRadice() {
        List<Categoria> radici = new ArrayList<>();
        for (Categoria c : categorie.values()) {
            if (c.getPadre() == null) {
                radici.add(c);
            }
        }
        return radici;
    }

    public List<Campo> getCampiBase() {
        return new ArrayList<>(campiBase);
    }

    public List<Campo> getCampiComuni() {
        return new ArrayList<>(campiComuni);
    }
}
