package it.unibs.ing.storage;

import it.unibs.ing.controller.GestoreCategorie;
import it.unibs.ing.controller.GestoreProposte;
import it.unibs.ing.model.Campo;
import it.unibs.ing.model.Categoria;
import it.unibs.ing.model.Proposta;
import it.unibs.ing.model.TipoCampo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ImportatoreBatch {

    private final String filePath;
    private final GestoreCategorie gestoreCategorie;
    private final GestoreProposte gestoreProposte;
    private int righeOk = 0;
    private int righeErrore = 0;

    public ImportatoreBatch(String filePath, GestoreCategorie gestoreCategorie, GestoreProposte gestoreProposte) {
        this.filePath = filePath;
        this.gestoreCategorie = gestoreCategorie;
        this.gestoreProposte = gestoreProposte;
    }

    /**
     * Innesca la lettura sequenziale del file batch fornito nel costruttore.
     * Ignora le righe vuote e i commenti scritti col prefisso '#'.
     */
    public void esegui() {
        System.out.println("\n=== AVVIO IMPORTAZIONE BATCH (" + filePath + ") ===");

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String riga;
            int numeroRiga = 0;
            while ((riga = reader.readLine()) != null) {
                numeroRiga++;
                riga = riga.trim();
                if (riga.isEmpty() || riga.startsWith("#")) {
                    continue;
                }
                System.out.print("Riga " + numeroRiga + ": ");
                processaRiga(riga);
            }
        } catch (IOException e) {
            System.err.println("ERRORE: Impossibile aprire il file batch '" + filePath + "': " + e.getMessage());
            return;
        }

        System.out.println("=== IMPORTAZIONE COMPLETATA: " + righeOk + " OK, " + righeErrore + " ERRORI ===\n");
    }

    /**
     * Analizza una specifica riga di comando separandola al carattere ';'
     * ed esegue lo switch sul comando corrispondente.
     * 
     * @param riga il comando testuale in lettura passiva
     */
    private void processaRiga(String riga) {
        String[] parti = riga.split(";", -1);
        if (parti.length < 1) {
            errore("Riga vuota o malformata.");
            return;
        }

        String comando = parti[0].trim().toUpperCase();

        switch (comando) {
            case "CREA_CATEGORIA":
                creaCategoriaCmd(parti);
                break;
            case "AGGIUNGI_CAMPO":
                aggiungiCampoCmd(parti);
                break;
            case "CREA_PROPOSTA":
                creaPropostaCmd(parti);
                break;
            case "PUBBLICA_PROPOSTA":
                pubblicaPropostaCmd(parti);
                break;
            default:
                errore("Comando sconosciuto: '" + comando + "'");
        }
    }

    /**
     * Esegue il comando CREA_CATEGORIA delegando al Controller le instanziazioni.
     * 
     * @param parti l'array generato dallo split contenente tokenizzati i parametri del comando
     */
    private void creaCategoriaCmd(String[] parti) {
        if (parti.length < 3) {
            errore("CREA_CATEGORIA richiede almeno: Nome;Descrizione");
            return;
        }
        String nome = parti[1].trim();
        String descr = parti[2].trim();
        String padre = parti.length >= 4 && !parti[3].isBlank() ? parti[3].trim() : null;

        if (gestoreCategorie.getCategoria(nome) != null) {
            errore("Categoria '" + nome + "' già esistente.");
            return;
        }
        try {
            gestoreCategorie.aggiungiCategoria(new Categoria(nome, descr), padre);
            ok("Categoria '" + nome + "' creata" + (padre != null ? " come figlia di '" + padre + "'" : "") + ".");
        } catch (IllegalArgumentException e) {
            errore(e.getMessage());
        }
    }

    /**
     * Esegue il comando AGGIUNGI_CAMPO su una categoria precedentemente caricata.
     * 
     * @param parti array delimitato ';' del comando riga
     */
    private void aggiungiCampoCmd(String[] parti) {
        if (parti.length < 6) {
            errore("AGGIUNGI_CAMPO richiede: NomeCat;NomeCampo;Descr;OBBLIGATORIO|OPZIONALE;TIPO");
            return;
        }
        String nomeCat = parti[1].trim();
        String nomeCampo = parti[2].trim();
        String descr = parti[3].trim();
        boolean obbligatorio = parti[4].trim().equalsIgnoreCase("OBBLIGATORIO");
        String tipoStr = parti[5].trim().toUpperCase();

        Categoria cat = gestoreCategorie.getCategoria(nomeCat);
        if (cat == null) {
            errore("Categoria '" + nomeCat + "' non trovata.");
            return;
        }
        TipoCampo tipo;
        try {
            tipo = TipoCampo.valueOf(tipoStr);
        } catch (IllegalArgumentException e) {
            errore("Tipo campo sconosciuto: '" + tipoStr
                    + "'. Valori validi: STRINGA, INTERO, BOOLEANO, DATA, ORA, DOUBLE");
            return;
        }
        try {
            cat.aggiungiCampo(new Campo(nomeCampo, descr, obbligatorio, tipo));
            ok("Campo '" + nomeCampo + "' aggiunto a '" + nomeCat + "'.");
        } catch (IllegalArgumentException e) {
            errore("Campo '" + nomeCampo + "' in '" + nomeCat + "': " + e.getMessage());
        }
    }

    /**
     * Forgia una nuova Proposta compilando i campi principali in automatico,
     * effettuando anche una successiva valutazione di Verifica Validità per memorizzarla in pool.
     * 
     * @param parti argomenti scaturiti dallo splitting del batch
     */
    private void creaPropostaCmd(String[] parti) {
        if (parti.length < 9) {
            errore("CREA_PROPOSTA richiede: Cat;Titolo;DataConclusiva;TermineIscrizione;Ora;Luogo;NumPart;Quota");
            return;
        }
        String nomeCat = parti[1].trim();
        String titolo = parti[2].trim();
        String dataConcl = parti[3].trim();
        String termineIscr = parti[4].trim();
        String ora = parti[5].trim();
        String luogo = parti[6].trim();
        String numPart = parti[7].trim();
        String quota = parti[8].trim();

        Categoria cat = gestoreCategorie.getCategoria(nomeCat);
        if (cat == null) {
            errore("Categoria '" + nomeCat + "' non trovata.");
            return;
        }

        Proposta p = new Proposta(cat);
        p.impostaValore("Titolo", titolo);
        p.impostaValore("Data conclusiva", dataConcl);
        p.impostaValore("Termine ultimo di iscrizione", termineIscr);
        p.impostaValore("Ora", ora);
        p.impostaValore("Luogo", luogo);
        p.impostaValore("Numero di partecipanti", numPart);
        p.impostaValore("Quota individuale", quota);

        p.impostaValore("Data", dataConcl);

        if (gestoreProposte.validaProposta(p)) {

            gestoreProposte.getBacheca();

            gestoreProposte.getProposteValide().put(titolo, p);
            ok("Proposta '" + titolo + "' creata e VALIDA (usa PUBBLICA_PROPOSTA per pubblicarla).");
        } else {
            errore("Proposta '" + titolo + "' NON valida. Controlla date e campi obbligatori.");
        }
    }

    /**
     * Richiama la pubblicazione in bacheca di una proposta battezzata come VALIDA.
     * 
     * @param parti i parametri estratti, di cui interessa essenzialmente la PK Titolo
     */
    private void pubblicaPropostaCmd(String[] parti) {
        if (parti.length < 2) {
            errore("PUBBLICA_PROPOSTA richiede: Titolo");
            return;
        }
        String titolo = parti[1].trim();
        Proposta p = gestoreProposte.getProposteValide().get(titolo);
        if (p == null) {
            errore("Nessuna proposta valida con titolo '" + titolo + "' trovata. Usa prima CREA_PROPOSTA.");
            return;
        }
        try {
            gestoreProposte.pubblicaProposta(p);
            gestoreProposte.getProposteValide().remove(titolo);
            ok("Proposta '" + titolo + "' pubblicata in bacheca.");
        } catch (IllegalArgumentException e) {
            errore(e.getMessage());
        }
    }

    private void ok(String msg) {
        System.out.println("[OK] " + msg);
        righeOk++;
    }

    private void errore(String msg) {
        System.out.println("[ERRORE] " + msg);
        righeErrore++;
    }

    public int getRigheOk() {
        return righeOk;
    }

    public int getRigheErrore() {
        return righeErrore;
    }
}
