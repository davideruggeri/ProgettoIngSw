package it.unibs.ing.model;

/**
 * Interfaccia per il pattern Observer.
 * Gli oggetti che implementano questa interfaccia possono registrarsi agli
 * Observable.
 */
public interface Observer {
    void update(String messaggio);
}
