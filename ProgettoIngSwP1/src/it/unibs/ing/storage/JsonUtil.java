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

public class JsonUtil {

    /**
     * Serializza lo stato di GestoreCategorie in formato JSON.
     * Include le categorie radice (con relative sottocategorie), i campi base e i campi comuni.
     * 
     * @param gestore l'istanza di GestoreCategorie da serializzare
     * @return una stringa contenente la rappresentazione JSON del gestore
     */
    public static String scriviCategorie(GestoreCategorie gestore) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");

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

        sb.append("  \"campiBase\": ");
        sb.append(scriviListaCampi(gestore.getCampiBase(), "  "));
        sb.append(",\n");

        sb.append("  \"campiComuni\": ");
        sb.append(scriviListaCampi(gestore.getCampiComuni(), "  "));

        sb.append("\n}");
        return sb.toString();
    }

    /**
     * Metodo di appoggio per serializzare ricorsivamente una singola singola Categoria 
     * e le sue sottocategorie, applicando l'indentazione corretta.
     * 
     * @param c la Categoria da serializzare
     * @param indent la stringa di indentazione corrente (es. spazi)
     * @return la stringa JSON corrispondente alla categoria
     */
    private static String scriviCategoria(Categoria c, String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("{\n");
        sb.append(indent).append("  \"nome\": \"").append(escape(c.getNome())).append("\",\n");
        sb.append(indent).append("  \"descrizione\": \"").append(escape(c.getDescrizione())).append("\",\n");

        sb.append(indent).append("  \"campi\": ")
                .append(scriviListaCampi(new ArrayList<>(c.getCampi().values()), indent + "  "));

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
            sb.append("\n");
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

    /**
     * Deserializza una stringa JSON trasformandola in un'istanza di GestoreCategorie.
     * Ricostruisce l'albero delle categorie e i campi comuni.
     * 
     * @param json la stringa JSON con i dati salvati
     * @return la nuova istanza di GestoreCategorie popolata con i dati letti
     */
    public static GestoreCategorie leggiCategorie(String json) {
        GestoreCategorie gestore = new GestoreCategorie();

        List<Categoria> categorieRadice = estraiAlberoCategorie(estraiBlocco(json, "categorie"));
        for (Categoria c : categorieRadice) {
            try {
                gestore.aggiungiCategoria(c);
                aggiungiSottocategorieRicorsive(gestore, c);
            } catch (Exception e) {
            }
        }

        List<Campo> commons = estraiListaCampi(estraiBlocco(json, "campiComuni"));
        for (Campo c : commons) {
            gestore.aggiungiCampoComune(c);
        }

        return gestore;
    }

    /**
     * Deserializza l'elenco degli utenti dal blocco JSON corrispondente.
     * Distingue tra CONFIGURATORE e FRUITORE in base al campo "ruolo".
     * 
     * @param json la stringa JSON contenente la lista degli utenti
     * @return la lista di oggetti Utente de-serializzati
     */
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
                    utenti.add(new Fruitore(nome, pass));
                }
            }
        }
        return utenti;
    }

    private static void aggiungiSottocategorieRicorsive(GestoreCategorie gestore, Categoria padre) {
        for (Categoria sub : padre.getSottocategorie()) {
            try {
                gestore.aggiungiCategoria(sub, padre.getNome());
                aggiungiSottocategorieRicorsive(gestore, sub);
            } catch (Exception e) {
            }
        }
    }

    /**
     * Visita ed estrae ricorsivamente un albero di Categorie da un frammento JSON ('['...']').
     * Supporta la lettura dei campi e delle sottocategorie annidate.
     * 
     * @param arrayContent il contenuto JSON puro della lista di categorie
     * @return la lista di oggetti Categoria parsati
     */
    private static List<Categoria> estraiAlberoCategorie(String arrayContent) {
        List<Categoria> list = new ArrayList<>();
        if (arrayContent == null || arrayContent.trim().isEmpty())
            return list;

        List<String> blocchiCat = estraiOggettiTopLevel(arrayContent);
        for (String b : blocchiCat) {
            String nome = estraiValore(b, "nome");
            String desc = estraiValore(b, "descrizione");
            if (nome != null) {
                Categoria c = new Categoria(nome, desc != null ? desc : "");

                String campiContent = estraiBlocco(b, "campi");
                List<Campo> campi = estraiListaCampi(campiContent);
                for (Campo cmp : campi) {
                    try {
                        c.aggiungiCampo(cmp);
                    } catch (Exception e) {
                    }
                }

                String subContent = estraiBlocco(b, "sottocategorie");
                if (subContent != null && !subContent.trim().isEmpty()) {
                    List<Categoria> subs = estraiAlberoCategorie(subContent);
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

    /**
     * Estrae un intero blocco JSON (oggetto o array, tra parentesi {} o []) 
     * corrispondente ad una determinata chiave (key).
     * Risolve il bilanciamento delle parentesi per un matching corretto.
     * 
     * @param json la stringa JSON completa
     * @param key il nome della chiave da cercare
     * @return il testo contenuto nel blocco (parentesi incluse), o null se non trovato
     */
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

    /**
     * Estrae il valore scalare puro associato a una singola chiave in un JSON frammentato,
     * supportando stringhe tra virgolette o valori grezzi/booleani interrotti dalla virgola.
     * 
     * @param oggetto il contenuto dell'oggetto corrente
     * @param key la chiave di cui trovare il valore
     * @return la stringa che rappresenta il valore (senza virgolette), o null se assente
     */
    private static String estraiValore(String oggetto, String key) {
        int startKey = oggetto.indexOf("\"" + key + "\"");
        if (startKey == -1)
            return null;

        int startSep = oggetto.indexOf(":", startKey);
        int startVal = startSep + 1;

        while (startVal < oggetto.length() && Character.isWhitespace(oggetto.charAt(startVal))) {
            startVal++;
        }

        if (startVal >= oggetto.length())
            return null;

        char firstChar = oggetto.charAt(startVal);
        if (firstChar == '\"') {
            int endVal = oggetto.indexOf("\"", startVal + 1);
            while (endVal > 0 && oggetto.charAt(endVal - 1) == '\\') {
                endVal = oggetto.indexOf("\"", endVal + 1);
            }
            return oggetto.substring(startVal + 1, endVal);
        } else {
            int endVal = startVal;
            while (endVal < oggetto.length() && oggetto.charAt(endVal) != ',' && oggetto.charAt(endVal) != '}') {
                endVal++;
            }
            return oggetto.substring(startVal, endVal).trim();
        }
    }
}
