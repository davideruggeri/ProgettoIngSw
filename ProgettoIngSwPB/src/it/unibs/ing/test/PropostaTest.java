package it.unibs.ing.test;

import it.unibs.ing.model.Categoria;
import it.unibs.ing.model.Proposta;
import it.unibs.ing.model.StatoProposta;
import it.unibs.ing.model.TipoCampo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Suite di test unitari focalizzata sul metodo Proposta.verificaValidita().
 * 
 * Requisito testato: Validazione dei criteri formali (campi obbligatori) e
 * temporali (scadenze).
 * Tipologie di testing applicate:
 * - Black-Box Testing: Equivalence Partitioning (partizioni valide/non valide)
 * & Boundary Value Analysis (valori limite sulle date).
 * - White-Box Testing: Statement & Branch Coverage (ramificazione degli if e
 * gestione delle eccezioni).
 */
public class PropostaTest {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static void main(String[] args) {
        System.out.println("=== ESECUZIONE TEST UNITARI: Proposta.verificaValidita() ===");

        int superati = 0;
        int falliti = 0;

        if (testVerificaValidita_HappyPath())
            superati++;
        else
            falliti++;
        if (testVerificaValidita_CampoObbligatorioMancante())
            superati++;
        else
            falliti++;
        if (testVerificaValidita_DataTermineNelPassato_Boundary())
            superati++;
        else
            falliti++;
        if (testVerificaValidita_DataInizioTroppoVicina_Boundary())
            superati++;
        else
            falliti++;
        if (testVerificaValidita_FormatoDataErrato_ExceptionHandling())
            superati++;
        else
            falliti++;

        System.out.println("\n=== RISULTATI TEST: " + superati + " PASSED, " + falliti + " FAILED ===");
        if (falliti > 0) {
            System.exit(1);
        }
    }

    /**
     * TEST 1 (Happy Path - Black-Box):
     * Campi obbligatori presenti e date valide (Termine > oggi, Data Inizio >
     * Termine + 1 giorno).
     * Risultato atteso: true e stato impostato a VALIDA.
     */
    public static boolean testVerificaValidita_HappyPath() {
        Categoria cat = new Categoria("Sport", "Attività sportive");
        cat.creaCampo("Titolo", "Titolo dell'evento", true, TipoCampo.STRINGA);

        Proposta p = new Proposta(cat);
        p.impostaValore("Titolo", "Torneo di Calcio");

        LocalDate oggi = LocalDate.now();
        LocalDate termineIscrizione = oggi.plusDays(5);
        LocalDate dataInizio = termineIscrizione.plusDays(3);

        p.impostaValore("Termine ultimo di iscrizione", termineIscrizione.format(FORMATTER));
        p.impostaValore("Data", dataInizio.format(FORMATTER));

        boolean esito = p.verificaValidita();
        boolean statoOk = p.getStato() == StatoProposta.VALIDA;

        return valutaTest("testVerificaValidita_HappyPath", esito && statoOk);
    }

    /**
     * TEST 2 (Equivalence Partitioning - Non Valido):
     * Campo obbligatorio mancante/vuoto.
     * Risultato atteso: false.
     */
    public static boolean testVerificaValidita_CampoObbligatorioMancante() {
        Categoria cat = new Categoria("Cultura", "Eventi culturali");
        cat.creaCampo("DescrizioneEstesa", "Descrizione del libro", true, TipoCampo.STRINGA);

        Proposta p = new Proposta(cat);
        // "DescrizioneEstesa" non viene impostato (rimane null/vuoto)

        boolean esito = p.verificaValidita();

        return valutaTest("testVerificaValidita_CampoObbligatorioMancante", !esito);
    }

    /**
     * TEST 3 (Boundary Value Analysis - Non Valido):
     * Data di termine iscrizione coincidente con la data di oggi (limite non valido
     * poiché deve essere strettamente nel futuro).
     * Risultato atteso: false.
     */
    public static boolean testVerificaValidita_DataTermineNelPassato_Boundary() {
        Categoria cat = new Categoria("Musica", "Concerti");

        Proposta p = new Proposta(cat);
        LocalDate oggi = LocalDate.now();

        p.impostaValore("Termine ultimo di iscrizione", oggi.format(FORMATTER));
        p.impostaValore("Data", oggi.plusDays(10).format(FORMATTER));

        boolean esito = p.verificaValidita();

        return valutaTest("testVerificaValidita_DataTermineNelPassato_Boundary", !esito);
    }

    /**
     * TEST 4 (Boundary Value Analysis - Non Valido):
     * Data Inizio dell'evento pari a Termine + 1 giorno (Boundary non valido poiché
     * deve valere dataInizio > term + 1).
     * Risultato atteso: false.
     */
    public static boolean testVerificaValidita_DataInizioTroppoVicina_Boundary() {
        Categoria cat = new Categoria("Teatro", "Spettacoli");

        Proposta p = new Proposta(cat);
        LocalDate oggi = LocalDate.now();
        LocalDate termine = oggi.plusDays(5);
        LocalDate dataInizioErrata = termine.plusDays(1); // Esattamente Termine + 1

        p.impostaValore("Termine ultimo di iscrizione", termine.format(FORMATTER));
        p.impostaValore("Data", dataInizioErrata.format(FORMATTER));

        boolean esito = p.verificaValidita();

        return valutaTest("testVerificaValidita_DataInizioTroppoVicina_Boundary", !esito);
    }

    /**
     * TEST 5 (White-Box / Exception Handling):
     * Stringa data con formato errato (es. "2026-08-18" anziché "dd/MM/yyyy").
     * Risultato atteso: intercettazione di DateTimeParseException e ritorno di
     * false.
     */
    public static boolean testVerificaValidita_FormatoDataErrato_ExceptionHandling() {
        Categoria cat = new Categoria("Cinema", "Rassegna cinematografica");

        Proposta p = new Proposta(cat);
        p.impostaValore("Termine ultimo di iscrizione", "2026-08-18"); // Formato errato ISO
        p.impostaValore("Data", "25/08/2026");

        boolean esito = p.verificaValidita();

        return valutaTest("testVerificaValidita_FormatoDataErrato_ExceptionHandling", !esito);
    }

    private static boolean valutaTest(String nomeTest, boolean successo) {
        if (successo) {
            System.out.println("  [PASS] " + nomeTest);
        } else {
            System.err.println("  [FAIL] " + nomeTest);
        }
        return successo;
    }
}
