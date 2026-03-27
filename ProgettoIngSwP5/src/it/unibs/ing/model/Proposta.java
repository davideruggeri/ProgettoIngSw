package it.unibs.ing.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Rappresenta un'iniziativa proposta da un Configuratore.
 */
public class Proposta implements Observable {
    private Categoria categoria;
    private Map<String, String> valoriCampi;
    private StatoProposta stato;
    private List<String> iscritti;
    private transient List<Observer> observers;

    public Proposta(Categoria categoria) {
        this.categoria = categoria;
        this.valoriCampi = new LinkedHashMap<>();
        this.stato = null; // Stato iniziale nullo finché non validata o impostata diversamente
        this.iscritti = new ArrayList<>();
        this.observers = new ArrayList<>();
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public Map<String, String> getValoriCampi() {
        return valoriCampi;
    }

    public void setValoriCampi(Map<String, String> valoriCampi) {
        this.valoriCampi = valoriCampi;
    }

    public void impostaValore(String nomeCampo, String valore) {
        this.valoriCampi.put(nomeCampo, valore);
    }

    public String getValore(String nomeCampo) {
        return this.valoriCampi.get(nomeCampo);
    }

    public StatoProposta getStato() {
        return stato;
    }

    public void setStato(StatoProposta nuovoStato) {
        if (this.stato != nuovoStato) {
            this.stato = nuovoStato;
            if (nuovoStato != null) {
                notifyObservers("La proposta '" + categoria.getNome() + "' ha cambiato stato in: " + nuovoStato);
            }
        }
    }

    public List<String> getIscritti() {
        return iscritti;
    }

    private void initObservers() {
        if (observers == null) {
            observers = new ArrayList<>();
        }
    }

    @Override
    public void addObserver(Observer o) {
        initObservers();
        if (!observers.contains(o)) {
            observers.add(o);
        }
    }

    @Override
    public void removeObserver(Observer o) {
        initObservers();
        observers.remove(o);
    }

    @Override
    public void notifyObservers(String messaggio) {
        initObservers();
        for (Observer o : observers) {
            o.update(messaggio);
        }
    }

    /**
     * @return true se c'è ancora posto, false se i posti sono esauriti.
     */
    public boolean puoIscrivere() {
        try {
            int maxPartecipanti = Integer.parseInt(getValore("Numero di partecipanti"));
            return iscritti.size() < maxPartecipanti;
        } catch (NumberFormatException | NullPointerException e) {
            return false;
        }
    }

    public boolean aggiungiIscritto(String usernameFruitore) {
        if (!iscritti.contains(usernameFruitore) && puoIscrivere()) {
            iscritti.add(usernameFruitore);
            return true;
        }
        return false;
    }

    public boolean rimuoviIscritto(String usernameFruitore) {
        return iscritti.remove(usernameFruitore);
    }

    /**
     * Valida la proposta controllando i campi obbligatori e le date.
     * 
     * @return true se la proposta è valida, false altrimenti
     */
    public boolean verificaValidita() {
        // 1. Controllo campi obbligatori
        for (Campo c : categoria.getCampi().values()) {
            if (c.isObbligatorio()) {
                String val = valoriCampi.get(c.getNome());
                if (val == null || val.trim().isEmpty()) {
                    return false;
                }
            }
        }

        // 2. Controllo date
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String dataTermineStr = valoriCampi.get("Termine ultimo di iscrizione");
            String dataInizioStr = valoriCampi.get("Data");

            if (dataTermineStr != null && dataInizioStr != null) {
                LocalDate dataTermine = LocalDate.parse(dataTermineStr, formatter);
                LocalDate dataInizio = LocalDate.parse(dataInizioStr, formatter);
                LocalDate oggi = LocalDate.now();

                // Il termine iscrizione deve essere nel futuro rispetto a oggi
                if (!dataTermine.isAfter(oggi)) {
                    return false;
                }

                // La data dell'evento deve essere almeno due giorni dopo il termine di
                // iscrizione
                if (!dataInizio.isAfter(dataTermine.plusDays(1))) { // plusDays(1) copre "successiva di almeno due
                                                                    // giorni" (deve superare dataTermine + 1)
                    return false; // se è "successiva di almeno 2 giorni", significa dataInizio >= dataTermine + 2
                }
            }
        } catch (DateTimeParseException | NullPointerException e) {
            // Se le date non sono formattate bene o mancano valori fondamentali, non è
            // valida
            return false;
        }

        this.stato = StatoProposta.VALIDA;
        return true;
    }

    /**
     * Pubblica la proposta facendola passare allo stato APERTA, a patto che sia
     * VALIDA.
     */
    public void pubblica() {
        if (this.stato == StatoProposta.VALIDA || verificaValidita()) {
            this.stato = StatoProposta.APERTA;
        } else {
            throw new IllegalStateException("Impossibile pubblicare una proposta non valida.");
        }
    }

    @Override
    public String toString() {
        return "Proposta [Categoria=" + categoria.getNome() + ", Stato=" + stato + "]";
    }
}
