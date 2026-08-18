package it.unibs.ing.controller;

import it.unibs.ing.model.Campo;
import it.unibs.ing.model.Categoria;
import java.util.List;
import java.util.Map;

public interface ICategorieFruitore {
    Categoria getCategoria(String nome);
    Map<String, Categoria> getCategorie();
    List<Categoria> getCategorieRadice();
    List<Campo> getCampiBase();
    List<Campo> getCampiComuni();
}
