package it.unibs.ing.storage;

import it.unibs.ing.model.Campo;
import it.unibs.ing.model.Categoria;
import it.unibs.ing.model.Configuratore;
import it.unibs.ing.model.Fruitore;
import it.unibs.ing.model.Proposta;
import it.unibs.ing.model.StatoProposta;
import it.unibs.ing.model.TipoCampo;
import it.unibs.ing.model.Utente;
import it.unibs.ing.controller.GestoreCategorie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        // Categorie (salviamo solo le radici, le sottocategorie saranno annidate)
        sb.append("  \"categorie\": [\n");
        int i = 0;
        List<Categoria> radici = gestore.getCategorieRadice();
        for (Categoria c : radici) {
            sb.append(scriviCategoria(c, "    "));
            if (i < radici.size() - 1)
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

        // Sottocategorie ricorsive
        if (!c.getSottocategorie().isEmpty()) {
            sb.append(",\n").append(indent).append("  \"sottocategorie\": [\n");
            int i = 0;
            List<Categoria> subs = c.getSottocategorie();
            for (Categoria sub : subs) {
                sb.append(scriviCategoria(sub, indent + "    "));
                if (i < subs.size() - 1)
                    sb.append(",\n");
                else
                    sb.append("\n");
                i++;
            }
            sb.append(indent).append("  ]\n");
        } else {
            sb.append("\n"); // chiusura dopo campi
        }

        sb.append(indent).append("}");
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
            if (u instanceof Fruitore) {
                Fruitore f = (Fruitore) u;
                sb.append(", \"notifiche\": [");
                for (int j = 0; j < f.getNotifiche().size(); j++) {
                    sb.append("\"").append(escape(f.getNotifiche().get(j))).append("\"");
                    if (j < f.getNotifiche().size() - 1)
                        sb.append(", ");
                }
                sb.append("]");
            }
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

        // 1. Estrai Campi Comuni PRIMA delle categorie, così il gestore li conosce già
        List<Campo> commons = estraiListaCampi(estraiBlocco(json, "campiComuni"));
        for (Campo c : commons) {
            gestore.aggiungiCampoComune(c);
        }

        // 2. Estrai Categorie (partendo dalle radici)
        List<Categoria> categorieRadice = estraiAlberoCategorie(estraiBlocco(json, "categorie"), gestore);
        for (Categoria c : categorieRadice) {
            try {
                gestore.aggiungiCategoria(c);
                aggiungiSottocategorieRicorsive(gestore, c);
            } catch (Exception e) {
            }
        }

        return gestore;
    }

    public static List<Utente> leggiUtenti(String json) {
        List<Utente> utenti = new ArrayList<>();
        List<String> blocchiUtenti = estraiOggettiTopLevel(estraiBlocco(json, "utenti"));

        for (String blocco : blocchiUtenti) {
            String nome = estraiValore(blocco, "nomeUtente");
            String pass = estraiValore(blocco, "password");
            String ruolo = estraiValore(blocco, "ruolo");

            if (nome != null && pass != null) {
                if ("CONFIGURATORE".equals(ruolo)) {
                    utenti.add(new Configuratore(nome, pass));
                } else {
                    Fruitore f = new Fruitore(nome, pass);
                    String notificheBlocco = estraiBlocco(blocco, "notifiche");
                    if (notificheBlocco != null) {
                        for (String n : estraiArrayStringhe(notificheBlocco)) {
                            f.aggiungiNotifica(n);
                        }
                    }
                    utenti.add(f);
                }
            }
        }
        return utenti;
    }

    // --- HELPER DI PARSING ---

    private static void aggiungiSottocategorieRicorsive(GestoreCategorie gestore, Categoria padre) {
        for (Categoria sub : padre.getSottocategorie()) {
            try {
                // Registra solo nella mappa (il link padre-figlio esiste già in memoria)
                gestore.registraCategoriaSenzaEreditare(sub);
                aggiungiSottocategorieRicorsive(gestore, sub);
            } catch (Exception e) {
            }
        }
    }

    // Nuova implementazione per supportare alberi gerarchici
    private static List<Categoria> estraiAlberoCategorie(String arrayContent, GestoreCategorie gestore) {
        List<Categoria> list = new ArrayList<>();
        if (arrayContent == null || arrayContent.trim().isEmpty())
            return list;

        List<String> blocchiCat = estraiOggettiTopLevel(arrayContent);
        for (String b : blocchiCat) {
            String nome = estraiValore(b, "nome");
            String desc = estraiValore(b, "descrizione");
            if (nome != null) {
                Categoria c = new Categoria(nome, desc != null ? desc : "");

                // Campi specifici
                String campiContent = estraiBlocco(b, "campi");
                List<Campo> campi = estraiListaCampi(campiContent);
                
                // Forza l'ordine: prima i Campi Base
                for (Campo base : gestore.getCampiBase()) {
                    for (Campo salvato : campi) {
                        if (salvato.getNome().equals(base.getNome())) {
                            c.aggiungiCampo(salvato);
                            break;
                        }
                    }
                }
                // Poi i Campi Comuni
                for (Campo comune : gestore.getCampiComuni()) {
                    for (Campo salvato : campi) {
                        if (salvato.getNome().equals(comune.getNome())) {
                            c.aggiungiCampo(salvato);
                            break;
                        }
                    }
                }
                // Poi eventuali altri campi specifici della categoria
                for (Campo cmp : campi) {
                    try {
                        c.aggiungiCampo(cmp);
                    } catch (Exception e) {
                        // Ignorato se già aggiunto
                    }
                }

                // Sottocategorie
                String subContent = estraiBlocco(b, "sottocategorie");
                if (subContent != null && !subContent.trim().isEmpty()) {
                    List<Categoria> subs = estraiAlberoCategorie(subContent, gestore);
                    for (Categoria sub : subs) {
                        try {
                            c.aggiungiSottocategoria(sub);
                        } catch (Exception e) {
                        }
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

        List<String> blocchi = estraiOggettiTopLevel(arrayContent);
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

    // Versione migliorata di estraiOggetti che ignora gli oggetti annidati
    // (top-level only)
    private static List<String> estraiOggettiTopLevel(String content) {
        List<String> oggetti = new ArrayList<>();
        if (content == null)
            return oggetti;

        int open = 0;
        int start = -1;
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '{') {
                if (open == 0)
                    start = i;
                open++;
            } else if (c == '}') {
                open--;
                if (open == 0 && start != -1) {
                    oggetti.add(content.substring(start + 1, i));
                    start = -1;
                }
            }
        }
        return oggetti;
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
            int endVal = startVal + 1;
            while (endVal < oggetto.length()) {
                endVal = oggetto.indexOf('\"', endVal);
                if (endVal == -1)
                    break;
                // Gestione escape semplice
                if (endVal > 0 && oggetto.charAt(endVal - 1) == '\\') {
                    endVal++; // Prosegue dopo l'escape
                } else {
                    break;
                }
            }
            if (endVal == -1)
                return null;
            return oggetto.substring(startVal + 1, endVal).replace("\\\"", "\"").replace("\\n", "\n").replace("\\\\",
                    "\\");
        } else {
            // Booleano o numero (fino a virgola o fine)
            int endVal = startVal;
            while (endVal < oggetto.length() && oggetto.charAt(endVal) != ',' && oggetto.charAt(endVal) != '}') {
                endVal++;
            }
            return oggetto.substring(startVal, endVal).trim();
        }
    }

    private static List<String> estraiArrayStringhe(String arrayContent) {
        List<String> list = new ArrayList<>();
        if (arrayContent == null || arrayContent.trim().isEmpty())
            return list;

        int pos = 0;
        while (pos < arrayContent.length()) {
            int startStr = arrayContent.indexOf('"', pos);
            if (startStr == -1)
                break;

            int endStr = startStr + 1;
            while (endStr < arrayContent.length()) {
                endStr = arrayContent.indexOf('"', endStr);
                if (endStr == -1)
                    break;
                if (arrayContent.charAt(endStr - 1) == '\\') {
                    endStr++;
                } else {
                    break;
                }
            }
            if (endStr != -1) {
                String str = arrayContent.substring(startStr + 1, endStr).replace("\\\"", "\"").replace("\\n", "\n")
                        .replace("\\\\", "\\");
                list.add(str);
                pos = endStr + 1;
            } else {
                break;
            }
        }
        return list;
    }

    // -----------------------------------------------
    // --- PROPOSTE ---
    // -----------------------------------------------

    /**
     * Serializza la lista di proposte aperte in formato JSON.
     * Ogni proposta tiene il nome della categoria e i valori dei campi.
     *
     * @param proposte Lista di proposte da serializzare.
     * @return Stringa JSON.
     */
    public static String scriviProposte(List<Proposta> proposte) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"proposte\": [\n");
        for (int i = 0; i < proposte.size(); i++) {
            Proposta p = proposte.get(i);
            sb.append("    {\n");
            sb.append("      \"categoria\": \"").append(escape(p.getCategoria().getNome())).append("\",\n");
            sb.append("      \"stato\": \"").append(p.getStato().name()).append("\",\n");
            sb.append("      \"campi\": {\n");
            Map<String, String> valori = p.getValoriCampi();
            List<String> keys = new ArrayList<>(valori.keySet());
            for (int j = 0; j < keys.size(); j++) {
                String k = keys.get(j);
                sb.append("        \"").append(escape(k)).append("\": \"").append(escape(valori.get(k))).append("\"");
                if (j < keys.size() - 1)
                    sb.append(",");
                sb.append("\n");
            }
            sb.append("      },\n");

            // iscritti
            sb.append("      \"iscritti\": [");
            for (int k = 0; k < p.getIscritti().size(); k++) {
                sb.append("\"").append(escape(p.getIscritti().get(k))).append("\"");
                if (k < p.getIscritti().size() - 1)
                    sb.append(", ");
            }
            sb.append("]\n");

            sb.append("    }");
            if (i < proposte.size() - 1)
                sb.append(",");
            sb.append("\n");
        }
        sb.append("  ]\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Deserializza le proposte dal JSON, ricollegando ogni proposta alla sua
     * Categoria tramite il GestoreCategorie.
     *
     * @param json    Contenuto del file proposte.json.
     * @param gestore GestoreCategorie già inizializzato, per risolvere la
     *                categoria.
     * @return Lista di proposte ricostruite (solo quelle con categoria trovata).
     */
    public static List<Proposta> leggiProposte(String json, GestoreCategorie gestore) {
        List<Proposta> risultato = new ArrayList<>();
        String arrayContent = estraiBlocco(json, "proposte");
        if (arrayContent == null)
            return risultato;

        List<String> blocchi = estraiOggettiTopLevel(arrayContent);
        for (String blocco : blocchi) {
            String nomeCategoria = estraiValore(blocco, "categoria");
            String statoStr = estraiValore(blocco, "stato");

            if (nomeCategoria == null || statoStr == null)
                continue;
            Categoria categoria = gestore.getCategoria(nomeCategoria);
            if (categoria == null)
                continue;

            Proposta p = new Proposta(categoria);
            try {
                p.setStato(StatoProposta.valueOf(statoStr));
            } catch (IllegalArgumentException e) {
                continue; // stato sconosciuto, scarto
            }

            // Leggo la mappa campi (oggetto JSON)
            String campiContent = estraiBlocco(blocco, "campi");
            if (campiContent != null) {
                Map<String, String> valori = estraiMappaStringhe(campiContent);
                for (Map.Entry<String, String> entry : valori.entrySet()) {
                    p.impostaValore(entry.getKey(), entry.getValue());
                }
            }

            // Leggo gli iscritti
            String iscrittiContent = estraiBlocco(blocco, "iscritti");
            if (iscrittiContent != null) {
                for (String iscr : estraiArrayStringhe(iscrittiContent)) {
                    p.aggiungiIscritto(iscr);
                }
            }

            risultato.add(p);
        }
        return risultato;
    }

    /**
     * Estrae una mappa chiave-valore da un oggetto JSON semplice (valori solo
     * stringa).
     */
    private static Map<String, String> estraiMappaStringhe(String oggetto) {
        Map<String, String> mappa = new HashMap<>();
        // Iteriamo cercando pattern "chiave": "valore"
        int pos = 0;
        while (pos < oggetto.length()) {
            int startKey = oggetto.indexOf('"', pos);
            if (startKey == -1)
                break;
            int endKey = oggetto.indexOf('"', startKey + 1);
            if (endKey == -1)
                break;
            String chiave = oggetto.substring(startKey + 1, endKey);

            int colon = oggetto.indexOf(':', endKey);
            if (colon == -1)
                break;
            // Cerca il valore stringa
            int startVal = oggetto.indexOf('"', colon);
            if (startVal == -1)
                break;
            int endVal = oggetto.indexOf('"', startVal + 1);
            // gestisci escape
            while (endVal > 0 && oggetto.charAt(endVal - 1) == '\\') {
                endVal = oggetto.indexOf('"', endVal + 1);
            }
            if (endVal == -1)
                break;
            String valore = oggetto.substring(startVal + 1, endVal);
            mappa.put(chiave, valore);
            pos = endVal + 1;
        }
        return mappa;
    }
}
