package it.unibs.ing.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Proposta implements Observable {
    private Categoria categoria;
    private Map<String, String> valoriCampi;
    private StatoProposta stato;
    private List<String> iscritti;
    private transient List<Observer> observers;

    public Proposta(Categoria categoria) {
        this.categoria = categoria;
        this.valoriCampi = new LinkedHashMap<>();
        this.stato = null;
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

    /**
     * Sostituisce lo Stato e, in caso di variazione rispetto all'esistente, 
     * innesca le Notifiche Observer a tutti gli iscritti.
     * 
     * @param nuovoStato enumerazione nuova per la vita della proposta
     */
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
     * Ispeziona i posti totali confrontandoli con quelli occupati.
     * 
     * @return booleano di idoneità all'iscrizione
     */
    public boolean puoIscrivere() {
        try {
            int maxPartecipanti = Integer.parseInt(getValore("Numero di partecipanti"));
            return iscritti.size() < maxPartecipanti;
        } catch (NumberFormatException | NullPointerException e) {
            return false;
        }
    }

    /**
     * Allega il Fruitore alla proposta, ma non lo instrada come Observer.
     * Avverrà un controllo automatico in altre fasi per riagganciarlo come listener.
     * 
     * @param usernameFruitore username univoco
     * @return l'esito del tesseramento all'evento
     */
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
     * Valutazione robusta che copre campi generici e parsing rigoroso delle scadenze.
     * Imposta implicitamente lo stato a VALIDA e lo restituisce.
     * 
     * @return true se tutti i criteri temporali e formali della categoria sono assecondati
     */
    public boolean verificaValidita() {

        for (Campo c : categoria.getCampi().values()) {
            if (c.isObbligatorio()) {
                String val = valoriCampi.get(c.getNome());
                if (val == null || val.trim().isEmpty()) {
                    return false;
                }
            }
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String dataTermineStr = valoriCampi.get("Termine ultimo di iscrizione");
            String dataInizioStr = valoriCampi.get("Data");

            if (dataTermineStr != null && dataInizioStr != null) {
                LocalDate dataTermine = LocalDate.parse(dataTermineStr, formatter);
                LocalDate dataInizio = LocalDate.parse(dataInizioStr, formatter);
                LocalDate oggi = LocalDate.now();

                if (!dataTermine.isAfter(oggi)) {
                    return false;
                }

                if (!dataInizio.isAfter(dataTermine.plusDays(1))) {

                    return false;
                }
            }
        } catch (DateTimeParseException | NullPointerException e) {

            return false;
        }

        this.stato = StatoProposta.VALIDA;
        return true;
    }

    /**
     * Consente un'immissione formale della proposta rendendola pubblica ed APERTA
     * all'adesione dei fruitori di sistema.
     * 
     * @throws IllegalStateException se i requisiti logico-formali non sono pronti
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
