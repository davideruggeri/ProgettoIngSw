package it.unibs.ing.view;

import it.unibs.ing.controller.GestoreCategorie;
import it.unibs.ing.controller.GestoreProposte;
import it.unibs.ing.controller.GestoreSessione;
import it.unibs.ing.model.Bacheca;
import it.unibs.ing.model.Categoria;
import it.unibs.ing.model.Campo;
import it.unibs.ing.model.Proposta;
import it.unibs.ing.model.TipoCampo;
import it.unibs.ing.storage.GestoreFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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

                gestoreProposte.getBacheca().aggiungiPropostaAperta(p);
            }
            vista.stampaMessaggio("Proposte in bacheca caricate (" + proposteCaricate.size() + ").");
        } catch (Exception e) {
            vista.stampaMessaggio("Nessun dato proposte trovato. Bacheca vuota.");
        }
    }

    public void run() {
        boolean inEsecuzione = true;
        while (inEsecuzione) {
            if (gestoreSessione.getUtenteCorrente() == null) {
                loopLogin();
            } else {
                if (gestoreSessione.isConfiguratore()) {
                    inEsecuzione = menuConfiguratore();
                } else {

                    gestoreSessione.logout();
                }
            }
        }
        salvaDati();
    }

    private void loopLogin() {
        vista.stampaMessaggio("\n--- LOGIN ---");
        String nomeUtente = vista.leggiStringa("Nome Utente");
        String password = vista.leggiStringa("Password");

        if (gestoreSessione.login(nomeUtente, password)) {
            vista.stampaMessaggio("Benvenuto, " + nomeUtente + "!");

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
            vista.stampaMessaggio("Credenziali non valide.");
        }
    }

    private boolean menuConfiguratore() {
        vista.stampaMessaggio("\n--- MENU CONFIGURATORE ---");
        vista.stampaMessaggio("1. Visualizza Categorie");
        vista.stampaMessaggio("2. Crea Nuova Categoria");
        vista.stampaMessaggio("3. Aggiungi Campo Comune");
        vista.stampaMessaggio("4. Rimuovi Categoria");
        vista.stampaMessaggio("5. Modifica Categoria");
        vista.stampaMessaggio("6. Gestisci Proposte");
        vista.stampaMessaggio("7. Logout");
        vista.stampaMessaggio("8. Salva & Esci");

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
                gestoreSessione.logout();
                break;
            case 8:
                return false;
            default:
                vista.stampaMessaggio("Scelta non valida.");
        }
        return true;
    }

    private void menuProposte() {
        boolean continua = true;
        while (continua) {
            vista.stampaMessaggio("\n--- MENU PROPOSTE ---");
            vista.stampaMessaggio("1. Crea Nuova Proposta");
            vista.stampaMessaggio("2. Pubblica una Proposta");
            vista.stampaMessaggio("3. Visualizza Bacheca");
            vista.stampaMessaggio("4. Torna al menu principale");

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
                    continua = false;
                    break;
                default:
                    vista.stampaMessaggio("Scelta non valida.");
            }
        }
    }

    private void creaProposta() {
        if (gestoreCategorie.getCategorie().isEmpty()) {
            vista.stampaMessaggio("Nessuna categoria disponibile. Crea prima una categoria.");
            return;
        }

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

        if (gestoreProposte.validaProposta(proposta)) {
            gestoreProposte.getBacheca();

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

    private void pubblicaProposta() {
        Bacheca bacheca = gestoreProposte.getBacheca();
        Map<String, List<Proposta>> tutte = bacheca.getTutteLeProposte();

        if (tutte.isEmpty()) {
            vista.stampaMessaggio(
                    "Nessuna proposta in bacheca. Crea e pubblica una proposta dalla voce 'Crea Nuova Proposta'.");
            return;
        }

        vista.stampaMessaggio("\n--- PROPOSTE IN BACHECA ---");
        visualizzaBacheca();
    }

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

    private void creaCategoria() {
        String nome = vista.leggiStringa("Nome Categoria");
        String descrizione = vista.leggiStringa("Descrizione");

        Categoria nuovaCategoria = new Categoria(nome, descrizione);

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

    private void salvaDati() {
        try {
            GestoreFile.salvaCategorie(gestoreCategorie, FILE_DATI);
            GestoreFile.salvaUtenti(gestoreSessione.getUtenti(), FILE_UTENTI);

            List<Proposta> proposteDaSalvare = new java.util.ArrayList<>();
            for (List<Proposta> lista : gestoreProposte.getBacheca().getTutteLeProposte().values()) {
                proposteDaSalvare.addAll(lista);
            }
            GestoreFile.salvaProposte(proposteDaSalvare, FILE_PROPOSTE);

            vista.stampaMessaggio("Dati salvati correttamente.");
        } catch (IOException e) {
            vista.stampaMessaggio("Errore salvataggio dati: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new MainCLI().run();
    }
}
