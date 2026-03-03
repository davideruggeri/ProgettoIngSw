package it.unibs.ing.model;

/**
 * Interfaccia per il pattern Observer (soggetto osservato).
 */
public interface Observable {
    void addObserver(Observer o);

    void removeObserver(Observer o);

    void notifyObservers(String messaggio);
}
