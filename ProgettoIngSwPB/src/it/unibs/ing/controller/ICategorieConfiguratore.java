package it.unibs.ing.controller;

import it.unibs.ing.model.Campo;
import it.unibs.ing.model.Categoria;
import it.unibs.ing.model.TipoCampo;

public interface ICategorieConfiguratore extends ICategorieFruitore {
    Categoria creaCategoria(String nome, String descrizione, String nomePadre);
    Categoria creaCategoria(String nome, String descrizione);
    void aggiungiCategoria(Categoria categoria, String nomePadre);
    void aggiungiCategoria(Categoria categoria);
    void registraCategoriaSenzaEreditare(Categoria categoria);
    Campo creaCampoComune(String nome, String descrizione, boolean obbligatorio, TipoCampo tipo);
    void aggiungiCampoComune(Campo campo);
    void rimuoviCategoria(String nome);
}
