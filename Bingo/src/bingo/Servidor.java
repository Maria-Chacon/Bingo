package bingo;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Vector;

/**
 *
 * @author MariaCh
 */
public class Servidor extends Thread {

    public static Vector<Flujo> usuarios = new Vector<Flujo>();
    private static int indiceActual = 0;
    private static ArrayList<Integer> numeros = new ArrayList<Integer>();

    public static void main(String[] args) {

        ServerSocket sfd = null;
        try {
            sfd = new ServerSocket(5000);
        } catch (IOException ioe) {
            System.out.println("Comunicación rechazada." + ioe);
            System.exit(1);
        }

        System.out.println("Servidor iniciado.");

        while (true) {
            try {
                Socket nsfd = sfd.accept();
                System.out.println("Conexión aceptada de: " + nsfd.getInetAddress());

                Flujo flujo = new Flujo(nsfd);
                usuarios.add(flujo);

                flujo.start();
            } catch (IOException ioe) {
                System.out.println("Error: " + ioe);
            }
        }
    }

    public static void broadcast(String mensaje) {
        for (Flujo flujo : usuarios) {
            flujo.enviarMensaje(mensaje);
        }
    }

    public static int generarNumero() {
        if (indiceActual >= numeros.size()) {
            // Ya se han devuelto todos los números en el ArrayList, mezclar de nuevo
            for (int i = 1; i <= 75; i++) {
                numeros.add(i);
            }
            Collections.shuffle(numeros);
            indiceActual = 0;
        }
        int siguienteNumero = numeros.get(indiceActual);
        indiceActual++;
        return siguienteNumero;

    }
}
