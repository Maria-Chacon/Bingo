package bingo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.TableView.TableCell;

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

        // Crear la tabla
        JTable tabla = new JTable(modeloTabla) {
            // Sobrescribir el método prepareRenderer para establecer el color de fondo de la celda
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                c.setBackground(Color.WHITE);
                return c;
            }
        };
        tabla.setPreferredScrollableViewportSize(new Dimension(500, 300)); // establecer el tamaño de la tabla
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS); // ajustar automáticamente el tamaño de las columnas
        tabla.setRowHeight(100); // Establecer la altura de las filas

        // Establecer el renderizador personalizado para cada celda de la tabla
        DefaultTableCellRenderer renderizador = new DefaultTableCellRenderer();
        renderizador.setHorizontalAlignment(JLabel.CENTER);
        tabla.setDefaultRenderer(Object.class, renderizador);

        // Agregar la tabla al panel
        JPanel panelTabla = new JPanel(new BorderLayout());
        panelTabla.add(tabla.getTableHeader(), BorderLayout.NORTH);
        panelTabla.add(tabla, BorderLayout.CENTER);
        panelTabla.setPreferredSize(new Dimension(500, 1000)); // Establecer el ancho del panel

        // Agregar el panel de la tabla al panel principal
        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.add(panelTabla, BorderLayout.WEST);
        panelPrincipal.add(panelMensajes, BorderLayout.CENTER);
        add(panelPrincipal, BorderLayout.CENTER);

        tabla.setRowSelectionAllowed(false);

        // Declarar un conjunto para llevar el registro de números seleccionados
        Set<Integer> numerosSeleccionados = new HashSet<Integer>();

        // Agregar controlador de eventos de clic a la tabla
        tabla.addMouseListener(new MouseAdapter() {
            private Component ultimaCeldaClickeada = null;

            public void mouseClicked(MouseEvent e) {

                int columna = tabla.columnAtPoint(e.getPoint());
                int fila = tabla.rowAtPoint(e.getPoint());

                if (fila >= 0 && columna >= 0) {
                    // Obtener la celda clickeada
                    TableCellRenderer renderer = tabla.getCellRenderer(fila, columna);
                    Component celda = tabla.prepareRenderer(renderer, fila, columna);

                    // Obtener el valor de la celda clickeada
                    Object valor = tabla.getValueAt(fila, columna);

                    // Si el valor es un entero, mostrar el mensaje en la consola y verificar si ya fue seleccionado
                    if (valor instanceof Integer) {
                        int numero = (Integer) valor;
                        if (numerosSeleccionados.contains(numero)) {
                            System.out.println("El número " + numero + " ya fue seleccionado anteriormente.");
                            return; // Salir del método sin cambiar el color de la celda
                        }
                        System.out.println("Se marcó el número " + numero);
                        numerosSeleccionados.add(numero); // Agregar el número al conjunto de seleccionados

                        // Cambiar el color de la celda clickeada
                        if (ultimaCeldaClickeada != null) {
                            ultimaCeldaClickeada.setBackground(Color.WHITE);
                        }

                        celda.setBackground(Color.GREEN);
                        ultimaCeldaClickeada = celda;

                        // Si se han marcado todas las celdas, imprimir "bingo" en la consola y cerrar el programa
                        if (numerosSeleccionados.size() == matrizObject.length * matrizObject[0].length) {
                            System.out.println("Bingo!");
                            System.exit(0);
                        }
                    }
                }
            }
        });

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

        setVisible(true);

        conectarAlServidor();

    }

    private void conectarAlServidor() {
        try {
            socket = new Socket("192.168.0.6", 5000);

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
