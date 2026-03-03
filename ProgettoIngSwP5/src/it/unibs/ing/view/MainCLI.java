package it.unibs.ing.view;

import it.unibs.ing.controller.GestoreCategorie;
import it.unibs.ing.controller.GestoreProposte;
import it.unibs.ing.controller.GestoreSessione;
import it.unibs.ing.model.Bacheca;
import it.unibs.ing.model.Categoria;
import it.unibs.ing.model.Campo;
import it.unibs.ing.model.Proposta;
import it.unibs.ing.model.TipoCampo;
import it.unibs.ing.model.Fruitore;
import it.unibs.ing.model.Utente;
import it.unibs.ing.storage.GestoreFile;
import it.unibs.ing.storage.ImportatoreBatch;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Classe principale del programma.
 * Gestisce il flusso dell'applicazione e le interazioni principali con l'utente
 * (Menu).
 */
public class MainCLI {

    private static final String FILE_DATI = "data/categorie.json";
    private static final String FILE_UTENTI = "data/utenti.json";
    private static final String FILE_PROPOSTE = "data/proposte.json";

    private GestoreCategorie gestoreCategorie;
    private GestoreSessione gestoreSessione;
    private GestoreProposte gestoreProposte;
    private InterfacciaConsole vista;

    public MainCLI() {
        this.vista = new InterfacciaConsole();
        this.gestoreProposte = new GestoreProposte();
        caricaDati();
    }

    /**
     * Carica i dati salvati all'avvio dell'applicazione.
     * Se non trova dati, inizializza un nuovo sistema vuoto.
     */
    private void caricaDati() {
        try {
            this.gestoreCategorie = GestoreFile.caricaCategorie(FILE_DATI);
            vista.stampaMessaggio("Dati categorie caricati con successo.");
        } catch (Exception e) {
            vista.stampaMessaggio("Nessun dato categorie trovato. Inizio nuova sessione.");
            this.gestoreCategorie = new GestoreCategorie();
        }

        try {
            this.gestoreSessione = new GestoreSessione();
            this.gestoreSessione.setUtenti(GestoreFile.caricaUtenti(FILE_UTENTI));
            vista.stampaMessaggio("Dati utenti caricati con successo.");
        } catch (Exception e) {
            vista.stampaMessaggio("Nessun dato utenti trovato. Inizializzazione utenti default.");
            this.gestoreSessione = new GestoreSessione();
        }

        try {
            List<Proposta> proposteCaricate = GestoreFile.caricaProposte(FILE_PROPOSTE, gestoreCategorie);
            for (Proposta p : proposteCaricate) {
                // Le proposte caricate sono già state scartate se non erano APERTE al
                // salvataggio.
                // Le rimettiamo in bacheca forzatamente.
                gestoreProposte.getBacheca().aggiungiPropostaAperta(p);
            }
            vista.stampaMessaggio("Proposte in bacheca caricate (" + proposteCaricate.size() + ").");
        } catch (Exception e) {
            vista.stampaMessaggio("Nessun dato proposte trovato. Bacheca vuota.");
        }
    }

    // ... (methods run, loopLogin, etc. remain unchanged)

    /**
     * Metodo principale di esecuzione del loop dell'applicazione.
     */
    public void run() {
        boolean inEsecuzione = true;
        while (inEsecuzione) {
            if (gestoreSessione.getUtenteCorrente() == null) {
                loopLogin();
            } else {
                gestoreProposte.controllaScadenze(gestoreSessione);

                if (gestoreSessione.isConfiguratore()) {
                    inEsecuzione = menuConfiguratore();
                } else {
                    inEsecuzione = menuFruitore();
                }
            }
        }
        salvaDati();
    }

    /**
     * Gestisce il processo di autenticazione.
     */
    private void loopLogin() {
        vista.stampaMessaggio("\n--- LOGIN ---");
        String nomeUtente = vista.leggiStringa("Nome Utente");
        String password = vista.leggiStringa("Password");

        if (gestoreSessione.login(nomeUtente, password)) {
            vista.stampaMessaggio("Benvenuto, " + nomeUtente + "!");

            // Controllo Primo Accesso (Password di default "admin")
            if (password.equals("admin") && gestoreSessione.isConfiguratore()) {
                vista.stampaMessaggio("ATTENZIONE: Primo accesso rilevato. È necessario cambiare la password.");
                boolean cambioAvvenuto = false;
                while (!cambioAvvenuto) {
                    String nuovaPass = vista.leggiStringa("Inserisci nuova password");
                    String confermaPass = vista.leggiStringa("Conferma nuova password");

                    if (!nuovaPass.isBlank() && nuovaPass.equals(confermaPass)) {
                        if (nuovaPass.equals("admin")) {
                            vista.stampaMessaggio("La nuova password non può essere 'admin'.");
                        } else {
                            gestoreSessione.getUtenteCorrente().setPassword(nuovaPass);
                            vista.stampaMessaggio("Password aggiornata con successo.");
                            cambioAvvenuto = true;
                        }
                    } else {
                        vista.stampaMessaggio("Le password non coincidono o sono vuote. Riprova.");
                    }
                }
            }
        } else {
            vista.stampaMessaggio("Credenziali non valide o utente inesistente.");
            if (vista.leggiBooleano("Vuoi registrarti come nuovo Fruitore con questo nome utente?")) {
                String nuovaPass = vista.leggiStringa("Inserisci una password per il tuo account");
                Fruitore nuovoFruitore = new Fruitore(nomeUtente, nuovaPass);
                List<Utente> listaUtenti = gestoreSessione.getUtenti();
                listaUtenti.add(nuovoFruitore);
                gestoreSessione.setUtenti(listaUtenti);
                vista.stampaMessaggio("Registrazione completata! Ora puoi effettuare il login.");
            }
        }
    }

    /**
     * Mostra il menu dedicato al Configuratore e gestisce le scelte.
     * 
     * @return true se l'applicazione deve continuare, false se l'utente sceglie di
     *         uscire.
     */
    private boolean menuConfiguratore() {
        vista.stampaMessaggio("\n--- MENU CONFIGURATORE ---");
        vista.stampaMessaggio("1. Visualizza Categorie");
        vista.stampaMessaggio("2. Crea Nuova Categoria");
        vista.stampaMessaggio("3. Aggiungi Campo Comune");
        vista.stampaMessaggio("4. Rimuovi Categoria");
        vista.stampaMessaggio("5. Modifica Categoria");
        vista.stampaMessaggio("6. Gestisci Proposte");
        vista.stampaMessaggio("7. Importa da File Batch");
        vista.stampaMessaggio("8. Logout");
        vista.stampaMessaggio("9. Salva & Esci");

        int scelta = vista.leggiIntero("Seleziona un'opzione");

        switch (scelta) {
            case 1:
                mostraCategorie();
                break;
            case 2:
                creaCategoria();
                break;
            case 3:
                aggiungiCampoComune();
                break;
            case 4:
                rimuoviCategoria();
                break;
            case 5:
                modificaCategoria();
                break;
            case 6:
                menuProposte();
                break;
            case 7:
                importaDaFileBatch();
                break;
            case 8:
                gestoreSessione.logout();
                break;
            case 9:
                return false;
            default:
                vista.stampaMessaggio("Scelta non valida.");
        }
        return true;
    }

    private void importaDaFileBatch() {
        String pathFile = vista.leggiStringa("Percorso del file batch (es. data/batch.txt)");
        new ImportatoreBatch(pathFile, gestoreCategorie, gestoreProposte).esegui();
    }

    // =========================================================
    // --- MENU FRUITORE (V3) ---
    // =========================================================

    private boolean menuFruitore() {
        vista.stampaMessaggio("\n--- MENU FRUITORE ---");
        vista.stampaMessaggio("1. Visualizza Bacheca");
        vista.stampaMessaggio("2. Iscriviti a una Proposta");
        vista.stampaMessaggio("3. Ritira l'iscrizione da una Proposta");
        vista.stampaMessaggio("4. Area Personale (Notifiche)");
        vista.stampaMessaggio("5. Logout");
        vista.stampaMessaggio("6. Salva & Esci");

        int scelta = vista.leggiIntero("Seleziona un'opzione");

        switch (scelta) {
            case 1:
                visualizzaBacheca();
                break;
            case 2:
                iscrivitiProposta();
                break;
            case 3:
                ritiraIscrizione();
                break;
            case 4:
                gestisciNotifiche();
                break;
            case 5:
                gestoreSessione.logout();
                break;
            case 6:
                return false;
            default:
                vista.stampaMessaggio("Scelta non valida.");
        }
        return true;
    }

    private void ritiraIscrizione() {
        Fruitore f = (Fruitore) gestoreSessione.getUtenteCorrente();
        String mioUsername = f.getNomeUtente();

        List<Proposta> mieIscrizioni = new java.util.ArrayList<>();
        for (List<Proposta> lista : gestoreProposte.getBacheca().getTutteLeProposte().values()) {
            for (Proposta p : lista) {
                if (p.getStato() == it.unibs.ing.model.StatoProposta.APERTA && p.getIscritti().contains(mioUsername)) {
                    mieIscrizioni.add(p);
                }
            }
        }

        if (mieIscrizioni.isEmpty()) {
            vista.stampaMessaggio("Non sei iscritto ad alcuna proposta in bacheca.");
            return;
        }

        vista.stampaMessaggio("\nLe tue iscrizioni attuali (Proposte Aperte):");
        for (int i = 0; i < mieIscrizioni.size(); i++) {
            Proposta p = mieIscrizioni.get(i);
            vista.stampaMessaggio((i + 1) + ". " + p.getValore("Titolo") + " [" + p.getCategoria().getNome() + "]");
        }

        int idx = vista.leggiIntero("Seleziona il numero della proposta da cui ritirarti") - 1;
        if (idx >= 0 && idx < mieIscrizioni.size()) {
            Proposta p = mieIscrizioni.get(idx);

            // Check opzionale: se lo stato è APERTA in teoria siamo già entro la data
            // limite,
            // ma verifichiamo la data di "Termine ultimo di iscrizione" per certezza V4
            String scadenzaStr = p.getValore("Termine ultimo di iscrizione");
            if (scadenzaStr != null) {
                try {
                    java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter
                            .ofPattern("dd/MM/yyyy");
                    java.time.LocalDate scadenza = java.time.LocalDate.parse(scadenzaStr, formatter);
                    java.time.LocalDate oggi = java.time.LocalDate.now();
                    if (oggi.isAfter(scadenza)) {
                        vista.stampaMessaggio("Il termine per ritirare l'iscrizione è scaduto.");
                        return;
                    }
                } catch (Exception ignored) {
                }
            }

            if (p.rimuoviIscritto(mioUsername)) {
                p.removeObserver(f); // Togliamo l'observer dal design pattern
                vista.stampaMessaggio("Iscrizione ritirata con successo.");
            } else {
                vista.stampaMessaggio("Errore nel ritiro dell'iscrizione.");
            }
        } else {
            vista.stampaMessaggio("Selezione non valida.");
        }
    }

    private void iscrivitiProposta() {
        String nomeCat = vista.leggiStringa("Inserisci la Categoria dell'evento a cui vuoi iscriverti");
        List<Proposta> aperte = gestoreProposte.getBacheca().getProposteApertePerCategoria(nomeCat);
        if (aperte.isEmpty()) {
            vista.stampaMessaggio("Nessuna proposta aperta per questa categoria.");
            return;
        }

        vista.stampaMessaggio("\nProposte disponibili in " + nomeCat + ":");
        for (int i = 0; i < aperte.size(); i++) {
            Proposta p = aperte.get(i);
            vista.stampaMessaggio((i + 1) + ". " + p.getValore("Titolo") + " (Iscritti: " + p.getIscritti().size() + "/"
                    + p.getValore("Numero di partecipanti") + ")");
        }

        int idx = vista.leggiIntero("Seleziona il numero della proposta") - 1;
        if (idx >= 0 && idx < aperte.size()) {
            Proposta p = aperte.get(idx);
            String mioUsername = gestoreSessione.getUtenteCorrente().getNomeUtente();
            if (p.getIscritti().contains(mioUsername)) {
                vista.stampaMessaggio("Sei già iscritto a questa proposta.");
            } else {
                if (p.aggiungiIscritto(mioUsername)) {
                    p.addObserver((Fruitore) gestoreSessione.getUtenteCorrente());
                    vista.stampaMessaggio("Iscrizione effettuata con successo!");
                } else {
                    vista.stampaMessaggio("Iscrizione fallita. Posti esauriti.");
                }
            }
        } else {
            vista.stampaMessaggio("Selezione non valida.");
        }
    }

    private void gestisciNotifiche() {
        Fruitore f = (Fruitore) gestoreSessione.getUtenteCorrente();
        List<String> notifiche = f.getNotifiche();
        if (notifiche.isEmpty()) {
            vista.stampaMessaggio("Non hai nuove notifiche nel tuo spazio personale.");
            return;
        }

        vista.stampaMessaggio("\n--- LE TUE NOTIFICHE ---");
        for (int i = 0; i < notifiche.size(); i++) {
            vista.stampaMessaggio((i + 1) + ". " + notifiche.get(i));
        }

        if (vista.leggiBooleano("Vuoi cancellare una notifica?")) {
            int idx = vista.leggiIntero("Numero notifica da cancellare") - 1;
            if (f.rimuoviNotifica(idx)) {
                vista.stampaMessaggio("Notifica eliminata dal tuo spazio personale.");
            } else {
                vista.stampaMessaggio("Numero non valido.");
            }
        }
    }

    // =========================================================
    // --- GESTIONE PROPOSTE (V2) ---
    // =========================================================

    /**
     * Sottomenu per la gestione delle proposte di iniziativa.
     */
    private void menuProposte() {
        boolean continua = true;
        while (continua) {
            vista.stampaMessaggio("\n--- MENU PROPOSTE ---");
            vista.stampaMessaggio("1. Crea Nuova Proposta");
            vista.stampaMessaggio("2. Pubblica una Proposta");
            vista.stampaMessaggio("3. Visualizza Bacheca");
            vista.stampaMessaggio("4. Ritira Proposta Esistente");
            vista.stampaMessaggio("5. Torna al menu principale");

            int scelta = vista.leggiIntero("Seleziona un'opzione");
            switch (scelta) {
                case 1:
                    creaProposta();
                    break;
                case 2:
                    pubblicaProposta();
                    break;
                case 3:
                    visualizzaBacheca();
                    break;
                case 4:
                    ritiraProposta();
                    break;
                case 5:
                    continua = false;
                    break;
                default:
                    vista.stampaMessaggio("Scelta non valida.");
            }
        }
    }

    /**
     * Flusso di creazione di una nuova proposta:
     * seleziona categoria -> compila campi -> valida.
     */
    private void creaProposta() {
        if (gestoreCategorie.getCategorie().isEmpty()) {
            vista.stampaMessaggio("Nessuna categoria disponibile. Crea prima una categoria.");
            return;
        }

        // 1. Selezione categoria
        vista.stampaMessaggio("\n--- CATEGORIE DISPONIBILI ---");
        for (Categoria c : gestoreCategorie.getCategorieRadice()) {
            stampaCategoriaRicorsiva(c, "");
        }
        String nomeCategoria = vista.leggiStringa("Nome della categoria per la proposta");
        Categoria categoria = gestoreCategorie.getCategoria(nomeCategoria);
        if (categoria == null) {
            vista.stampaMessaggio("Categoria non trovata.");
            return;
        }

        // 2. Compilazione campi
        Proposta proposta = new Proposta(categoria);
        vista.stampaMessaggio("\nCompila i campi per la categoria '" + categoria.getNome() + "':");
        vista.stampaMessaggio("(Per i campi di tipo DATA usa il formato dd/MM/yyyy, per ORA usa HH:mm)");

        for (Campo campo : categoria.getCampi().values()) {
            String etichetta = campo.getNome()
                    + " [" + campo.getTipo().name() + "]"
                    + (campo.isObbligatorio() ? " *" : " (opzionale)");
            String valore = vista.leggiStringa(etichetta);
            if (!valore.isBlank()) {
                proposta.impostaValore(campo.getNome(), valore);
            }
        }

        // 3. Validazione
        if (gestoreProposte.validaProposta(proposta)) {
            gestoreProposte.getBacheca(); // assicura bacheca inizializzata
            // Teniamo la proposta in una lista locale per poterla poi pubblicare
            // La aggiungiamo alla bacheca interna del gestore come "in attesa"
            vista.stampaMessaggio("\nProposta VALIDA! Puoi pubblicarla con l'opzione 'Pubblica una Proposta'.");
            if (vista.leggiBooleano("Vuoi pubblicarla subito in bacheca?")) {
                try {
                    gestoreProposte.pubblicaProposta(proposta);
                    vista.stampaMessaggio("Proposta pubblicata in bacheca con successo.");
                } catch (IllegalArgumentException e) {
                    vista.stampaMessaggio("Errore: " + e.getMessage());
                }
            } else {
                vista.stampaMessaggio("Proposta salvata come VALIDA. Sarà scartata se non pubblicata prima di uscire.");
            }
        } else {
            vista.stampaMessaggio("\nProposta NON valida. Controlla:");
            vista.stampaMessaggio("  - Tutti i campi obbligatori (*) devono essere compilati.");
            vista.stampaMessaggio("  - 'Termine ultimo di iscrizione' deve essere una data futura.");
            vista.stampaMessaggio("  - 'Data' dell'evento deve essere almeno 2 giorni dopo il termine di iscrizione.");
        }
    }

    /**
     * Permette di pubblicare in bacheca una proposta già valida.
     * In questa versione semplificata, chiede di ricreare la proposta
     * (le proposte non ancora pubblicate non sopravvivono alla chiusura).
     */
    private void pubblicaProposta() {
        Bacheca bacheca = gestoreProposte.getBacheca();
        Map<String, List<Proposta>> tutte = bacheca.getTutteLeProposte();

        // Mostra le proposte già in bacheca
        if (tutte.isEmpty()) {
            vista.stampaMessaggio(
                    "Nessuna proposta in bacheca. Crea e pubblica una proposta dalla voce 'Crea Nuova Proposta'.");
            return;
        }

        // Se ci sono già proposte aperte le mostra
        vista.stampaMessaggio("\n--- PROPOSTE IN BACHECA ---");
        visualizzaBacheca();
    }

    /**
     * Mostra tutte le proposte attualmente pubblicate in bacheca.
     */
    private void visualizzaBacheca() {
        Bacheca bacheca = gestoreProposte.getBacheca();
        Map<String, List<Proposta>> tutte = bacheca.getTutteLeProposte();

        if (tutte.isEmpty()) {
            vista.stampaMessaggio("La bacheca è vuota.");
            return;
        }

        vista.stampaMessaggio("\n========== BACHECA ==========");
        for (Map.Entry<String, List<Proposta>> entry : tutte.entrySet()) {
            vista.stampaMessaggio("\n[Categoria: " + entry.getKey() + "]");
            List<Proposta> proposte = entry.getValue();
            for (int i = 0; i < proposte.size(); i++) {
                Proposta p = proposte.get(i);
                vista.stampaMessaggio("  Proposta #" + (i + 1) + " - Stato: " + p.getStato());
                for (Map.Entry<String, String> campo : p.getValoriCampi().entrySet()) {
                    vista.stampaMessaggio("    " + campo.getKey() + ": " + campo.getValue());
                }
            }
        }
        vista.stampaMessaggio("=============================");
    }

    private void ritiraProposta() {
        boolean trovate = false;
        List<Proposta> proposteDaRitirare = new java.util.ArrayList<>();

        vista.stampaMessaggio("\n--- PROPOSTE RITIRABILI (Aperte o Confermate) ---");
        for (List<Proposta> lista : gestoreProposte.getBacheca().getTutteLeProposte().values()) {
            for (Proposta p : lista) {
                if (p.getStato() == it.unibs.ing.model.StatoProposta.APERTA
                        || p.getStato() == it.unibs.ing.model.StatoProposta.CONFERMATA) {
                    proposteDaRitirare.add(p);
                    vista.stampaMessaggio(proposteDaRitirare.size() + ". " + p.getValore("Titolo") + " ["
                            + p.getCategoria().getNome() + "] (Stato: " + p.getStato() + ")");
                    trovate = true;
                }
            }
        }

        if (!trovate) {
            vista.stampaMessaggio("Nessuna proposta può essere ritirata in questo momento.");
            return;
        }

        int idx = vista.leggiIntero("Seleziona il numero della proposta da ritirare per forza maggiore") - 1;
        if (idx >= 0 && idx < proposteDaRitirare.size()) {
            Proposta p = proposteDaRitirare.get(idx);
            if (vista.leggiBooleano(
                    "Sei sicuro di volerla ritirare? Questo avviserà " + p.getIscritti().size() + " iscritti.")) {
                if (gestoreProposte.ritiraProposta(p, gestoreSessione)) {
                    vista.stampaMessaggio("Proposta ritirata con successo.");
                } else {
                    vista.stampaMessaggio("Errore: Ritiro fallito. Potrebbe essere fuori tempo massimo.");
                }
            }
        } else {
            vista.stampaMessaggio("Selezione non valida.");
        }
    }

    private void mostraCategorie() {
        if (gestoreCategorie.getCategorie().isEmpty()) {
            vista.stampaMessaggio("Nessuna categoria definita.");
        } else {
            vista.stampaMessaggio("\n--- ELENCO CATEGORIE ---");
            for (Categoria c : gestoreCategorie.getCategorieRadice()) {
                stampaCategoriaRicorsiva(c, "");
            }
            vista.stampaMessaggio("------------------------\n");
        }
    }

    private void stampaCategoriaRicorsiva(Categoria c, String indent) {
        vista.stampaMessaggio(indent + "- " + c.getNome() + " (" + c.getCampi().size() + " campi specifici)");
        if (!c.getSottocategorie().isEmpty()) {
            for (Categoria sub : c.getSottocategorie()) {
                stampaCategoriaRicorsiva(sub, indent + "  ");
            }
        }
    }

    /**
     * Gestisce il flusso di creazione di una nuova categoria.
     */
    private void creaCategoria() {
        String nome = vista.leggiStringa("Nome Categoria");
        String descrizione = vista.leggiStringa("Descrizione");

        Categoria nuovaCategoria = new Categoria(nome, descrizione);

        // Scelta padre
        String nomePadre = null;
        if (!gestoreCategorie.getCategorie().isEmpty()
                && vista.leggiBooleano("Questa categoria è una Sottocategoria di un'altra esistente?")) {
            nomePadre = vista.leggiStringa("Inserisci il nome esatto della Categoria Padre");
            if (gestoreCategorie.getCategoria(nomePadre) == null) {
                vista.stampaMessaggio("Errore: Categoria padre non trovata. Creazione annullata.");
                return;
            }
        }

        vista.stampaMessaggio("I Campi Base e Comuni verranno aggiunti/ereditati automaticamente.");

        boolean aggiuntaCampi = true;
        while (aggiuntaCampi) {
            if (vista.leggiBooleano("Vuoi aggiungere un campo specifico (per questa categoria)?")) {
                String nomeCampo = vista.leggiStringa("Nome Campo");
                String descCampo = vista.leggiStringa("Descrizione Campo");
                boolean obbligatorio = vista.leggiBooleano("È obbligatorio?");

                vista.stampaMessaggio("Tipi: 0=STRINGA, 1=INTERO, 2=BOOLEANO, 3=DATA, 4=ORA");
                int idxTipo = vista.leggiIntero("Tipo Campo");
                try {
                    TipoCampo tipo = TipoCampo.values()[Math.min(Math.max(0, idxTipo), TipoCampo.values().length - 1)];
                    nuovaCategoria.aggiungiCampo(new Campo(nomeCampo, descCampo, obbligatorio, tipo));
                } catch (Exception e) {
                    vista.stampaMessaggio("Errore aggiunta campo: " + e.getMessage());
                }
            } else {
                aggiuntaCampi = false;
            }
        }

        try {
            gestoreCategorie.aggiungiCategoria(nuovaCategoria, nomePadre);
            vista.stampaMessaggio("Categoria creata con successo.");
        } catch (IllegalArgumentException e) {
            vista.stampaMessaggio("Errore: " + e.getMessage());
        }
    }

    /**
     * Permette di aggiungere un nuovo campo comune a tutte le categorie.
     */
    private void aggiungiCampoComune() {
        vista.stampaMessaggio("\n--- NUOVO CAMPO COMUNE ---");
        String nomeCampo = vista.leggiStringa("Nome Campo");
        String descCampo = vista.leggiStringa("Descrizione Campo");
        boolean obbligatorio = vista.leggiBooleano("È obbligatorio?");

        vista.stampaMessaggio("Tipi: 0=STRINGA, 1=INTERO, 2=BOOLEANO, 3=DATA, 4=ORA");
        int idxTipo = vista.leggiIntero("Tipo Campo");
        TipoCampo tipo = TipoCampo.values()[Math.min(Math.max(0, idxTipo), TipoCampo.values().length - 1)];

        try {
            Campo nuovoCampo = new Campo(nomeCampo, descCampo, obbligatorio, tipo);
            gestoreCategorie.aggiungiCampoComune(nuovoCampo);
            vista.stampaMessaggio("Campo comune aggiunto con successo a tutte le categorie.");
        } catch (Exception e) {
            vista.stampaMessaggio("Errore aggiunta campo comune: " + e.getMessage());
        }
    }

    /**
     * Rimuove una categoria esistente.
     */
    private void rimuoviCategoria() {
        String nome = vista.leggiStringa("Nome della categoria da rimuovere");
        if (gestoreCategorie.getCategoria(nome) != null) {
            if (vista.leggiBooleano("Sei sicuro di voler eliminare la categoria '" + nome + "'?")) {
                gestoreCategorie.rimuoviCategoria(nome);
                vista.stampaMessaggio("Categoria rimossa.");
            }
        } else {
            vista.stampaMessaggio("Categoria non trovata.");
        }
    }

    /**
     * Gestisce la modifica di una categoria esistente.
     */
    private void modificaCategoria() {
        String nomeCategoria = vista.leggiStringa("Inserisci il nome della categoria da modificare");
        Categoria categoria = gestoreCategorie.getCategoria(nomeCategoria);

        if (categoria == null) {
            vista.stampaMessaggio("Categoria non trovata.");
            return;
        }

        vista.stampaMessaggio("Modifica Categoria: " + categoria.getNome());
        vista.stampaMessaggio("1. Modifica Descrizione Campo");
        vista.stampaMessaggio("2. Modifica Obbligatorietà Campo");
        vista.stampaMessaggio("3. Rimuovi Campo Specifico");
        vista.stampaMessaggio("4. Torni al menu principale");

        int scelta = vista.leggiIntero("Scelta");

        switch (scelta) {
            case 1:
                String nomeCampoDesc = vista.leggiStringa("Nome del campo da modificare");
                Campo campoDesc = categoria.getCampo(nomeCampoDesc);
                if (campoDesc != null) {
                    String nuovaDesc = vista.leggiStringa("Nuova descrizione");
                    campoDesc.setDescrizione(nuovaDesc);
                    vista.stampaMessaggio("Descrizione aggiornata.");
                } else {
                    vista.stampaMessaggio("Campo non trovato.");
                }
                break;
            case 2:
                String nomeCampoObbl = vista.leggiStringa("Nome del campo da modificare");
                Campo campoObbl = categoria.getCampo(nomeCampoObbl);
                if (campoObbl != null) {
                    boolean obbligatorio = vista.leggiBooleano("È obbligatorio?");
                    campoObbl.setObbligatorio(obbligatorio);
                    vista.stampaMessaggio("Obbligatorietà aggiornata.");
                } else {
                    vista.stampaMessaggio("Campo non trovato.");
                }
                break;
            case 3:
                String nomeCampoRimuovi = vista.leggiStringa("Nome del campo da rimuovere");
                if (categoria.getCampo(nomeCampoRimuovi) != null) {
                    categoria.rimuoviCampo(nomeCampoRimuovi);
                    vista.stampaMessaggio("Campo rimosso.");
                } else {
                    vista.stampaMessaggio("Campo non trovato.");
                }
                break;
            case 4:
                return;
            default:
                vista.stampaMessaggio("Scelta non valida.");
        }
    }

    /**
     * Salva lo stato corrente del sistema su file.
     */
    private void salvaDati() {
        try {
            GestoreFile.salvaCategorie(gestoreCategorie, FILE_DATI);
            GestoreFile.salvaUtenti(gestoreSessione.getUtenti(), FILE_UTENTI);

            // Per le proposte, estraiamo solo quelle attualmente in Bacheca (APERTE)
            List<Proposta> bacheca = new java.util.ArrayList<>();
            for (List<Proposta> lista : gestoreProposte.getBacheca().getTutteLeProposte().values()) {
                bacheca.addAll(lista);
            }
            GestoreFile.salvaProposte(bacheca, FILE_PROPOSTE);

            vista.stampaMessaggio("Dati salvati correttamente.");
        } catch (IOException e) {
            vista.stampaMessaggio("Errore salvataggio dati: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        MainCLI app = new MainCLI();
        // V5: se viene passato un file batch come argomento, lo elabora prima del loop
        if (args.length > 0) {
            new ImportatoreBatch(args[0], app.gestoreCategorie, app.gestoreProposte).esegui();
        }
        app.run();
    }
}
