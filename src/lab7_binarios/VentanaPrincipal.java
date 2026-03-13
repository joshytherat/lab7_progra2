package lab7_binarios;

import javax.swing.*;
import javax.swing.border.*;
import javax.sound.sampled.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class VentanaPrincipal extends JFrame implements ReproductorMusica.ReproductorListener {

    private static final Color BG_BASE = new Color(0, 0, 0);
    private static final Color BG_DARK = new Color(18, 18, 18);
    private static final Color BG_ELEVATED = new Color(40, 40, 40);
    private static final Color BG_HOVER = new Color(52, 52, 52);
    private static final Color ACCENT = new Color(30, 215, 96);
    private static final Color ACCENT_DIM = new Color(21, 160, 70);
    private static final Color TEXT_WHITE = new Color(255, 255, 255);
    private static final Color TEXT_GRAY = new Color(179, 179, 179);
    private static final Color TEXT_DIM = new Color(107, 107, 107);
    private static final Color TRACK_BG = new Color(80, 80, 80);
    private static final Color SEP = new Color(30, 30, 30);
    private static final Color ERR = new Color(220, 60, 60);

    private final ReproductorMusica reproductor;
    private final GestorCancionesArchivo gestor;
    private final ArrayList<Cancion> canciones = new ArrayList<>();
    private int idxSel = -1;

    private JLabel lblArtwork;
    private JLabel lblNombre;
    private JLabel lblArtista;
    private JLabel lblGenero;
    private JProgressBar progressBar;
    private JLabel lblTiempoActual;
    private JLabel lblTiempoTotal;
    private JLabel lblEstado;

    private CtrlButton btnPlay;
    private CtrlButton btnPause;
    private CtrlButton btnStop;

    private JPanel listaPanel;
    private JPanel panelForm;
    private boolean formVisible = false;

    private JTextField txtNombre;
    private JTextField txtArtista;
    private JTextField txtDuracion;
    private JTextField txtRutaAudio;
    private JTextField txtRutaImagen;
    private JComboBox<GeneroMusical> cmbGenero;

    private JLabel lblFeedback;
    private Timer feedbackTimer;

    public VentanaPrincipal() {
        reproductor = new ReproductorMusica();
        gestor = new GestorCancionesArchivo(System.getProperty("user.home") + File.separator + "reproductor_canciones.dat");
        reproductor.agregarListener(this);
        configurarVentana();
        construirUI();
        cargarCanciones();
    }

    private void configurarVentana() {
        setTitle("Reproductor de Música");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 720);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
        }
    }

    private void construirUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_DARK);
        setContentPane(root);
        root.add(construirSidebar(), BorderLayout.WEST);
        root.add(construirCentro(), BorderLayout.CENTER);
        root.add(construirPie(), BorderLayout.SOUTH);
    }

    private JPanel construirSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(310, 0));
        sidebar.setBackground(BG_BASE);
        sidebar.setBorder(new MatteBorder(0, 0, 0, 1, SEP));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_BASE);
        header.setBorder(BorderFactory.createEmptyBorder(22, 18, 10, 18));

        JLabel lblLib = new JLabel("Tu Biblioteca");
        lblLib.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblLib.setForeground(TEXT_GRAY);
        header.add(lblLib, BorderLayout.WEST);

        JPanel btnHead = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        btnHead.setOpaque(false);
        IconBtn btnAdd = new IconBtn(true, "Agregar canción");
        IconBtn btnDel = new IconBtn(false, "Eliminar seleccionada");
        btnAdd.addActionListener(e -> toggleFormulario());
        btnDel.addActionListener(e -> eliminarSeleccionada());
        btnHead.add(btnDel);
        btnHead.add(btnAdd);
        header.add(btnHead, BorderLayout.EAST);
        sidebar.add(header, BorderLayout.NORTH);

        panelForm = construirFormulario();
        panelForm.setVisible(false);

        listaPanel = new JPanel();
        listaPanel.setLayout(new BoxLayout(listaPanel, BoxLayout.Y_AXIS));
        listaPanel.setBackground(BG_BASE);

        JScrollPane scroll = new JScrollPane(listaPanel);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG_BASE);
        scroll.getVerticalScrollBar().setUI(new DarkScrollUI());

        JPanel contenido = new JPanel();
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setBackground(BG_BASE);
        contenido.add(panelForm);
        contenido.add(scroll);
        sidebar.add(contenido, BorderLayout.CENTER);

        lblFeedback = new JLabel(" ");
        lblFeedback.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblFeedback.setForeground(ACCENT);
        lblFeedback.setBackground(BG_BASE);
        lblFeedback.setOpaque(true);
        lblFeedback.setBorder(BorderFactory.createEmptyBorder(6, 18, 8, 18));
        sidebar.add(lblFeedback, BorderLayout.SOUTH);

        return sidebar;
    }

    private JPanel construirFormulario() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(new Color(26, 26, 26));
        p.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, SEP),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));

        JLabel titulo = new JLabel("Nueva Canción");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 14));
        titulo.setForeground(TEXT_WHITE);
        titulo.setAlignmentX(LEFT_ALIGNMENT);
        p.add(titulo);
        p.add(Box.createVerticalStrut(12));

        txtRutaAudio = crearCampo("Sin seleccionar...");
        txtRutaAudio.setEditable(false);
        JButton btnAudio = crearBtnSmall("...");
        btnAudio.setToolTipText("Seleccionar archivo de audio");
        btnAudio.addActionListener(e -> seleccionarAudio());
        p.add(crearFilaConBtn("Archivo de audio (WAV / AIFF / AU)", txtRutaAudio, btnAudio));
        p.add(Box.createVerticalStrut(8));

        txtNombre = crearCampo("Nombre de la canción");
        p.add(crearFila("Nombre", txtNombre));
        p.add(Box.createVerticalStrut(8));

        txtArtista = crearCampo("Artista");
        p.add(crearFila("Artista", txtArtista));
        p.add(Box.createVerticalStrut(8));

        txtDuracion = crearCampo("Detectado al seleccionar archivo");
        txtDuracion.setEditable(false);
        p.add(crearFila("Duración — detectada automáticamente", txtDuracion));
        p.add(Box.createVerticalStrut(8));

        txtRutaImagen = crearCampo("Opcional");
        txtRutaImagen.setEditable(false);
        JButton btnImg = crearBtnSmall("...");
        btnImg.setToolTipText("Seleccionar imagen del álbum");
        btnImg.addActionListener(e -> seleccionarImagen());
        p.add(crearFilaConBtn("Imagen del álbum", txtRutaImagen, btnImg));
        p.add(Box.createVerticalStrut(8));

        cmbGenero = crearCombo();
        p.add(crearFilaCombo("Género musical", cmbGenero));
        p.add(Box.createVerticalStrut(14));

        JPanel bRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bRow.setOpaque(false);
        bRow.setAlignmentX(LEFT_ALIGNMENT);
        JButton btnCancelar = crearBtnSec("Cancelar");
        JButton btnGuardar = crearBtnPri("Guardar");
        btnCancelar.addActionListener(e -> cerrarFormulario());
        btnGuardar.addActionListener(e -> guardarCancion());
        bRow.add(btnCancelar);
        bRow.add(btnGuardar);
        p.add(bRow);

        return p;
    }

    private JPanel construirCentro() {
        JPanel centro = new JPanel(new BorderLayout());
        centro.setBackground(BG_DARK);

        JPanel np = new JPanel();
        np.setLayout(new BoxLayout(np, BoxLayout.Y_AXIS));
        np.setBackground(BG_DARK);
        np.setBorder(BorderFactory.createEmptyBorder(48, 64, 32, 64));

        JPanel artwork = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_ELEVATED);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
            }
        };
        artwork.setOpaque(false);
        artwork.setMaximumSize(new Dimension(260, 260));
        artwork.setPreferredSize(new Dimension(260, 260));
        artwork.setAlignmentX(CENTER_ALIGNMENT);
        lblArtwork = new JLabel();
        lblArtwork.setHorizontalAlignment(SwingConstants.CENTER);
        lblArtwork.setVerticalAlignment(SwingConstants.CENTER);
        defaultArtwork();
        artwork.add(lblArtwork, BorderLayout.CENTER);
        np.add(artwork);
        np.add(Box.createVerticalStrut(28));

        lblNombre = centerLabel("Selecciona una canción", new Font("SansSerif", Font.BOLD, 24), TEXT_WHITE);
        lblArtista = centerLabel(" ", new Font("SansSerif", Font.PLAIN, 16), TEXT_GRAY);
        lblGenero = centerLabel(" ", new Font("SansSerif", Font.PLAIN, 13), TEXT_DIM);
        np.add(lblNombre);
        np.add(Box.createVerticalStrut(5));
        np.add(lblArtista);
        np.add(Box.createVerticalStrut(3));
        np.add(lblGenero);
        np.add(Box.createVerticalStrut(28));

        JPanel progRow = new JPanel(new BorderLayout(10, 0));
        progRow.setOpaque(false);
        progRow.setMaximumSize(new Dimension(420, 26));
        progRow.setAlignmentX(CENTER_ALIGNMENT);
        lblTiempoActual = dimLabel("0:00");
        lblTiempoTotal = dimLabel("0:00");
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(false);
        progressBar.setBorderPainted(false);
        progressBar.setUI(new SlimProgressUI());
        progRow.add(lblTiempoActual, BorderLayout.WEST);
        progRow.add(progressBar, BorderLayout.CENTER);
        progRow.add(lblTiempoTotal, BorderLayout.EAST);
        np.add(progRow);
        np.add(Box.createVerticalStrut(28));

        JPanel ctrl = new JPanel(new FlowLayout(FlowLayout.CENTER, 24, 0));
        ctrl.setOpaque(false);
        ctrl.setAlignmentX(CENTER_ALIGNMENT);
        btnStop = new CtrlButton(CtrlButton.STOP, 48, false);
        btnPlay = new CtrlButton(CtrlButton.PLAY, 64, true);
        btnPause = new CtrlButton(CtrlButton.PAUSE, 48, false);
        btnPlay.addActionListener(e -> accionPlay());
        btnPause.addActionListener(e -> accionPause());
        btnStop.addActionListener(e -> accionStop());
        ctrl.add(btnStop);
        ctrl.add(btnPlay);
        ctrl.add(btnPause);
        np.add(ctrl);

        lblEstado = new JLabel("DETENIDO", SwingConstants.CENTER);
        lblEstado.setFont(new Font("SansSerif", Font.BOLD, 11));
        lblEstado.setForeground(TEXT_DIM);
        lblEstado.setAlignmentX(CENTER_ALIGNMENT);
        np.add(Box.createVerticalStrut(14));
        np.add(lblEstado);

        centro.add(np, BorderLayout.CENTER);
        return centro;
    }

    private JPanel construirPie() {
        JPanel pie = new JPanel(new BorderLayout());
        pie.setBackground(new Color(9, 9, 9));
        pie.setBorder(new MatteBorder(1, 0, 0, 0, SEP));
        pie.setPreferredSize(new Dimension(0, 34));
        JLabel l = new JLabel("  \u266B  Reproductor de Música");
        l.setFont(new Font("SansSerif", Font.PLAIN, 12));
        l.setForeground(TEXT_DIM);
        pie.add(l, BorderLayout.WEST);
        return pie;
    }

    private JPanel crearFilaCancion(Cancion c, int idx) {
        boolean activa = (idx == idxSel);
        JPanel fila = new JPanel(new BorderLayout(12, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(activa ? BG_ELEVATED : getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        fila.setBackground(BG_BASE);
        fila.setOpaque(false);
        fila.setMaximumSize(new Dimension(310, 62));
        fila.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        fila.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel num = new JLabel(String.valueOf(idx + 1));
        num.setFont(new Font("SansSerif", Font.PLAIN, 13));
        num.setForeground(activa ? ACCENT : TEXT_DIM);
        num.setPreferredSize(new Dimension(22, 22));
        num.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);
        JLabel lNom = new JLabel(trunc(c.getNombre(), 24));
        lNom.setFont(new Font("SansSerif", Font.BOLD, 13));
        lNom.setForeground(activa ? ACCENT : TEXT_WHITE);
        JLabel lSub = new JLabel(trunc(c.getArtista(), 20) + " · " + c.getDuracionFormateada());
        lSub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lSub.setForeground(TEXT_GRAY);
        info.add(lNom);
        info.add(Box.createVerticalStrut(3));
        info.add(lSub);
        fila.add(num, BorderLayout.WEST);
        fila.add(info, BorderLayout.CENTER);

        fila.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!activa) {
                    fila.setBackground(BG_HOVER);
                }
                fila.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!activa) {
                    fila.setBackground(BG_BASE);
                }
                fila.repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                seleccionar(idx);
                if (e.getClickCount() == 2) {
                    accionPlay();
                }
            }
        });
        return fila;
    }

    private void accionPlay() {
        if (idxSel < 0 || idxSel >= canciones.size()) {
            feedback("Selecciona una cancion primero", false);
            return;
        }
        if (reproductor.estaPausado()) {
            reproductor.resume();
        } else {
            Cancion c = canciones.get(idxSel);
            reproductor.play(c);
            mostrarInfoCancion(c);
        }
    }

    private void accionPause() {
        if (reproductor.estaReproduciendo()) {
            reproductor.pause();
        } else if (reproductor.estaPausado()) {
            reproductor.resume();
        }
    }

    private void accionStop() {
        reproductor.stop();
    }

    private void seleccionar(int idx) {
        idxSel = idx;
        refreshLista();
        if (idx >= 0 && idx < canciones.size()) {
            mostrarInfoCancion(canciones.get(idx));
        }
    }

    private void toggleFormulario() {
        formVisible = !formVisible;
        panelForm.setVisible(formVisible);
        if (formVisible) {
            limpiarFormulario();
        }
        panelForm.revalidate();
        panelForm.repaint();
    }

    private void seleccionarAudio() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Seleccionar archivo de audio");
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Archivos de audio (*.wav, *.aiff, *.au, *.mp3)", "wav", "aiff", "au", "mp3"));
        fc.setAcceptAllFileFilterUsed(false);
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = fc.getSelectedFile();
        txtRutaAudio.setText(file.getAbsolutePath());

        if (txtNombre.getText().trim().isEmpty()) {
            String nom = file.getName();
            int dot = nom.lastIndexOf('.');
            txtNombre.setText(dot > 0 ? nom.substring(0, dot) : nom);
        }

        int dur = detectarDuracion(file);
        if (dur > 0) {
            txtDuracion.setText(String.valueOf(dur));
            txtDuracion.setForeground(ACCENT);
            feedback("Duración detectada: " + fmtTiempo(dur), true);
        } else {
            txtDuracion.setText("0");
            txtDuracion.setForeground(ERR);
            feedback("No se detectó duración — se usará 0", false);
        }
    }

    private void seleccionarImagen() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Seleccionar imagen del álbum");
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Imágenes (*.jpg, *.jpeg, *.png, *.gif)", "jpg", "jpeg", "png", "gif"));
        fc.setAcceptAllFileFilterUsed(false);
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File img = fc.getSelectedFile();
        txtRutaImagen.setText(img.getAbsolutePath());
        cargarArtwork(img.getAbsolutePath());
    }

    private int detectarDuracion(File file) {
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(file);
            AudioFormat fmt = ais.getFormat();
            long frames = ais.getFrameLength();
            ais.close();
            if (frames > 0 && fmt.getFrameRate() > 0) {
                return (int) Math.round(frames / fmt.getFrameRate());
            }
        } catch (Exception ignored) {
        }
        long bytes = file.length();
        if (bytes > 0) {
            return (int) (bytes / 16_000);
        }
        return 0;
    }

    private void guardarCancion() {
        String nombre = txtNombre.getText().trim();
        String artista = txtArtista.getText().trim();
        String durStr = txtDuracion.getText().trim();
        String rutaAud = txtRutaAudio.getText().trim();
        String rutaImg = txtRutaImagen.getText().trim();
        GeneroMusical genero = (GeneroMusical) cmbGenero.getSelectedItem();

        if (nombre.isEmpty()) {
            feedback("El nombre es requerido", false);
            return;
        }
        if (artista.isEmpty()) {
            feedback("El artista es requerido", false);
            return;
        }
        if (rutaAud.isEmpty()) {
            feedback("Selecciona un archivo de audio", false);
            return;
        }

        int duracion;
        try {
            duracion = Integer.parseInt(durStr);
        } catch (NumberFormatException e) {
            duracion = 0;
        }

        Cancion nueva = new Cancion(nombre, artista, duracion, rutaImg, rutaAud, genero);
        try {
            gestor.guardarCancion(nueva);
            canciones.add(nueva);
            refreshLista();
            cerrarFormulario();
            feedback("Agregada: " + nombre + " — " + artista, true);
        } catch (IOException e) {
            feedback("Error al guardar: " + e.getMessage(), false);
        }
    }

    private void eliminarSeleccionada() {
        if (idxSel < 0) {
            feedback("Selecciona una canción para eliminar", false);
            return;
        }
        Cancion c = canciones.get(idxSel);
        if (!reproductor.estaDetenido()) {
            Cancion actual = reproductor.getCancionActual();
            if (actual != null && actual.equals(c)) {
                reproductor.stop();
            }
        }
        gestor.eliminarCancion(idxSel);
        canciones.remove(idxSel);
        idxSel = -1;
        refreshLista();
        resetearInfoCancion();
        feedback("Canción eliminada", true);
    }

    private void cargarCanciones() {
        canciones.clear();
        canciones.addAll(gestor.listarCanciones());
        refreshLista();
    }

    private void refreshLista() {
        listaPanel.removeAll();
        if (canciones.isEmpty()) {
            JLabel lv = new JLabel("No hay canciones", SwingConstants.CENTER);
            lv.setFont(new Font("SansSerif", Font.ITALIC, 13));
            lv.setForeground(TEXT_DIM);
            lv.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));
            lv.setAlignmentX(CENTER_ALIGNMENT);
            listaPanel.add(lv);
        } else {
            for (int i = 0; i < canciones.size(); i++) {
                listaPanel.add(crearFilaCancion(canciones.get(i), i));
                if (i < canciones.size() - 1) {
                    JSeparator sep = new JSeparator();
                    sep.setForeground(SEP);
                    sep.setBackground(SEP);
                    sep.setMaximumSize(new Dimension(310, 1));
                    listaPanel.add(sep);
                }
            }
        }
        listaPanel.revalidate();
        listaPanel.repaint();
    }

    private void cerrarFormulario() {
        formVisible = false;
        panelForm.setVisible(false);
        limpiarFormulario();
    }

    private void limpiarFormulario() {
        txtNombre.setText("");
        txtArtista.setText("");
        txtDuracion.setText("");
        txtDuracion.setForeground(TEXT_DIM);
        txtRutaAudio.setText("");
        txtRutaImagen.setText("");
        cmbGenero.setSelectedIndex(0);
    }

    private void mostrarInfoCancion(Cancion c) {
        lblNombre.setText(c.getNombre());
        lblArtista.setText(c.getArtista());
        lblGenero.setText(c.getGenero().getDescripcion());
        lblTiempoTotal.setText(c.getDuracionFormateada());
        progressBar.setValue(0);
        lblTiempoActual.setText("0:00");
        cargarArtwork(c.getRutaImagen());
    }

    private void resetearInfoCancion() {
        lblNombre.setText("Selecciona una canción");
        lblArtista.setText(" ");
        lblGenero.setText(" ");
        lblTiempoActual.setText("0:00");
        lblTiempoTotal.setText("0:00");
        progressBar.setValue(0);
        defaultArtwork();
    }

    private void cargarArtwork(String ruta) {
        if (ruta == null || ruta.trim().isEmpty()) {
            defaultArtwork();
            return;
        }
        try {
            File f = new File(ruta.trim());
            if (f.exists()) {
                BufferedImage img = ImageIO.read(f);
                if (img != null) {
                    lblArtwork.setIcon(new ImageIcon(img.getScaledInstance(260, 260, Image.SCALE_SMOOTH)));
                    lblArtwork.setText("");
                    return;
                }
            }
        } catch (IOException ignored) {
        }
        defaultArtwork();
    }

    private void defaultArtwork() {
        lblArtwork.setIcon(null);
        lblArtwork.setText("\u266B");
        lblArtwork.setFont(new Font("SansSerif", Font.PLAIN, 80));
        lblArtwork.setForeground(TEXT_DIM);
    }

    @Override
    public void onEstadoChanged(EstadoReproductor estado) {
        SwingUtilities.invokeLater(() -> {
            lblEstado.setText(estado.getDescripcion().toUpperCase());
            switch (estado) {
                case PLAYING:
                    lblEstado.setForeground(ACCENT);
                    break;
                case PAUSED:
                    lblEstado.setForeground(new Color(255, 200, 60));
                    break;
                case STOPPED:
                    lblEstado.setForeground(TEXT_DIM);
                    progressBar.setValue(0);
                    lblTiempoActual.setText("0:00");
                    break;
            }
        });
    }

    @Override
    public void onProgresoChanged(int tiempoActual, int duracionTotal) {
        SwingUtilities.invokeLater(() -> {
            if (duracionTotal > 0) {
                progressBar.setValue((int) ((tiempoActual * 100.0) / duracionTotal));
            }
            lblTiempoActual.setText(fmtTiempo(tiempoActual));
        });
    }

    @Override
    public void onCancionFinalizada(Cancion cancion) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(100);
            feedback("Finalizada: " + cancion.getNombre(), true);
        });
    }

    private void feedback(String msg, boolean ok) {
        lblFeedback.setText(msg);
        lblFeedback.setForeground(ok ? ACCENT : ERR);
        if (feedbackTimer != null) {
            feedbackTimer.stop();
        }
        feedbackTimer = new Timer(3500, e -> lblFeedback.setText(" "));
        feedbackTimer.setRepeats(false);
        feedbackTimer.start();
    }

    private JLabel centerLabel(String txt, Font f, Color c) {
        JLabel l = new JLabel(txt, SwingConstants.CENTER);
        l.setFont(f);
        l.setForeground(c);
        l.setAlignmentX(CENTER_ALIGNMENT);
        return l;
    }

    private JLabel dimLabel(String txt) {
        JLabel l = new JLabel(txt);
        l.setFont(new Font("SansSerif", Font.PLAIN, 12));
        l.setForeground(TEXT_DIM);
        return l;
    }

    private JTextField crearCampo(String hint) {
        JTextField tf = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(TEXT_DIM);
                    g2.setFont(getFont().deriveFont(Font.ITALIC));
                    int y = (getHeight() + g2.getFontMetrics().getAscent() - g2.getFontMetrics().getDescent()) / 2;
                    g2.drawString(hint, 10, y);
                    g2.dispose();
                }
            }
        };
        tf.setBackground(BG_ELEVATED);
        tf.setForeground(TEXT_WHITE);
        tf.setCaretColor(ACCENT);
        tf.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(60, 60, 60), 1, true),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        tf.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(new LineBorder(ACCENT, 1, true), BorderFactory.createEmptyBorder(5, 8, 5, 8)));
            }

            @Override
            public void focusLost(FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(60, 60, 60), 1, true), BorderFactory.createEmptyBorder(5, 8, 5, 8)));
            }
        });
        return tf;
    }

    private JComboBox<GeneroMusical> crearCombo() {
        JComboBox<GeneroMusical> cb = new JComboBox<>(GeneroMusical.values());
        cb.setBackground(BG_ELEVATED);
        cb.setForeground(TEXT_WHITE);
        cb.setFont(new Font("SansSerif", Font.PLAIN, 13));
        cb.setBorder(new LineBorder(new Color(60, 60, 60), 1, true));
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                super.getListCellRendererComponent(l, v, i, s, f);
                setBackground(s ? BG_HOVER : BG_ELEVATED);
                setForeground(TEXT_WHITE);
                setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                if (v instanceof GeneroMusical) {
                    setText(((GeneroMusical) v).getDescripcion());
                }
                return this;
            }
        });
        return cb;
    }

    private JPanel crearFila(String lbl, JTextField tf) {
        JPanel p = new JPanel(new BorderLayout(0, 3));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(278, 56));
        p.setAlignmentX(LEFT_ALIGNMENT);
        JLabel l = new JLabel(lbl);
        l.setFont(new Font("SansSerif", Font.PLAIN, 11));
        l.setForeground(TEXT_DIM);
        p.add(l, BorderLayout.NORTH);
        p.add(tf, BorderLayout.CENTER);
        return p;
    }

    private JPanel crearFilaConBtn(String lbl, JTextField tf, JButton btn) {
        JPanel outer = new JPanel(new BorderLayout(0, 3));
        outer.setOpaque(false);
        outer.setMaximumSize(new Dimension(278, 56));
        outer.setAlignmentX(LEFT_ALIGNMENT);
        JLabel l = new JLabel(lbl);
        l.setFont(new Font("SansSerif", Font.PLAIN, 11));
        l.setForeground(TEXT_DIM);
        outer.add(l, BorderLayout.NORTH);
        JPanel row = new JPanel(new BorderLayout(4, 0));
        row.setOpaque(false);
        row.add(tf, BorderLayout.CENTER);
        row.add(btn, BorderLayout.EAST);
        outer.add(row, BorderLayout.CENTER);
        return outer;
    }

    private JPanel crearFilaCombo(String lbl, JComboBox<?> cb) {
        JPanel p = new JPanel(new BorderLayout(0, 3));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(278, 56));
        p.setAlignmentX(LEFT_ALIGNMENT);
        JLabel l = new JLabel(lbl);
        l.setFont(new Font("SansSerif", Font.PLAIN, 11));
        l.setForeground(TEXT_DIM);
        p.add(l, BorderLayout.NORTH);
        p.add(cb, BorderLayout.CENTER);
        return p;
    }

    private JButton crearBtnSmall(String txt) {
        JButton b = new JButton(txt) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? BG_HOVER : BG_ELEVATED);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.setColor(TEXT_GRAY);
                g2.setFont(new Font("SansSerif", Font.BOLD, 12));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        b.setPreferredSize(new Dimension(34, 30));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton crearBtnPri(String txt) {
        JButton b = new JButton(txt) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? ACCENT_DIM : ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.setColor(BG_BASE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 13));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        b.setPreferredSize(new Dimension(88, 33));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton crearBtnSec(String txt) {
        JButton b = new JButton(txt) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? BG_HOVER : BG_ELEVATED);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.setColor(TEXT_GRAY);
                g2.setFont(new Font("SansSerif", Font.BOLD, 13));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        b.setPreferredSize(new Dimension(88, 33));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private String fmtTiempo(int s) {
        return (s / 60) + ":" + (s % 60 < 10 ? "0" : "") + (s % 60);
    }

    private String trunc(String s, int n) {
        return s.length() > n ? s.substring(0, n - 1) + "\u2026" : s;
    }

    static class CtrlButton extends JButton {

        static final int PLAY = 0, PAUSE = 1, STOP = 2;
        private final int tipo, sz;
        private final boolean primario;

        CtrlButton(int tipo, int sz, boolean primario) {
            this.tipo = tipo;
            this.sz = sz;
            this.primario = primario;
            setPreferredSize(new Dimension(sz, sz));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color bgC = primario
                    ? (getModel().isRollover() ? ACCENT_DIM : ACCENT)
                    : (getModel().isRollover() ? BG_HOVER : BG_ELEVATED);
            g2.setColor(bgC);
            g2.fillOval(0, 0, sz, sz);

            g2.setColor(primario ? BG_BASE : TEXT_WHITE);
            int cx = sz / 2, cy = sz / 2, arm = sz / 5;

            switch (tipo) {
                case PLAY: {
                    int[] px = {cx - arm + 2, cx + arm + 2, cx - arm + 2};
                    int[] py = {cy - arm, cy, cy + arm};
                    g2.fillPolygon(px, py, 3);
                    break;
                }
                case PAUSE: {
                    int bw = Math.max(3, arm / 2), bh = arm + arm / 2, gap = Math.max(2, arm / 3);
                    g2.fillRoundRect(cx - gap - bw, cy - bh, bw, bh * 2, 3, 3);
                    g2.fillRoundRect(cx + gap, cy - bh, bw, bh * 2, 3, 3);
                    break;
                }
                case STOP: {
                    int s2 = arm + 2;
                    g2.fillRoundRect(cx - s2, cy - s2, s2 * 2, s2 * 2, 4, 4);
                    break;
                }
            }
            g2.dispose();
        }
    }

    static class IconBtn extends JButton {

        private final boolean plus;

        IconBtn(boolean plus, String tooltip) {
            this.plus = plus;
            setToolTipText(tooltip);
            setPreferredSize(new Dimension(30, 30));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getModel().isRollover() ? BG_HOVER : BG_ELEVATED);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            g2.setColor(TEXT_GRAY);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int cx = getWidth() / 2, cy = getHeight() / 2, arm = 6;
            g2.drawLine(cx - arm, cy, cx + arm, cy);
            if (plus) {
                g2.drawLine(cx, cy - arm, cx, cy + arm);
            }
            g2.dispose();
        }
    }

    static class DarkScrollUI extends javax.swing.plaf.basic.BasicScrollBarUI {

        @Override
        protected void configureScrollBarColors() {
            thumbColor = BG_ELEVATED;
            trackColor = BG_BASE;
        }

        @Override
        protected JButton createDecreaseButton(int o) {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            return b;
        }

        @Override
        protected JButton createIncreaseButton(int o) {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            return b;
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumbColor);
            g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 6, 6);
            g2.dispose();
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
            g.setColor(trackColor);
            g.fillRect(r.x, r.y, r.width, r.height);
        }
    }

    static class SlimProgressUI extends javax.swing.plaf.basic.BasicProgressBarUI {

        @Override
        public void paint(Graphics g, JComponent c) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            JProgressBar pb = (JProgressBar) c;
            int w = pb.getWidth(), h = pb.getHeight();
            g2.setColor(TRACK_BG);
            g2.fillRoundRect(0, 0, w, h, h, h);
            int filled = (int) (w * pb.getPercentComplete());
            if (filled > 0) {
                g2.setColor(ACCENT);
                g2.fillRoundRect(0, 0, filled, h, h, h);
            }
            g2.dispose();
        }

        @Override
        public Dimension getPreferredSize(JComponent c) {
            return new Dimension(340, 4);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }
}
