package bingo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
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
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
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
//    private JLabel[][] matrizNumeros;
    private final JButton[][] carton = new JButton[5][5];
    private final Random random = new Random();

    public Cliente() {

        setTitle("Bingo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        JPanel panelMensajes = new JPanel(new BorderLayout());
        panelMensajes.setPreferredSize(new Dimension(500, 300));

        entradaTexto = new JTextArea();
        entradaTexto.setEditable(false);
        entradaTexto.setLineWrap(true);

        JScrollPane scrollPaneMensajes = new JScrollPane(entradaTexto);
        scrollPaneMensajes.setPreferredSize(new Dimension(500, 300));
        scrollPaneMensajes.setBorder(BorderFactory.createTitledBorder("Mensajes"));

        panelMensajes.add(scrollPaneMensajes, BorderLayout.CENTER);

        // Crear la tabla con la matriz y agregarla al panel
        int[][] matriz = new int[5][5];
        Set<Integer> numerosGenerados = new HashSet<>();
        Random random = new Random();

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                int numero;
                do {
                    numero = random.nextInt(75) + 1;
                } while (numerosGenerados.contains(numero));
                numerosGenerados.add(numero);
                matriz[i][j] = numero;
            }
        }

            // Convertir los valores de int a Object
        Object[][] matrizObject = new Object[5][5];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                matrizObject[i][j] = Integer.valueOf(matriz[i][j]);
            }
        }

        // Crear el modelo de la tabla y agregar la matriz
        DefaultTableModel modeloTabla = new DefaultTableModel();
        modeloTabla.setDataVector(matrizObject, new Object[]{"B", "I", "N", "G", "O"});

        // Crear la tabla y agregarla al panel
        JTable tabla = new JTable(modeloTabla);
        tabla.setPreferredScrollableViewportSize(new Dimension(500, 300)); // establecer el tamaño de la tabla
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS); // ajustar automáticamente el tamaño de las columnas
        JPanel panelTabla = new JPanel(new BorderLayout());
        panelTabla.add(tabla.getTableHeader(), BorderLayout.NORTH);
        panelTabla.add(tabla, BorderLayout.CENTER);
        panelTabla.setPreferredSize(new Dimension(500, 1000)); // Establecer el ancho del panel
        

        // Agregar el panel de la tabla al panel principal
        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.add(panelTabla, BorderLayout.WEST);
        panelPrincipal.add(panelMensajes, BorderLayout.CENTER);
        add(panelPrincipal, BorderLayout.CENTER);

        // Agregar botón para salir del juego
        JButton salir = new JButton("Salir");
        salir.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    entrada.close();
                    salida.close();
                    socket.close();
                } catch (IOException ex) {
                    Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
                }
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
