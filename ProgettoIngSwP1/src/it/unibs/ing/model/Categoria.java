package it.unibs.ing.model;

import java.io.Serializable;
import java.util.HashMap;
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
        this.campi = new HashMap<>(); // Inizializza la mappa dei campi vuota
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
        return new HashMap<>(campi);
    }

    public Campo getCampo(String nome) {
        return campi.get(nome);
    }

    @Override
    public String toString() {
        return String.format("Categoria: %s\n%s\nCampi definiti: %d", nome, descrizione, campi.size());
    }
}
