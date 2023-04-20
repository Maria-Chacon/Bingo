/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bingo;
import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.Vector;
/**
 *
 * @author MariaCh
 */
public class Servidor extends Thread {
    public static Vector<Flujo> usuarios = new Vector<Flujo>();

    public static void main(String[] args) {
        ServerSocket sfd = null;
        try {
            sfd = new ServerSocket(7000);
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
        Random random = new Random();
        return random.nextInt(75) + 1;
    }
}
