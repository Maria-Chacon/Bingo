/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bingo;
import java.awt.Frame;
import java.awt.TextField;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 *
 * @author MariaCh
 */
public class Cliente extends Frame  {
    private static final long serialVersionUID = 1L;

    private Socket socket;
    private DataInputStream entrada;
    private DataOutputStream salida;
   // private TextField salidaTexto;
    private TextArea entradaTexto;

    public Cliente() {
        setTitle("Bingo");
        setSize(350, 200);
        setResizable(false);

        //salidaTexto = new TextField(30);
        //salidaTexto.addActionListener(this);

        entradaTexto = new TextArea();
        entradaTexto.setEditable(false);

        //add(salidaTexto, "South");
        add(entradaTexto, "Center");

        setVisible(true);

        conectarAlServidor();
    }

    private void conectarAlServidor() {
        try {
            socket = new Socket("localhost", 7000);

            entrada = new DataInputStream(socket.getInputStream());
            salida = new DataOutputStream(socket.getOutputStream());

            new Thread(new EscuchaServidor()).start();
        } catch (IOException ioe) {
            System.out.println("Error al conectar al servidor." + ioe);
            System.exit(1);
        }
    }

//    public void actionPerformed(ActionEvent e) {
//        try {
//            String mensaje = salidaTexto.getText();
//            salida.writeUTF(mensaje);
//            salida.flush();
//            salidaTexto.setText("");
//        } catch (IOException ioe) {
//            System.out.println("Error al enviar mensaje." + ioe);
//        }
//    }

    private class EscuchaServidor implements Runnable {
        public void run() {
            try {
                while (true) {
                    String mensaje = entrada.readUTF();
                    entradaTexto.append(mensaje + "\n");
                }
            } catch (IOException ioe) {
                System.out.println("Error al escuchar al servidor." + ioe);
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

    public static void main(String[] args) {
        new Cliente();
    }
}