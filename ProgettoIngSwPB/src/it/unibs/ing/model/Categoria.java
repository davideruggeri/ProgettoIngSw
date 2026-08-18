package it.unibs.ing.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Categoria implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nome;
    private String descrizione;

    private Map<String, Campo> campi;

    private Categoria padre;
    private List<Categoria> sottocategorie;

    public Categoria(String nome, String descrizione) {
        assert nome != null && !nome.isBlank() : "Il nome non può essere nullo o vuoto";

        this.nome = nome;
        this.descrizione = descrizione;
        this.campi = new LinkedHashMap<>();
        this.sottocategorie = new ArrayList<>();
        this.padre = null;
    }

    /**
     * Metodo Creator (GRASP) per creare ed associare un nuovo Campo a questa Categoria.
     * 
     * @param nome il nome del campo
     * @param descrizione la descrizione del campo
     * @param obbligatorio se il campo è obbligatorio
     * @param tipo il tipo di dato del campo
     * @return l'oggetto Campo appena creato
     */
    public Campo creaCampo(String nome, String descrizione, boolean obbligatorio, TipoCampo tipo) {
        Campo c = new Campo(nome, descrizione, obbligatorio, tipo);
        aggiungiCampo(c);
        return c;
    }

    /**
     * Associa un nuovo campo alla Categoria controllando che non ci siano duplicati.
     * 
     * @param campo l'oggetto Campo da aggiungere
     * @throws IllegalArgumentException se un campo con lo stesso nome esiste già
     */
    public void aggiungiCampo(Campo campo) {
        assert campo != null : "Il campo non può essere nullo";

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

    public Map<String, Campo> getCampi() {
        return new LinkedHashMap<>(campi);
    }

    public Campo getCampo(String nome) {
        return campi.get(nome);
    }

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
     * Metodo Creator (GRASP) per istanziare ed associare una sotto-categoria a questa Categoria.
     * 
     * @param nome nome della sotto-categoria
     * @param descrizione descrizione della sotto-categoria
     * @return la nuova Categoria creata
     */
    public Categoria creaSottocategoria(String nome, String descrizione) {
        Categoria sub = new Categoria(nome, descrizione);
        aggiungiSottocategoria(sub);
        return sub;
    }

    /**
     * Collega una categoria figlia propagando automaticamente i campi 
     * attualmente posseduti (base, comuni e specifici ereditati).
     * 
     * @param sottocategoria il nodo figlio da aggiungere
     * @throws IllegalArgumentException se il nome va in conflitto con figli esistenti
     */
    public void aggiungiSottocategoria(Categoria sottocategoria) {
        assert sottocategoria != null : "La sottocategoria non può essere nulla";

        for (Categoria sub : sottocategorie) {
            if (sub.getNome().equalsIgnoreCase(sottocategoria.getNome())) {
                throw new IllegalArgumentException("Esiste già una sottocategoria con questo nome.");
            }
        }

        for (Campo c : this.campi.values()) {
            try {

                sottocategoria.aggiungiCampo(c);
            } catch (IllegalArgumentException e) {

            }
        }

        sottocategoria.setPadre(this);
        this.sottocategorie.add(sottocategoria);
    }

    public void rimuoviSottocategoria(Categoria sottocategoria) {
        this.sottocategorie.remove(sottocategoria);
        sottocategoria.setPadre(null);
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
            sb.setLength(sb.length() - 2);
            sb.append("\n");
        }
        return sb.toString();
    }
}
