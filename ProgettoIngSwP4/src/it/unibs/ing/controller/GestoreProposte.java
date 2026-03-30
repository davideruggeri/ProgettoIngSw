package it.unibs.ing.controller;

import it.unibs.ing.model.Bacheca;
import it.unibs.ing.model.Proposta;
import it.unibs.ing.model.StatoProposta;
import it.unibs.ing.model.Utente;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class GestoreProposte {
    private Bacheca bacheca;

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

    public boolean validaProposta(Proposta proposta) {
        return proposta.verificaValidita();
    }

    /**
     * Valida e pubblica una proposta in bacheca cambiandone lo stato.
     * 
     * @param proposta la proposta da pubblicare
     * @throws IllegalArgumentException in caso di proposta non valida o problemi in fase di pubblicazione
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
     * Associa gli utenti (che si sono iscritti) in qualità di Observer per notificare
     * eventuali cambiamenti di stato della proposta stessa.
     * 
     * @param p la proposta da monitorare
     * @param sessione la sessione corrente per recuperare gli oggetti Utente corretti
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
     * Consente al creatore (o a chi ne ha i permessi) di ritirare una proposta 
     * aperta o confermata, purché l'evento non sia già in corso di svolgimento o passato.
     * 
     * @param proposta la proposta da ritirare
     * @param sessione la sessione per collegare temporaneamente gli Observer prima della transizione
     * @return true se il ritiro va a buon fine, false se non è ammissibile ritirarla
     */
    public boolean ritiraProposta(Proposta proposta, GestoreSessione sessione) {
        if (proposta.getStato() == StatoProposta.APERTA || proposta.getStato() == StatoProposta.CONFERMATA) {
            String dataInizioStr = proposta.getValore("Data");
            if (dataInizioStr != null) {
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    LocalDate dataInizio = LocalDate.parse(dataInizioStr, formatter);
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
     * Scansiona tutte le proposte aperte o confermate in bacheca confrontandone
     * le date di scadenza con la data odierna. Aggiorna lo stato in CONFERMATA,
     * ANNULLATA o CONCLUSA a seconda delle condizioni temporali e degli iscritti,
     * avvisando gli Observer (fruitori partecipanti).
     * 
     * @param sessione la sessione necessaria per collegare gli Observer (partecipanti)
     */
    public void controllaScadenze(GestoreSessione sessione) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate oggi = LocalDate.now();

        for (List<Proposta> lista : bacheca.getTutteLeProposte().values()) {
            for (Proposta p : lista) {
                try {
                    if (p.getStato() == StatoProposta.APERTA) {
                        int target = 0;
                        try {
                            target = Integer.parseInt(p.getValore("Numero di partecipanti"));
                        } catch (Exception ignored) {
                        }

                        if (p.getIscritti().size() >= target) {
                            collegaObservers(p, sessione);
                            p.setStato(StatoProposta.CONFERMATA);
                        } else {
                            String scadenzaStr = p.getValore("Termine ultimo di iscrizione");
                            if (scadenzaStr != null) {
                                LocalDate scadenza = LocalDate.parse(scadenzaStr, formatter);
                                if (oggi.isAfter(scadenza)) {
                                    collegaObservers(p, sessione);
                                    p.setStato(StatoProposta.ANNULLATA);
                                }
                            }
                        }
                    } else if (p.getStato() == StatoProposta.CONFERMATA) {
                        String dataFinStr = p.getValore("Data conclusiva");
                        if (dataFinStr != null) {
                            LocalDate dataFin = LocalDate.parse(dataFinStr, formatter);
                            if (oggi.isAfter(dataFin)) {
                                collegaObservers(p, sessione);
                                p.setStato(StatoProposta.CONCLUSA);
                            }
                        }
                    }
                } catch (DateTimeParseException ignored) {

                }
            }
        }
    }
}
