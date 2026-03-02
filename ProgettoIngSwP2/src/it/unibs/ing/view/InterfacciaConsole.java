package it.unibs.ing.view;

import java.util.Scanner;

/**
 * Classe di utilità per gestire l'input/output da console.
 * Fornisce metodi per leggere stringhe, interi e booleani in modo robusto.
 */
public class InterfacciaConsole {

    private Scanner scanner;

    public InterfacciaConsole() {
        this.scanner = new Scanner(System.in);
    }

    /**
     * Stampa un messaggio a video.
     * 
     * @param messaggio Il messagio da stampare.
     */
    public void stampaMessaggio(String messaggio) {
        System.out.println(messaggio);
    }

    /**
     * Legge una stringa da input.
     * 
     * @param prompt Il messaggio di richiesta da mostrare..
     * @return La stringa inserita.
     */
    public String leggiStringa(String prompt) {
        System.out.print(prompt + ": ");
        return scanner.nextLine().trim();
    }

    /**
     * Legge un intero da input, gestendo errori di formato.
     * 
     * @param prompt Il messaggio di richiesta da mostrare.
     * @return Il numero intero inserito.
     */
    public int leggiIntero(String prompt) {
        while (true) {
            System.out.print(prompt + ": ");
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Input non valido. Inserire un numero.");
            }
        }
    }

    /**
     * Legge una risposta booleana (si/no).
     * 
     * @param prompt La domanda da porre.
     * @return true per 's', 'si', 'y', false altrimenti.
     */
    public boolean leggiBooleano(String prompt) {
        System.out.print(prompt + " (s/n): ");
        String input = scanner.nextLine().trim().toLowerCase();
        return input.equals("s") || input.equals("si") || input.equals("y");
    }
}
