package it.unibs.ing.controller;

import it.unibs.ing.model.Campo;
import it.unibs.ing.model.Categoria;
import it.unibs.ing.model.TipoCampo;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Classe Controller che gestisce tutte le operazioni relative alle Categorie.
 * Mantiene l'elenco delle categorie esistenti e permette di aggiungerne di
 * nuove.
 * Gestisce inoltre i Campi Base (immutabili) e i Campi Comuni (modificabili).
 */
public class GestoreCategorie implements Serializable {
    private static final long serialVersionUID = 1L;

    private Map<String, Categoria> categorie;

    // Lista dei Campi Base (obbligatori per tutte le categorie)
    private final List<Campo> campiBase;

    // Lista dei Campi Comuni (facoltativi/obbligatori, personalizzabili dal
    // Configuratore)
    private List<Campo> campiComuni;

    public GestoreCategorie() {
        this.categorie = new HashMap<>();
        this.campiBase = inizializzaCampiBase();
        this.campiComuni = new ArrayList<>();
    }

    /**
     * Inizializza l'elenco dei campi base immutabili come da specifica.
     * 
     * @return Lista dei campi base.
     */
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

    /**
     * Aggiunge una nuova categoria al sistema, includendo automaticamente i campi
     * Base e Comuni.
     * 
     * @param categoria La categoria da aggiungere (con già i suoi campi specifici).
     * @throws IllegalArgumentException se la categoria esiste già.
     */
    public void aggiungiCategoria(Categoria categoria) {
        assert categoria != null : "La categoria non può essere nulla";
        if (categorie.containsKey(categoria.getNome())) {
            throw new IllegalArgumentException("Categoria già esistente: " + categoria.getNome());
        }

        // Aggiunge i campi Base alla categoria
        for (Campo c : campiBase) {
            try {
                categoria.aggiungiCampo(c);
            } catch (IllegalArgumentException e) {
                // Ignora se già presente
            }
        }

        // Aggiunge i campi Comuni correnti alla categoria
        for (Campo c : campiComuni) {
            try {
                categoria.aggiungiCampo(c);
            } catch (IllegalArgumentException e) {
                // Ignora duplicati
            }
        }

        categorie.put(categoria.getNome(), categoria);
    }

    /**
     * Aggiunge un nuovo Campo Comune alla definizione globale.
     * Aggiorna anche tutte le categorie esistenti.
     * 
     * @param campo Il nuovo campo comune.
     */
    public void aggiungiCampoComune(Campo campo) {
        campiComuni.add(campo);
        for (Categoria c : categorie.values()) {
            try {
                c.aggiungiCampo(campo);
            } catch (IllegalArgumentException e) {
                // Già presente, ignoriamo
            }
        }
    }

    /**
     * Recupera una categoria dato il suo nome.
     * 
     * @param nome Il nome della categoria da cercare.
     * @return L'oggetto Categoria corrispondente, o null se non trovata.
     */
    public Categoria getCategoria(String nome) {
        return categorie.get(nome);
    }

    /**
     * Rimuove una categoria dal sistema.
     * 
     * @param nome Nome della categoria da rimuovere.
     */
    public void rimuoviCategoria(String nome) {
        categorie.remove(nome);
    }

    /**
     * Restituisce tutte le categorie presenti nel sistema.
     * 
     * @return Una mappa contenente le categorie.
     */
    public Map<String, Categoria> getCategorie() {
        return new HashMap<>(categorie);
    }

    public List<Campo> getCampiBase() {
        return new ArrayList<>(campiBase);
    }

    public List<Campo> getCampiComuni() {
        return new ArrayList<>(campiComuni);
    }
}
