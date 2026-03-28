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
