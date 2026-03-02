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
 * Mantiene l'elenco delle categorie radice esistenti e permette di aggiungerne
 * di nuove
 * e di gestire la gerarchia.
 * Gestisce inoltre i Campi Base (immutabili) e i Campi Comuni (modificabili).
 */
public class GestoreCategorie implements Serializable {
    private static final long serialVersionUID = 1L;

    // Mappa di tutte le categorie nel sistema, per ricerca rapida (Nome ->
    // Categoria)
    // Contiene sia radici che sottocategorie
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
     * Aggiunge una nuova categoria al sistema (come radice o sottocategoria),
     * includendo automaticamente i campi Base e Comuni (o ereditandoli dal padre).
     * 
     * @param categoria La categoria da aggiungere.
     * @param nomePadre Il nome della categoria padre. Se null, aggiunge come
     *                  radice.
     * @throws IllegalArgumentException se la categoria esiste già o il padre non
     *                                  esiste.
     */
    public void aggiungiCategoria(Categoria categoria, String nomePadre) {
        assert categoria != null : "La categoria non può essere nulla";
        if (categorie.containsKey(categoria.getNome())) {
            throw new IllegalArgumentException("Categoria già esistente: " + categoria.getNome());
        }

        if (nomePadre == null || nomePadre.isBlank()) {
            // È una categoria radice, aggiungiamo campi base e comuni
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
            // È una sottocategoria, il padre la aggiunge e le passa i suoi campi (che
            // includono base e comuni)
            Categoria padre = getCategoria(nomePadre);
            if (padre == null) {
                throw new IllegalArgumentException("Categoria padre non trovata: " + nomePadre);
            }
            padre.aggiungiSottocategoria(categoria);
        }

        categorie.put(categoria.getNome(), categoria);
    }

    /**
     * Metodo di supporto per aggiungere una categoria radice (mantenendo la firma
     * originale per comodità o retrocompatibilità).
     * 
     * @param categoria La categoria da aggiungere come radice.
     */
    public void aggiungiCategoria(Categoria categoria) {
        aggiungiCategoria(categoria, null);
    }

    /**
     * Registra una categoria (già costruita con la gerarchia corretta) nella mappa
     * interna, senza re-eseguire la logica di ereditarietà dei campi.
     * Usato esclusivamente dalla deserializzazione (JsonUtil).
     */
    public void registraCategoriaSenzaEreditare(Categoria categoria) {
        if (!categorie.containsKey(categoria.getNome())) {
            categorie.put(categoria.getNome(), categoria);
        }
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
     * Se la categoria ha un padre, viene rimossa dalle sottocategorie del padre.
     * Rimuove anche a cascata tutte le sue sottocategorie dalla mappa generale.
     * 
     * @param nome Nome della categoria da rimuovere.
     */
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
        // Usa una lista temporanea per evitare ConcurrentModificationException
        List<Categoria> subCats = new ArrayList<>(cat.getSottocategorie());
        for (Categoria sub : subCats) {
            rimuoviCategoriaRicorsivamente(sub);
        }
        categorie.remove(cat.getNome());
    }

    /**
     * Restituisce tutte le categorie presenti nel sistema (mappa piatta).
     * 
     * @return Una mappa contenente tutte le categorie.
     */
    public Map<String, Categoria> getCategorie() {
        return new HashMap<>(categorie);
    }

    /**
     * Restituisce solo le categorie radice (che non hanno padre), utili per la
     * visualizzazione dell'albero base.
     * 
     * @return Una lista di categorie radice.
     */
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
