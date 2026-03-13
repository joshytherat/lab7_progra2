/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lab7_binarios;

/**
 *
 * @author janinadiaz
 */
import java.util.Calendar;
import java.util.ArrayList;

public class ReproductorMusica {

    private Cancion cancionActual;
    private EstadoReproductor estado;
    private int tiempoActualSegundos;
    private Thread hiloReproduccion;
    private volatile boolean ejecutando;
    private volatile boolean pausado;

    private ArrayList<Cancion> historialReproduccion;
    private Calendar tiempoInicio;
    private ArrayList<ReproductorListener> listeners;

    public ReproductorMusica() {
        this.estado = EstadoReproductor.STOPPED;
        this.tiempoActualSegundos = 0;
        this.cancionActual = null;
        this.ejecutando = false;
        this.pausado = false;
        this.historialReproduccion = new ArrayList<>();
        this.listeners = new ArrayList<>();
    }

    public synchronized void play(Cancion cancion) {
        try {
            if (estado == EstadoReproductor.PLAYING || estado == EstadoReproductor.PAUSED) {
                stop();
            }
            if (cancion == null) {
                throw new IllegalArgumentException("La canción no puede ser null");
            }

            this.cancionActual = cancion;
            this.tiempoActualSegundos = 0;
            this.estado = EstadoReproductor.PLAYING;
            this.pausado = false;
            agregarAlHistorial(cancion);
            cancion.marcarReproducida();

            tiempoInicio = Calendar.getInstance();
            iniciarHiloReproduccion();
            notificarCambioEstado();

            System.out.println("▶ Reproduciendo: " + cancion.getNombre() + " - " + cancion.getArtista());

        } catch (IllegalArgumentException e) {
            System.err.println("Error al reproducir: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error inesperado al reproducir: " + e.getMessage());
            estado = EstadoReproductor.STOPPED;
        }
    }

    public synchronized void resume() {
        try {
            if (estado == EstadoReproductor.PAUSED && cancionActual != null) {
                estado = EstadoReproductor.PLAYING;
                pausado = false;

                // Actualizar tiempo de inicio
                tiempoInicio = Calendar.getInstance();

                notificarCambioEstado();
                System.out.println(" Reanudando: " + cancionActual.getNombre());
            }
        } catch (Exception e) {
            System.err.println("Error al reanudar: " + e.getMessage());
        }
    }

    /**
     * Pausa la reproducción actual
     */
    public synchronized void pause() {
        try {
            if (estado == EstadoReproductor.PLAYING) {
                estado = EstadoReproductor.PAUSED;
                pausado = true;

                notificarCambioEstado();
                System.out.println("Pausado: " + cancionActual.getNombre()
                        + " [" + formatearTiempo(tiempoActualSegundos) + "]");
            }
        } catch (Exception e) {
            System.err.println("Error al pausar: " + e.getMessage());
        }
    }

    public synchronized void stop() {
        try {
            if (estado != EstadoReproductor.STOPPED) {
                ejecutando = false;
                pausado = false;

                if (hiloReproduccion != null && hiloReproduccion.isAlive()) {
                    try {
                        hiloReproduccion.join(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

                estado = EstadoReproductor.STOPPED;
                tiempoActualSegundos = 0;

                String nombreCancion;

                if (cancionActual != null) {
                    nombreCancion = cancionActual.getNombre();
                } else {
                    nombreCancion = "Ninguna";
                }

                notificarCambioEstado();
                System.out.println("Detenido: " + nombreCancion);
            }
        } catch (Exception e) {
            System.err.println("Error al detener: " + e.getMessage());
            estado = EstadoReproductor.STOPPED;
        }
    }

    
    private void iniciarHiloReproduccion() {
        ejecutando = true;
        hiloReproduccion=new Thread(() -> {
            try {
                while (ejecutando && tiempoActualSegundos < cancionActual.getDuracionSegundos()) {
                    while (pausado && ejecutando) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }

                    if (!ejecutando) {
                        break;
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }

                    if (!pausado && ejecutando) {
                        tiempoActualSegundos++;
                        notificarProgreso(tiempoActualSegundos, cancionActual.getDuracionSegundos());

                        if(tiempoActualSegundos%5 == 0) {
                            System.out.println("Tiempo: " + formatearTiempo(tiempoActualSegundos) + " / " + cancionActual.getDuracionFormateada());
                        }
                    }
                }

                if (ejecutando && tiempoActualSegundos >= cancionActual.getDuracionSegundos()) {
                    System.out.println("✓ Canción finalizada: " + cancionActual.getNombre());
                    notificarCancionFinalizada();
                    synchronized (ReproductorMusica.this) {
                        estado = EstadoReproductor.STOPPED;
                        tiempoActualSegundos = 0;
                    }
                }

            } catch (Exception e) {
                System.err.println("Error en hilo de reproducción: " + e.getMessage());
                synchronized (ReproductorMusica.this) {
                    estado = EstadoReproductor.STOPPED;
                }
            }
        });

        hiloReproduccion.setDaemon(true);
        hiloReproduccion.start();
    }

    private void agregarAlHistorial(Cancion cancion) {
        if (!historialReproduccion.contains(cancion)) {
            historialReproduccion.add(cancion);
        }
    }

    private String formatearTiempo(int segundos) {
        int minutos = segundos / 60;
        int segs =segundos%60;

        if (segs < 10){
            return minutos + ":0" + segs;
        }

        return minutos + ":" + segs;
    }

    public double getPorcentajeReproduccion() {
        if (cancionActual == null || cancionActual.getDuracionSegundos() == 0) {
            return 0.0;
        }
        return (tiempoActualSegundos * 100.0) / cancionActual.getDuracionSegundos();
    }

    public long getTiempoTranscurridoMilisegundos() {
        if (tiempoInicio == null) {
            return 0;
        }
        Calendar ahora = Calendar.getInstance();
        return ahora.getTimeInMillis() - tiempoInicio.getTimeInMillis();
    }

    void agregarListener(ReproductorListener listener){
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void eliminarListener(ReproductorListener listener) {
        listeners.remove(listener);
    }

    private void notificarCambioEstado() {
        for (ReproductorListener listener : listeners) {
            try {
                listener.onEstadoChanged(estado);
            } catch (Exception e) {
                System.err.println("Error en listener: " + e.getMessage());
            }
        }
    }

    private void notificarProgreso(int tiempoActual, int duracionTotal) {
        for(ReproductorListener listener : listeners) {
            try{
                listener.onProgresoChanged(tiempoActual, duracionTotal);
            } catch (Exception e) {
                System.err.println("Error en listener: " + e.getMessage());
            }
        }
    }

    private void notificarCancionFinalizada() {
        for(ReproductorListener listener : listeners) {
            try{
                listener.onCancionFinalizada(cancionActual);
            } catch (Exception e) {
                System.err.println("Error en listener: " + e.getMessage());
            }
        }
    }

    public Cancion getCancionActual() {
        return cancionActual;
    }

    public EstadoReproductor getEstado() {
        return estado;
    }

    public int getTiempoActualSegundos() {
        return tiempoActualSegundos;
    }

    public ArrayList<Cancion> getHistorialReproduccion() {
        return new ArrayList<>(historialReproduccion);
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

    public String getEstadoInfo() {
        String info ="";
        info = info + "Estado: "+ estado + "\n";

        if (cancionActual != null) {
            info = info + "Cancion: "+cancionActual.getNombre() + "\n";
            info = info + "Artista: " + cancionActual.getArtista() + "\n";
            info = info + "Tiempo: " + formatearTiempo(tiempoActualSegundos)
                    + " / " + cancionActual.getDuracionFormateada() + "\n";

            double porcentaje = getPorcentajeReproduccion();
            info=info + "Progreso: "+((int) porcentaje) + "%";
        } else {
            info = info+"Sin cancion cargada";
        }

        return info;
    }

    public interface ReproductorListener {

        void onEstadoChanged(EstadoReproductor nuevoEstado);

        void onProgresoChanged(int tiempoActual, int duracionTotal);

        void onCancionFinalizada(Cancion cancion);
    }
}
