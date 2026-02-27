package it.unibs.ing.storage;

import it.unibs.ing.model.Campo;
import it.unibs.ing.model.Categoria;
import it.unibs.ing.model.Configuratore;
import it.unibs.ing.model.Fruitore;
import it.unibs.ing.model.TipoCampo;
import it.unibs.ing.model.Utente;
import it.unibs.ing.controller.GestoreCategorie;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe di utilità per convertire gli oggetti del dominio in formato JSON e
 * viceversa.
 * Implementazione manuale "naive" per evitare dipendenze esterne.
 */
public class JsonUtil {

    // --- WRITER ---

    public static String scriviCategorie(GestoreCategorie gestore) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");

        // Categorie
        sb.append("  \"categorie\": [\n");
        int i = 0;
        for (Categoria c : gestore.getCategorie().values()) {
            sb.append(scriviCategoria(c, "    "));
            if (i < gestore.getCategorie().size() - 1)
                sb.append(",\n");
            else
                sb.append("\n");
            i++;
        }
        sb.append("  ],\n");

        // Campi Base
        sb.append("  \"campiBase\": ");
        sb.append(scriviListaCampi(gestore.getCampiBase(), "  "));
        sb.append(",\n");

        // Campi Comuni
        sb.append("  \"campiComuni\": ");
        sb.append(scriviListaCampi(gestore.getCampiComuni(), "  "));

        sb.append("\n}");
        return sb.toString();
    }

    private static String scriviCategoria(Categoria c, String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("{\n");
        sb.append(indent).append("  \"nome\": \"").append(escape(c.getNome())).append("\",\n");
        sb.append(indent).append("  \"descrizione\": \"").append(escape(c.getDescrizione())).append("\",\n");
        sb.append(indent).append("  \"campi\": ")
                .append(scriviListaCampi(new ArrayList<>(c.getCampi().values()), indent + "  "));
        sb.append("\n").append(indent).append("}");
        return sb.toString();
    }

    private static String scriviListaCampi(List<Campo> campi, String indent) {
        if (campi.isEmpty())
            return "[]";
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        for (int i = 0; i < campi.size(); i++) {
            Campo c = campi.get(i);
            sb.append(indent).append("  {");
            sb.append("\"nome\": \"").append(escape(c.getNome())).append("\", ");
            sb.append("\"descrizione\": \"").append(escape(c.getDescrizione())).append("\", ");
            sb.append("\"obbligatorio\": ").append(c.isObbligatorio()).append(", ");
            sb.append("\"tipo\": \"").append(c.getTipo().name()).append("\"");
            sb.append("}");
            if (i < campi.size() - 1)
                sb.append(",\n");
            else
                sb.append("\n");
        }
        sb.append(indent).append("]");
        return sb.toString();
    }

    public static String scriviUtenti(List<Utente> utenti) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"utenti\": [\n");
        for (int i = 0; i < utenti.size(); i++) {
            Utente u = utenti.get(i);
            sb.append("    {");
            sb.append("\"nomeUtente\": \"").append(escape(u.getNomeUtente())).append("\", ");
            sb.append("\"password\": \"").append(escape(u.getPassword())).append("\", ");
            sb.append("\"ruolo\": \"").append(u instanceof Configuratore ? "CONFIGURATORE" : "FRUITORE").append("\"");
            sb.append("}");
            if (i < utenti.size() - 1)
                sb.append(",\n");
            else
                sb.append("\n");
        }
        sb.append("  ]\n");
        sb.append("}");
        return sb.toString();
    }

    private static String escape(String s) {
        if (s == null)
            return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    // --- READER ---

    public static GestoreCategorie leggiCategorie(String json) {
        GestoreCategorie gestore = new GestoreCategorie();
        // Parsing molto semplificato basato su regex per trovare blocchi

        // 1. Estrai Categorie
        List<Categoria> categorie = estraiListaCategorie(json);
        for (Categoria c : categorie) {
            try {
                gestore.aggiungiCategoria(c);
            } catch (Exception e) {
                // Ignora duplicati o errori
            }
        }

        // 2. Estrai Campi Comuni (Campi Base sono hardcoded nel costruttore, ma
        // potremmo sovrascriverli se volessimo)
        // Per ora leggiamo solo i Comuni aggiuntivi
        List<Campo> commons = estraiListaCampi(estraiBlocco(json, "campiComuni"));
        for (Campo c : commons) {
            gestore.aggiungiCampoComune(c);
        }

        return gestore;
    }

    public static List<Utente> leggiUtenti(String json) {
        List<Utente> utenti = new ArrayList<>();
        List<String> blocchiUtenti = estraiOggetti(estraiBlocco(json, "utenti"));

        for (String blocco : blocchiUtenti) {
            String nome = estraiValore(blocco, "nomeUtente");
            String pass = estraiValore(blocco, "password");
            String ruolo = estraiValore(blocco, "ruolo");

            if (nome != null && pass != null) {
                if ("CONFIGURATORE".equals(ruolo)) {
                    utenti.add(new Configuratore(nome, pass));
                } else {
                    utenti.add(new Fruitore(nome, pass));
                }
            }
        }
        return utenti;
    }

    // --- HELPER DI PARSING ---

    private static List<Categoria> estraiListaCategorie(String json) {
        List<Categoria> list = new ArrayList<>();
        String arrayContent = estraiBlocco(json, "categorie");
        if (arrayContent == null)
            return list;

        // Categorie contengono campi annidati, qui la regex semplice fallisce se usiamo
        // matcher su }
        // Dobbiamo estrarre gli oggetti bilanciati.
        // Dato che non abbiamo librerie, assumiamo che le graffe siano bilanciate.

        List<String> blocchiCat = estraiOggettiBilanciati(arrayContent);
        for (String b : blocchiCat) {
            String nome = estraiValore(b, "nome");
            String desc = estraiValore(b, "descrizione");
            if (nome != null) {
                Categoria c = new Categoria(nome, desc != null ? desc : "");
                // Campi specifici
                String campiContent = estraiBlocco(b, "campi");
                List<Campo> campi = estraiListaCampi(campiContent);
                for (Campo cmp : campi) {
                    try {
                        c.aggiungiCampo(cmp);
                    } catch (Exception e) {
                    }
                }
                list.add(c);
            }
        }
        return list;
    }

    private static List<Campo> estraiListaCampi(String arrayContent) {
        List<Campo> list = new ArrayList<>();
        if (arrayContent == null)
            return list;

        List<String> blocchi = estraiOggetti(arrayContent);
        for (String b : blocchi) {
            String nome = estraiValore(b, "nome");
            String desc = estraiValore(b, "descrizione");
            String obbl = estraiValore(b, "obbligatorio");
            String tipoStr = estraiValore(b, "tipo");

            if (nome != null && tipoStr != null) {
                boolean obbligatorio = "true".equalsIgnoreCase(obbl);
                TipoCampo tipo = TipoCampo.valueOf(tipoStr);
                list.add(new Campo(nome, desc != null ? desc : "", obbligatorio, tipo));
            }
        }
        return list;
    }

    // Trova il contenuto dentro "key": [ ... ] oppure "key": { ... }
    private static String estraiBlocco(String json, String key) {
        int startKey = json.indexOf("\"" + key + "\"");
        if (startKey == -1)
            return null;

        int startVal = json.indexOf(":", startKey);
        int startBracket = -1;
        int openParams = 0;
        char targetOpen = ' ';
        char targetClose = ' ';

        for (int i = startVal; i < json.length(); i++) {
            char c = json.charAt(i);
            if (startBracket == -1) {
                if (c == '[') {
                    startBracket = i;
                    targetOpen = '[';
                    targetClose = ']';
                    openParams = 1;
                } else if (c == '{') {
                    startBracket = i;
                    targetOpen = '{';
                    targetClose = '}';
                    openParams = 1;
                }
            } else {
                if (c == targetOpen)
                    openParams++;
                else if (c == targetClose)
                    openParams--;

                if (openParams == 0) {
                    return json.substring(startBracket + 1, i);
                }
            }
        }
        return null;
    }

    // Estrae oggetti semplici { ... } da una lista, assumendo no nesting complesso
    // per i campi semplici
    private static List<String> estraiOggetti(String arrayContent) {
        List<String> oggetti = new ArrayList<>();
        if (arrayContent == null)
            return oggetti;

        int open = 0;
        int start = -1;
        for (int i = 0; i < arrayContent.length(); i++) {
            char c = arrayContent.charAt(i);
            if (c == '{') {
                if (open == 0)
                    start = i;
                open++;
            } else if (c == '}') {
                open--;
                if (open == 0 && start != -1) {
                    oggetti.add(arrayContent.substring(start + 1, i));
                    start = -1;
                }
            }
        }
        return oggetti;
    }

    private static List<String> estraiOggettiBilanciati(String content) {
        return estraiOggetti(content); // Per ora la logica è la stessa
    }

    private static String estraiValore(String oggetto, String key) {
        int startKey = oggetto.indexOf("\"" + key + "\"");
        if (startKey == -1)
            return null;

        int startSep = oggetto.indexOf(":", startKey);
        int startVal = startSep + 1;

        // Skip whitespace
        while (startVal < oggetto.length() && Character.isWhitespace(oggetto.charAt(startVal))) {
            startVal++;
        }

        if (startVal >= oggetto.length())
            return null;

        char firstChar = oggetto.charAt(startVal);
        if (firstChar == '\"') {
            // Stringa
            int endVal = oggetto.indexOf("\"", startVal + 1);
            // Gestione escape semplice
            while (endVal > 0 && oggetto.charAt(endVal - 1) == '\\') {
                endVal = oggetto.indexOf("\"", endVal + 1);
            }
            return oggetto.substring(startVal + 1, endVal);
        } else {
            // Booleano o numero (fino a virgola o fine)
            int endVal = startVal;
            while (endVal < oggetto.length() && oggetto.charAt(endVal) != ',' && oggetto.charAt(endVal) != '}') {
                endVal++;
            }
            return oggetto.substring(startVal, endVal).trim();
        }
    }
}
