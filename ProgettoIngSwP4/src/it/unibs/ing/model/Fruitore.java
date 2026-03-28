package it.unibs.ing.model;

import java.util.ArrayList;
import java.util.List;

public class Fruitore extends Utente implements Observer {
    private static final long serialVersionUID = 1L;

    private List<String> notifiche;

    public Fruitore(String nomeUtente, String password) {
        super(nomeUtente, password);
        this.notifiche = new ArrayList<>();
    }

    public List<String> getNotifiche() {
        return notifiche;
    }

    public void aggiungiNotifica(String messaggio) {
        notifiche.add(messaggio);
    }

    public boolean rimuoviNotifica(int indice) {
        if (indice >= 0 && indice < notifiche.size()) {
            notifiche.remove(indice);
            return true;
        }
        return false;
    }

    @Override
    public void update(String messaggio) {
        aggiungiNotifica(messaggio);
    }
}
