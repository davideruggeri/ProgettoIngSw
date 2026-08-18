package it.unibs.ing.controller;

import it.unibs.ing.model.Bacheca;
import it.unibs.ing.model.Proposta;
import it.unibs.ing.model.StatoProposta;
import it.unibs.ing.model.Utente;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GestoreProposte implements IProposteFruitore, IProposteConfiguratore {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Bacheca bacheca;
    private Map<String, Proposta> proposteValide = new HashMap<>();

    public GestoreProposte(Bacheca bacheca) {
        if (bacheca == null) {
            this.bacheca = new Bacheca();
        } else {
            this.bacheca = bacheca;
        }
    }

    public GestoreProposte() {
        this.bacheca = new Bacheca();
    }

    public Bacheca getBacheca() {
        return bacheca;
    }

    public Map<String, Proposta> getProposteValide() {
        return proposteValide;
    }

    public boolean validaProposta(Proposta proposta) {
        return proposta.verificaValidita();
    }

    /**
     * Tenta di rendere la proposta APERTA e la appende alla bacheca.
     * 
     * @param proposta la proposta approvata in fase di creazione
     * @throws IllegalArgumentException in caso di proposta non valida
     */
    public void pubblicaProposta(Proposta proposta) {
        try {
            proposta.pubblica();
            bacheca.aggiungiPropostaAperta(proposta);
        } catch (Exception e) {
            throw new IllegalArgumentException("Errore durante la pubblicazione: " + e.getMessage());
        }
    }

    /**
     * Aggancia gli iscritti come Observer sulla proposta.
     * 
     * @param p la proposta in analisi
     * @param sessione l'insieme di account per cercare gli Utenti corrispettivi agli username
     */
    private void collegaObservers(Proposta p, GestoreSessione sessione) {
        for (String username : p.getIscritti()) {
            Utente u = sessione.getUtente(username);
            if (u instanceof it.unibs.ing.model.Observer) {
                p.addObserver((it.unibs.ing.model.Observer) u);
            }
        }
    }

    /**
     * Annulla volontariamente una proposta aperta o confermata che non abbia
     * ancora raggiunto la "Data" di svolgimento. Attiva il ciclo degli observer
     * inviando notifiche di ritiro per forza maggiore.
     * 
     * @param proposta la proposta da sopprimere
     * @param sessione la sessione corrente
     * @return true in caso il ritiro sia ammissibile, false per limiti di tempo raggiunti
     */
    public boolean ritiraProposta(Proposta proposta, GestoreSessione sessione) {
        if (proposta.getStato() == StatoProposta.APERTA || proposta.getStato() == StatoProposta.CONFERMATA) {
            String dataInizioStr = proposta.getValore("Data");
            if (dataInizioStr != null) {
                try {
                    LocalDate dataInizio = LocalDate.parse(dataInizioStr, DATE_FORMATTER);
                    LocalDate oggi = LocalDate.now();

                    if (oggi.isBefore(dataInizio)) {
                        collegaObservers(proposta, sessione);
                        proposta.setStato(StatoProposta.RITIRATA);
                        return true;
                    }
                } catch (DateTimeParseException e) {
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * Routine principale per aggiornare lo stato di vita 
     * di tutte le proposte in bacheca in funzione della data di sistema odierna,
     * determinando CONFERMA, ANNULLAMENTO o CONCLUSIONE dell'evento.
     * 
     * @param sessione la sessione con i riferimenti reali agli Observer da avvisare
     */
    public void controllaScadenze(GestoreSessione sessione) {
        LocalDate oggi = LocalDate.now();
        for (List<Proposta> lista : bacheca.getTutteLeProposte().values()) {
            for (Proposta p : lista) {
                aggiornaStatoProposta(p, oggi, sessione);
            }
        }
    }

    private void aggiornaStatoProposta(Proposta p, LocalDate oggi, GestoreSessione sessione) {
        if (p == null || p.getStato() == null) {
            return;
        }
        try {
            if (p.getStato() == StatoProposta.APERTA) {
                gestisciPropostaAperta(p, oggi, sessione);
            } else if (p.getStato() == StatoProposta.CONFERMATA) {
                gestisciPropostaConfermata(p, oggi, sessione);
            }
        } catch (DateTimeParseException ignored) {
        }
    }

    private void gestisciPropostaAperta(Proposta p, LocalDate oggi, GestoreSessione sessione) {
        int target = 0;
        try {
            target = Integer.parseInt(p.getValore("Numero di partecipanti"));
        } catch (Exception ignored) {
        }

        if (p.getIscritti().size() >= target && target > 0) {
            collegaObservers(p, sessione);
            p.setStato(StatoProposta.CONFERMATA);
        } else {
            String scadenzaStr = p.getValore("Termine ultimo di iscrizione");
            if (scadenzaStr != null) {
                LocalDate scadenza = LocalDate.parse(scadenzaStr, DATE_FORMATTER);
                if (oggi.isAfter(scadenza)) {
                    collegaObservers(p, sessione);
                    p.setStato(StatoProposta.ANNULLATA);
                }
            }
        }
    }

    private void gestisciPropostaConfermata(Proposta p, LocalDate oggi, GestoreSessione sessione) {
        String dataFinStr = p.getValore("Data conclusiva");
        if (dataFinStr != null) {
            LocalDate dataFin = LocalDate.parse(dataFinStr, DATE_FORMATTER);
            if (oggi.isAfter(dataFin)) {
                collegaObservers(p, sessione);
                p.setStato(StatoProposta.CONCLUSA);
            }
        }
    }

    /**
     * Delega alla Bacheca la ricerca avanzata mediante Strategy.
     * 
     * @param strategy la strategia di filtraggio
     * @return le proposte corrispondenti
     */
    @Override
    public List<Proposta> cercaProposte(it.unibs.ing.model.FiltroProposteStrategy strategy) {
        return bacheca.cercaProposte(strategy);
    }
}
