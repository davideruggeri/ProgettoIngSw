package it.unibs.ing.controller;

import it.unibs.ing.model.Bacheca;
import it.unibs.ing.model.Proposta;
import it.unibs.ing.model.StatoProposta;
import it.unibs.ing.model.Utente;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Controller per la gestione logica delle proposte di iniziativa
 * e dell'interazione con la Bacheca e Archivio.
 */
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

    /**
     * Tenta di validare una proposta. Se non è valida, lancia eccezione.
     * Altrimenti lo stato interno della proposta verrà settato a VALIDA.
     */
    public boolean validaProposta(Proposta proposta) {
        return proposta.verificaValidita();
    }

    /**
     * Pubblica una proposta valida in bacheca.
     */
    public void pubblicaProposta(Proposta proposta) {
        try {
            proposta.pubblica(); // Passa a stato APERTA, throws exception se non valida
            bacheca.aggiungiPropostaAperta(proposta);
        } catch (Exception e) {
            throw new IllegalArgumentException("Errore durante la pubblicazione: " + e.getMessage());
        }
    }

    private void collegaObservers(Proposta p, GestoreSessione sessione) {
        for (String username : p.getIscritti()) {
            Utente u = sessione.getUtente(username);
            if (u instanceof it.unibs.ing.model.Observer) {
                p.addObserver((it.unibs.ing.model.Observer) u);
            }
        }
    }

    /**
     * Permette al configuratore di ritirare una proposta APERTA o CONFERMATA,
     * a patto che non sia ancora scaduto il giorno precedente alla "Data"
     * dell'evento.
     * Notifica tutti i fruitori iscritti se presente.
     */
    public boolean ritiraProposta(Proposta proposta, GestoreSessione sessione) {
        if (proposta.getStato() == StatoProposta.APERTA || proposta.getStato() == StatoProposta.CONFERMATA) {
            String dataInizioStr = proposta.getValore("Data");
            if (dataInizioStr != null) {
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    LocalDate dataInizio = LocalDate.parse(dataInizioStr, formatter);
                    LocalDate oggi = LocalDate.now();

                    // Il ritiro può avvenire fino alle "ore 23.59 del giorno precedente quello
                    // indicato dal campo Data"
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
     * Algoritmo schedulato (invocato al login) che verifica tutte le proposte.
     * Cambia lo stato a CONFERMATA/ANNULLATA se scade il termine di iscrizione.
     * Cambia lo stato a CONCLUSA se è passata la data conclusiva di un evento
     * confermato.
     * Invia le notifiche nell'area personale dei Fruitori.
     */
    public void controllaScadenze(GestoreSessione sessione) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate oggi = LocalDate.now();

        // Iteriamo su tutte le proposte per aggiornare gli stati
        for (List<Proposta> lista : bacheca.getTutteLeProposte().values()) {
            for (Proposta p : lista) {
                try {
                    if (p.getStato() == StatoProposta.APERTA) {
                        String scadenzaStr = p.getValore("Termine ultimo di iscrizione");
                        if (scadenzaStr != null) {
                            LocalDate scadenza = LocalDate.parse(scadenzaStr, formatter);
                            if (oggi.isAfter(scadenza)) {
                                int target = 0;
                                try {
                                    target = Integer.parseInt(p.getValore("Numero di partecipanti"));
                                } catch (Exception ignored) {
                                }

                                collegaObservers(p, sessione);

                                if (p.getIscritti().size() >= target) {
                                    p.setStato(StatoProposta.CONFERMATA);
                                } else {
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
                    // Dati incorretti salvati precedentemente
                }
            }
        }
    }
}
