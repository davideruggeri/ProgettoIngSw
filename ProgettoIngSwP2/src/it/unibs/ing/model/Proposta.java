package it.unibs.ing.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

public class Proposta {
    private Categoria categoria;
    private Map<String, String> valoriCampi;
    private StatoProposta stato;

    public Proposta(Categoria categoria) {
        this.categoria = categoria;
        this.valoriCampi = new HashMap<>();
        this.stato = null;
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

    public void setStato(StatoProposta stato) {
        this.stato = stato;
    }

    /**
     * Controlla che tutti i campi obbligatori definiti dalla Categoria siano stati compilati 
     * e valuta la coerenza temporale delle date di iscrizione e svolgimento.
     * In caso di successo, la proposta diventa automaticamente VALIDA.
     * 
     * @return true se la proposta rispetta tutti i vincoli, false altrimenti
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
     * Tenta di rendere la proposta APERTA verificandone prima la validità strutturale.
     * L'apertura consente ai fruitori di iscriversi.
     * 
     * @throws IllegalStateException se l'esito di verificaValidita() è falso
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
