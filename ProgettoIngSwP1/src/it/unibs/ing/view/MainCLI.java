package it.unibs.ing.view;

import it.unibs.ing.controller.GestoreCategorie;
import it.unibs.ing.controller.GestoreSessione;
import it.unibs.ing.model.Categoria;
import it.unibs.ing.model.Campo;
import it.unibs.ing.model.TipoCampo;
import it.unibs.ing.storage.GestoreFile;

import java.io.IOException;

public class MainCLI {

    private static final String FILE_DATI = "data/categorie.json";
    private static final String FILE_UTENTI = "data/utenti.json";

    private GestoreCategorie gestoreCategorie;
    private GestoreSessione gestoreSessione;
    private InterfacciaConsole vista;

    public MainCLI() {
        this.vista = new InterfacciaConsole();
        caricaDati();
    }

    /**
     * Tenta il caricamento dei dati di sistema (categorie e utenti) da file JSON.
     * Se i file non esistono o sono corrotti, inizializza strutture vuote/default.
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
    }

    /**
     * Avvia il ciclo principale dell'applicazione.
     * Gestisce l'alternanza tra fase di login e menu del configuratore,
     * assicurando il salvataggio dei dati all'uscita.
     */
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

    /**
     * Gestisce il flusso interattivo di login. Se l'accesso ha successo e 
     * il configuratore utilizza la password di default ("admin"), forza 
     * la procedura di cambio password.
     */
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

    /**
     * Stampa e gestisce le scelte del menu principale dedicato al Configuratore.
     * 
     * @return true se l'utente desidera continuare la sessione, false se richiede l'uscita
     */
    private boolean menuConfiguratore() {
        vista.stampaMessaggio("\n--- MENU CONFIGURATORE ---");
        vista.stampaMessaggio("1. Visualizza Categorie");
        vista.stampaMessaggio("2. Crea Nuova Categoria");
        vista.stampaMessaggio("3. Aggiungi Campo Comune");
        vista.stampaMessaggio("4. Rimuovi Categoria");
        vista.stampaMessaggio("5. Modifica Categoria ");
        vista.stampaMessaggio("6. Logout");
        vista.stampaMessaggio("7. Salva & Esci");

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
                gestoreSessione.logout();
                break;
            case 7:
                return false; 
            default:
                vista.stampaMessaggio("Scelta non valida.");
        }
        return true;
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
     * Guida l'utente attraverso la creazione interattiva di una nuova Categoria.
     * Chiede l'eventuale categoria padre e offre l'inserimento ciclico di campi specifici.
     */
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

    /**
     * Intercetta la richiesta di modifica di una categoria esistente tramite CLI.
     * Per adesso supporta la modifica/rimozione dei campi specifici.
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

    private void salvaDati() {
        try {
            GestoreFile.salvaCategorie(gestoreCategorie, FILE_DATI);
            GestoreFile.salvaUtenti(gestoreSessione.getUtenti(), FILE_UTENTI);
            vista.stampaMessaggio("Dati salvati correttamente.");
        } catch (IOException e) {
            vista.stampaMessaggio("Errore salvataggio dati: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new MainCLI().run();
    }
}
