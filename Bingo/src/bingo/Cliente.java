/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bingo;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.TextField;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 *
 * @author MariaCh
 */
public class Cliente extends JFrame {
    private static final long serialVersionUID = 1L;
    private final int PUERTO = 7000;
    private Socket socket;
    private DataInputStream entrada;
    private DataOutputStream salida;
    private JTextArea areaTexto;


    public Cliente() {
        setTitle("Bingo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 300);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        JPanel panel = new JPanel(new BorderLayout());

        areaTexto = new JTextArea();
        areaTexto.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(areaTexto);

        panel.add(scrollPane, BorderLayout.CENTER);

        add(panel, BorderLayout.CENTER);
    // Agregar botón para salir del juego
        JButton salir = new JButton("Salir");
        salir.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        JPanel panelBoton = new JPanel();
        panelBoton.add(salir);
        add(panelBoton, BorderLayout.SOUTH);

        setVisible(true);

        conectarAlServidor();
    }

    private void conectarAlServidor() {
        try {
            socket = new Socket("localhost", PUERTO);

            entrada = new DataInputStream(socket.getInputStream());
            salida = new DataOutputStream(socket.getOutputStream());

            new Thread(new EscuchaServidor()).start();
        } catch (IOException ioe) {
            System.out.println("Error al conectar al servidor." + ioe);
            System.exit(1);
        }
    }

    private class EscuchaServidor implements Runnable {
        public void run() {
            try {
                while (true) {
                    String mensaje = entrada.readUTF();
                    areaTexto.append(mensaje + "\n");
                    areaTexto.setCaretPosition(areaTexto.getDocument().getLength());
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