/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bingo;

import java.io.*;
import java.net.*;

/**
 *
 * @author MariaCh
 */
public class Flujo extends Thread {
    private Socket socket;
    private DataInputStream entrada;
    private DataOutputStream salida;

    public Flujo(Socket socket) {
        this.socket = socket;

        try {
            entrada = new DataInputStream(socket.getInputStream());
            salida = new DataOutputStream(socket.getOutputStream());
        } catch (IOException ioe) {
            System.out.println("Error al crear flujo de entrada/salida." + ioe);
        }
    }

    public void enviarMensaje(String mensaje) {
        try {
            salida.writeUTF(mensaje);
            salida.flush();
        } catch (IOException ioe) {
            System.out.println("Error al enviar mensaje." + ioe);
        }
    }

    public void run() {
        try {
            while (true) {
                int numero = Servidor.generarNumero();
                Servidor.broadcast("Número generado: " + numero);

                Thread.sleep(5000);
            }
        } catch (InterruptedException ie) {
            System.out.println("Error: " + ie);
        } finally {
            try {
                entrada.close();
                salida.close();
                socket.close();
            } catch (IOException ioe) {
                System.out.println("Error al cerrar el socket." + ioe);
            }
        }
    }
}
