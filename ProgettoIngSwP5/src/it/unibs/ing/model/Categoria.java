package it.unibs.ing.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Rappresenta una Categoria di eventi (es. "Partita di Calcio", "Concerto").
 * Una categoria definisce quali informazioni (Campi) sono necessarie per creare
 * un evento di quel tipo.
 */
public class Categoria implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nome;
    private String descrizione;
    // Mappa dei campi definiti per questa categoria (NomeCampo -> OggettoCampo)
    private Map<String, Campo> campi;

    // Gestione Gerarchia
    private Categoria padre;
    private List<Categoria> sottocategorie;

    /**
     * Crea una nuova Categoria.
     * 
     * @param nome        Nome della categoria (es. "Sport").
     * @param descrizione Descrizione della categoria.
     */
    public Categoria(String nome, String descrizione) {
        assert nome != null && !nome.isBlank() : "Il nome non può essere nullo o vuoto";

        this.nome = nome;
        this.descrizione = descrizione;
        this.campi = new LinkedHashMap<>(); // Inizializza la mappa dei campi vuota (mantiene ordine)
        this.sottocategorie = new ArrayList<>();
        this.padre = null;
    }

    /**
     * Aggiunge un nuovo campo alla definizione della categoria.
     * 
     * @param campo Il campo da aggiungere.
     * @throws IllegalArgumentException se il campo esiste già o è nullo.
     */
    public void aggiungiCampo(Campo campo) {
        assert campo != null : "Il campo non può essere nullo";
        // Precondizione: Il nome del campo deve essere univoco nella categoria
        if (campi.containsKey(campo.getNome())) {
            throw new IllegalArgumentException("Campo già esistente: " + campo.getNome());
        }
        campi.put(campo.getNome(), campo);
    }

    public void rimuoviCampo(String nomeCampo) {
        campi.remove(nomeCampo);
    }

    public String getNome() {
        return nome;
    }

    public String getDescrizione() {
        return descrizione;
    }

    /**
     * Restituisce una copia della mappa dei campi per preservare l'incapsulamento.
     * 
     * @return Mappa dei campi.
     */
    public Map<String, Campo> getCampi() {
        return new LinkedHashMap<>(campi);
    }

    public Campo getCampo(String nome) {
        return campi.get(nome);
    }

    // Metodi Gerarchia
    public Categoria getPadre() {
        return padre;
    }

    public void setPadre(Categoria padre) {
        this.padre = padre;
    }

    public List<Categoria> getSottocategorie() {
        return new ArrayList<>(sottocategorie);
    }

    /**
     * Aggiunge una sottocategoria a questa categoria.
     * Copia automaticamente i campi della categoria padre nella sottocategoria.
     * 
     * @param sottocategoria La sottocategoria da aggiungere.
     * @throws IllegalArgumentException se la sottocategoria è nulla o esiste già
     *                                  con lo stesso nome.
     */
    public void aggiungiSottocategoria(Categoria sottocategoria) {
        assert sottocategoria != null : "La sottocategoria non può essere nulla";

        for (Categoria sub : sottocategorie) {
            if (sub.getNome().equalsIgnoreCase(sottocategoria.getNome())) {
                throw new IllegalArgumentException("Esiste già una sottocategoria con questo nome.");
            }
        }

        // Eredita tutti i campi dal padre
        for (Campo c : this.campi.values()) {
            try {
                // Creiamo una copia del campo per sicurezza, ma possiamo anche usare gli stessi
                // riferimenti.
                // In questo contesto, usando gli stessi riferimenti la modifica di un campo si
                // riflette anche nei figli (se voluta).
                // Per semplicità e sicurezza, aggiungiamo il riferimento.
                sottocategoria.aggiungiCampo(c);
            } catch (IllegalArgumentException e) {
                // Ignoriamo se il campo è già presente (es. aggiunto prima)
            }
        }

        sottocategoria.setPadre(this);
        this.sottocategorie.add(sottocategoria);
    }

    public void rimuoviSottocategoria(Categoria sottocategoria) {
        this.sottocategorie.remove(sottocategoria);
        sottocategoria.setPadre(null); // Orfana
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Categoria: %s\n%s\nCampi definiti: %d\n", nome, descrizione, campi.size()));
        if (!sottocategorie.isEmpty()) {
            sb.append("Sottocategorie: ");
            for (Categoria sub : sottocategorie) {
                sb.append(sub.getNome()).append(", ");
            }
            sb.setLength(sb.length() - 2); // Rimuovi ultima virgola e spazio
            sb.append("\n");
        }
        return sb.toString();
    }
}
