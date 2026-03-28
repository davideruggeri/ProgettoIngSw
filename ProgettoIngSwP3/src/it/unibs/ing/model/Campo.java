package it.unibs.ing.model;

import java.io.Serializable;

public class Campo implements Serializable {

    private static final long serialVersionUID = 1L;
    private String nome;
    private String descrizione;
    private boolean obbligatorio;
    private TipoCampo tipo;

    public Campo(String nome, String descrizione, boolean obbligatorio, TipoCampo tipo) {
        assert nome != null && !nome.isBlank() : "Il nome non può essere nullo o vuoto";
        assert descrizione != null : "La descrizione non può essere nulla";
        assert tipo != null : "Il tipo non può essere nullo";

        this.nome = nome;
        this.descrizione = descrizione;
        this.obbligatorio = obbligatorio;
        this.tipo = tipo;
    }

    public String getNome() {
        return nome;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public boolean isObbligatorio() {
        return obbligatorio;
    }

    public TipoCampo getTipo() {
        return tipo;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public void setObbligatorio(boolean obbligatorio) {
        this.obbligatorio = obbligatorio;
    }

    @Override
    public String toString() {
        return String.format("%s (%s) [%s] - %s", nome, tipo, obbligatorio ? "Obbligatorio" : "Opzionale", descrizione);
    }
}
