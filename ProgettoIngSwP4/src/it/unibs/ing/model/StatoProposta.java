package it.unibs.ing.model;

/**
 * Rappresenta i possibili stati di una Proposta secondo i requisiti della
 * Versione 2.
 */
public enum StatoProposta {
    /**
     * La proposta ha superato i controlli di validità (tutti i campi obbligatori
     * compilati correttamente e le date rispettano i vincoli).
     */
    VALIDA,

    /**
     * La proposta valida è stata pubblicata nella bacheca ed è pronta per
     * raccogliere iscrizioni.
     */
    APERTA,

    /**
     * La proposta ha raggiunto il target di iscritti entro il termine utile.
     */
    CONFERMATA,

    /**
     * La proposta non ha raggiunto il target di iscritti ed è scaduta.
     */
    ANNULLATA,

    /**
     * La proposta confermata si è conclusa e il giorno finale è passato.
     */
    CONCLUSA,

    /**
     * La proposta è stata ritirata per cause di forza maggiore prima della sua
     * esecuzione.
     */
    RITIRATA
}
