/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lab7_binarios;

/**
 *
 * @author janinadiaz
 */
import javax.sound.sampled.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;

import javazoom.jl.player.Player;

public class ReproductorMusica {

    private Cancion cancionActual;
    private EstadoReproductor estado;
    private volatile boolean ejecutando;
    private volatile boolean pausado;

    private Clip audioClip;

    private Player mp3Player;
    private Thread hiloMp3;
    private File archivoMp3;
    private long mp3BytePosicion;
    private long mp3TotalBytes;
    private long mp3InicioMs;
    private int mp3OffsetSegundos;

    private Thread hiloProgreso;
    private Calendar tiempoInicio;

    private final ArrayList<Cancion> historial = new ArrayList<>();
    private final ArrayList<ReproductorListener> listeners = new ArrayList<>();

    public ReproductorMusica() {
        this.estado = EstadoReproductor.STOPPED;
        this.ejecutando = false;
        this.pausado = false;
    }

    public synchronized void play(Cancion cancion) {
        if (estado != EstadoReproductor.STOPPED) {
            stop();
        }
        if (cancion == null) {
            throw new IllegalArgumentException("Canción null");
        }

        cancionActual = cancion;
        mp3BytePosicion = 0;
        mp3OffsetSegundos = 0;
        pausado = false;
        ejecutando = true;
        tiempoInicio = Calendar.getInstance();

        agregarAlHistorial(cancion);
        cancion.marcarReproducida();

        String ruta = cancion.getRutaAudio();
        boolean esMp3 = ruta != null && ruta.toLowerCase().endsWith(".mp3");

        if (esMp3) {
            archivoMp3 = new File(ruta.trim());
            mp3TotalBytes = archivoMp3.length();
            iniciarMp3(0);
        } else {
            boolean ok = abrirClip(ruta);
            if (ok) {
                audioClip.setMicrosecondPosition(0);
                audioClip.start();
            }
        }

        estado = EstadoReproductor.PLAYING;
        iniciarHiloProgreso(esMp3);
        notificarEstado();
        System.out.println("▶ Reproduciendo: " + cancion.getNombre());
    }

    public synchronized void pause() {
        if (estado != EstadoReproductor.PLAYING) {
            return;
        }
        pausado = true;
        estado = EstadoReproductor.PAUSED;

        if (mp3Player != null) {

            long transcurridoMs = System.currentTimeMillis() - mp3InicioMs;
            int durMs = cancionActual.getDuracionSegundos() * 1000;
            if (durMs > 0) {
                double fraccion = (double) transcurridoMs / durMs;
                long bytesDelta = (long) (mp3TotalBytes * fraccion);
                mp3BytePosicion += bytesDelta;
                mp3BytePosicion = Math.min(mp3BytePosicion, mp3TotalBytes);
            }
            mp3OffsetSegundos += (int) (transcurridoMs / 1000);
            mp3Player.close();
            mp3Player = null;
        }

        if (audioClip != null && audioClip.isRunning()) {
            audioClip.stop();
        }

        notificarEstado();
        System.out.println("Pausado: " + cancionActual.getNombre());
    }

    public synchronized void resume() {
        if (estado != EstadoReproductor.PAUSED) {
            return;
        }
        pausado = false;
        estado = EstadoReproductor.PLAYING;

        String ruta = cancionActual.getRutaAudio();
        boolean esMp3 = ruta != null && ruta.toLowerCase().endsWith(".mp3");

        if (esMp3) {
            iniciarMp3(mp3BytePosicion);
        } else if (audioClip != null) {
            audioClip.start();
        }

        tiempoInicio = Calendar.getInstance();
        notificarEstado();
        System.out.println("Reanudando: " + cancionActual.getNombre());
    }

    public synchronized void stop() {
        if (estado == EstadoReproductor.STOPPED) {
            return;
        }
        ejecutando = false;
        pausado = false;

        if (mp3Player != null) {
            mp3Player.close();
            mp3Player = null;
        }
        if (audioClip != null) {
            try {
                audioClip.stop();
                audioClip.close();
            } catch (Exception ignored) {
            }
            audioClip = null;
        }
        if (hiloMp3 != null && hiloMp3.isAlive()) {
            try {
                hiloMp3.join(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (hiloProgreso != null && hiloProgreso.isAlive()) {
            try {
                hiloProgreso.join(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        mp3BytePosicion = 0;
        mp3OffsetSegundos = 0;
        estado = EstadoReproductor.STOPPED;
        notificarEstado();
        System.out.println("⏹ Detenido");
    }

    private void iniciarMp3(long desdeBytes) {
        mp3InicioMs = System.currentTimeMillis();
        hiloMp3 = new Thread(() -> {
            try {
                FileInputStream fis = new FileInputStream(archivoMp3);
                BufferedInputStream bis = new BufferedInputStream(fis);
                if (desdeBytes > 0) {
                    long skipped = bis.skip(desdeBytes);
                    System.out.println("Retomando desde ~" + skipped + " bytes");
                }
                Player p = new Player(bis);
                synchronized (ReproductorMusica.this) {
                    mp3Player = p;
                }
                p.play();
                boolean termino = false;
                synchronized (ReproductorMusica.this) {
                    termino = ejecutando && !pausado;
                }
                if (termino) {
                    finalizarCancion();
                }

            } catch (javazoom.jl.decoder.JavaLayerException | IOException e) {
                if (ejecutando) {
                    System.err.println("Error MP3: " + e.getMessage());
                }
            }
        });
        hiloMp3.setDaemon(true);
        hiloMp3.start();
    }

    
    private boolean abrirClip(String ruta) {
        if (ruta == null || ruta.trim().isEmpty()) {
            return false;
        }
        File file = new File(ruta.trim());
        if (!file.exists()) {
            System.err.println("Archivo no encontrado: " + ruta);
            return false;
        }
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(file);
            AudioFormat fmt = ais.getFormat();
            AudioFormat pcm = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    fmt.getSampleRate(), 16, fmt.getChannels(),
                    fmt.getChannels() * 2, fmt.getSampleRate(), false);
            AudioInputStream aisPcm;
            try {
                aisPcm = AudioSystem.getAudioInputStream(pcm, ais);
            } catch (Exception e) {
                aisPcm = ais;
            }
            audioClip = AudioSystem.getClip();
            audioClip.open(aisPcm);
            audioClip.addLineListener(ev -> {
                if (ev.getType() == LineEvent.Type.STOP && ejecutando && !pausado) {
                    if (audioClip.getMicrosecondPosition() >= audioClip.getMicrosecondLength() - 50_000) {
                        finalizarCancion();
                    }
                }
            });
            return true;
        } catch (Exception e) {
            System.err.println("No se pudo abrir WAV: " + e.getMessage());
            audioClip = null;
            return false;
        }
    }

    private void iniciarHiloProgreso(boolean esMp3) {
        int duracion = cancionActual.getDuracionSegundos();
        hiloProgreso = new Thread(() -> {
            while (ejecutando) {
                if (!pausado) {
                    int segundos;
                    if (esMp3) {

                        long elapsed = System.currentTimeMillis() - mp3InicioMs;
                        segundos = mp3OffsetSegundos + (int) (elapsed / 1000);
                    } else if (audioClip != null) {
                        segundos = (int) (audioClip.getMicrosecondPosition() / 1_000_000L);
                    } else {
                        long elapsed = Calendar.getInstance().getTimeInMillis()
                                - tiempoInicio.getTimeInMillis();
                        segundos = (int) (elapsed / 1000);
                    }
                    segundos = Math.min(segundos, duracion);
                    notificarProgreso(segundos, duracion);
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        hiloProgreso.setDaemon(true);
        hiloProgreso.start();
    }

    private void finalizarCancion() {
        synchronized (this) {
            if (!ejecutando) {
                return;
            }
            ejecutando = false;
            estado = EstadoReproductor.STOPPED;
        }
        System.out.println("✓ Finalizada: " + cancionActual.getNombre());
        notificarFinalizada();
        notificarEstado();
    }

    private void agregarAlHistorial(Cancion c) {
        if (!historial.contains(c)) {
            historial.add(c);
        }
    }

    public void agregarListener(ReproductorListener l) {
        if (l != null && !listeners.contains(l)) {
            listeners.add(l);
        }
    }

    public void eliminarListener(ReproductorListener l) {
        listeners.remove(l);
    }

    private void notificarEstado() {
        for (ReproductorListener l : listeners) try {
            l.onEstadoChanged(estado);
        } catch (Exception ignored) {
        }
    }

    private void notificarProgreso(int actual, int total) {
        for (ReproductorListener l : listeners) try {
            l.onProgresoChanged(actual, total);
        } catch (Exception ignored) {
        }
    }

    private void notificarFinalizada() {
        for (ReproductorListener l : listeners) try {
            l.onCancionFinalizada(cancionActual);
        } catch (Exception ignored) {
        }
    }

    public Cancion getCancionActual() {
        return cancionActual;
    }

    public EstadoReproductor getEstado() {
        return estado;
    }

    public boolean estaReproduciendo() {
        return estado == EstadoReproductor.PLAYING;
    }

    public boolean estaPausado() {
        return estado == EstadoReproductor.PAUSED;
    }

    public boolean estaDetenido() {
        return estado == EstadoReproductor.STOPPED;
    }

    public int getTiempoActualSegundos() {
        if (audioClip != null) {
            return (int) (audioClip.getMicrosecondPosition() / 1_000_000L);
        }
        return mp3OffsetSegundos + (int) ((System.currentTimeMillis() - mp3InicioMs) / 1000);
    }

    public ArrayList<Cancion> getHistorialReproduccion() {
        return new ArrayList<>(historial);
    }

    public double getPorcentajeReproduccion() {
        if (cancionActual == null || cancionActual.getDuracionSegundos() == 0) {
            return 0;
        }
        return (getTiempoActualSegundos() * 100.0) / cancionActual.getDuracionSegundos();
    }

    public interface ReproductorListener {

        void onEstadoChanged(EstadoReproductor nuevoEstado);

        void onProgresoChanged(int tiempoActual, int duracionTotal);

        void onCancionFinalizada(Cancion cancion);
    }
}
