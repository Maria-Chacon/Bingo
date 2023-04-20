package bingo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import javax.swing.JButton;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultHighlighter;

/**
 *
 * @author MariaCh
 */
public class Cliente extends JFrame {

    private static final long serialVersionUID = 1L;

    private Socket socket;
    private DataInputStream entrada;
    private DataOutputStream salida;
    private JTextArea entradaTexto;
    private ArrayList<Integer> numerosMarcados = new ArrayList<>();

    public Cliente() {

        setTitle("Bingo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 300);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        JPanel panel = new JPanel(new BorderLayout());

        entradaTexto = new JTextArea();
        entradaTexto.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(entradaTexto);

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

        // Hace que la JTextArea siempre muestre el último mensaje recibido
        DefaultCaret caret = (DefaultCaret) entradaTexto.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        entradaTexto.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int offset = entradaTexto.viewToModel2D(e.getPoint());
                try {
                    int start = entradaTexto.getLineStartOffset(entradaTexto.getLineOfOffset(offset));
                    int end = entradaTexto.getLineEndOffset(entradaTexto.getLineOfOffset(offset));
                    String texto = entradaTexto.getText(start, end - start).trim();
                    String numeroString = texto.replaceAll("\\D+", "");
                    if (numeroString.isEmpty()) {
                        return;
                    }
                    int numero = Integer.parseInt(numeroString);

                    if (numerosMarcados.contains(numero)) {
                        System.out.println("El número " + numero + " ya está marcado.");
                    } else {
                        numerosMarcados.add(numero);
                        System.out.println("Número marcado: " + numero);
                    }

                } catch (BadLocationException ex) {
                    System.out.println("Error al marcar número: ");
                    ex.printStackTrace();
                } catch (NumberFormatException ex) {
                    System.out.println("Error al obtener el número: ");
                    ex.printStackTrace();
                }
            }
        });

        setVisible(true);

        conectarAlServidor();

    }

    private void conectarAlServidor() {
        try {
            socket = new Socket("localHost", 7000);

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
