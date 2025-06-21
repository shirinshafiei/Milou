package aut.ap;

import aut.ap.framwork.SingletonSessionFactory;
import aut.ap.ui.CLI;
import aut.ap.ui.GUI;

import javax.swing.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("Welcome to Milou Mail!");
        System.out.println("Choose your interface: [1] CLI  |  [2] GUI");
        System.out.print("Your choice: ");

        Scanner scanner = new Scanner(System.in);
        String choice = scanner.nextLine().trim();

        try {
            switch (choice) {
                case "1":
                    CLI cli = new CLI();
                    cli.start();
                    break;
                case "2":
                    GUI gui = new GUI();
                    gui.start();
                    break;
                default:
                    System.err.println("Invalid choice. Please restart the application.");
                    return;
            }
        } finally {
           SingletonSessionFactory.close();
        }
    }
}