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

    public void pubblicaProposta(Proposta proposta) {
        try {
            proposta.pubblica();
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
