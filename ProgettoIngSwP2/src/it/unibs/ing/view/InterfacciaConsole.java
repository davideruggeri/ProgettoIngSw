package it.unibs.ing.view;

import java.util.Scanner;

public class InterfacciaConsole {

    private Scanner scanner;

    public InterfacciaConsole() {
        this.scanner = new Scanner(System.in);
    }

    public void stampaMessaggio(String messaggio) {
        System.out.println(messaggio);
    }

    public String leggiStringa(String prompt) {
        System.out.print(prompt + ": ");
        return scanner.nextLine().trim();
    }

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

    public boolean leggiBooleano(String prompt) {
        System.out.print(prompt + " (s/n): ");
        String input = scanner.nextLine().trim().toLowerCase();
        return input.equals("s") || input.equals("si") || input.equals("y");
    }
}
