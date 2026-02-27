package it.unibs.ing.storage;

import java.io.*;

import it.unibs.ing.controller.GestoreCategorie;
import java.util.List;
import it.unibs.ing.model.Utente;

/**
 * Utility per il salvataggio e caricamento di oggetti su file JSON.
 */
public class GestoreFile {

    public static void salvaCategorie(GestoreCategorie gestore, String nomeFile) throws IOException {
        assicuraDirectory(nomeFile);
        String json = JsonUtil.scriviCategorie(gestore);
        try (PrintWriter out = new PrintWriter(new FileWriter(nomeFile))) {
            out.write(json);
        }
    }

    public static void salvaUtenti(List<Utente> utenti, String nomeFile) throws IOException {
        assicuraDirectory(nomeFile);
        String json = JsonUtil.scriviUtenti(utenti);
        try (PrintWriter out = new PrintWriter(new FileWriter(nomeFile))) {
            out.write(json);
        }
    }

    private static void assicuraDirectory(String nomeFile) {
        File f = new File(nomeFile);
        File parent = f.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
    }

    public static GestoreCategorie caricaCategorie(String nomeFile) throws IOException {
        String json = leggiFile(nomeFile);
        return JsonUtil.leggiCategorie(json);
    }

    public static List<Utente> caricaUtenti(String nomeFile) throws IOException {
        String json = leggiFile(nomeFile);
        return JsonUtil.leggiUtenti(json);
    }

    private static String leggiFile(String nomeFile) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(nomeFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }
}
