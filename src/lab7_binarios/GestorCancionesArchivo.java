/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lab7_binarios;

/**
 *
 * @author janinadiaz
 */
import java.io.RandomAccessFile;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class GestorCancionesArchivo {

    private static final int TAMANIO_NOMBRE = 100;
    private static final int TAMANIO_ARTISTA = 100;
    private static final int TAMANIO_GENERO = 30;
    private static final int TAMANIO_RUTA_IMAGEN = 200;
    private static final int TAMANIO_RUTA_AUDIO = 200;
    private static final int TAMANIO_REGISTRO = (TAMANIO_NOMBRE * 2) + (TAMANIO_ARTISTA * 2) + 4 + (TAMANIO_GENERO * 2) + (TAMANIO_RUTA_IMAGEN * 2) + (TAMANIO_RUTA_AUDIO * 2) + 8 + 1;

    private String rutaArchivo;

    public GestorCancionesArchivo(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
        try {
            File archivo = new File(rutaArchivo);
            if (!archivo.exists()) {
                archivo.getParentFile().mkdirs();
                archivo.createNewFile();
            }
        } catch (IOException e) {
            System.err.println("Error al crear archivo: " + e.getMessage());
        }
    }

    public int guardarCancion(Cancion cancion) throws IOException {
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(rutaArchivo, "rw");

            int posicion = contarCanciones();
            long byteOffset = (long) posicion * TAMANIO_REGISTRO;

            raf.seek(byteOffset);
            escribirCancion(raf, cancion);
            System.out.println("Canción guardada en posición: " + posicion);
            return posicion;

        } catch (IOException e) {
            System.err.println("Error al guardar canción: " + e.getMessage());
            throw e;
        } finally {
            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException e) {
                    System.err.println("Error al cerrar archivo: " + e.getMessage());
                }
            }
        }
    }

    public Cancion leerCancion(int posicion) throws IOException {
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(rutaArchivo, "r");
            long tamanioArchivo = raf.length();
            long byteOffset = (long) posicion * TAMANIO_REGISTRO;

            if (byteOffset >= tamanioArchivo) {
                System.out.println("Posición fuera de rango");
                return null;
            }
            raf.seek(byteOffset);
            return leerCancionDesdeArchivo(raf);

        } catch (IOException e) {
            System.err.println("Error al leer canción: " + e.getMessage());
            throw e;
        } finally {
            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException e) {
                    System.err.println("Error al cerrar archivo: " + e.getMessage());
                }
            }
        }

    }

    public ArrayList<Cancion> listarCanciones() {

    ArrayList<Cancion> canciones = new ArrayList<>();

    try (RandomAccessFile raf = new RandomAccessFile(rutaArchivo, "r")) {

        int total = (int) (raf.length() / TAMANIO_REGISTRO);

        for (int i = 0; i < total; i++) {

            raf.seek(i * TAMANIO_REGISTRO);

            Cancion c = leerCancionDesdeArchivo(raf);

            if (c != null) {
                canciones.add(c);
            }
        }

    } catch (IOException e) {
        System.out.println("Error listando canciones: " + e.getMessage());
    }

    return canciones;
}

    public boolean eliminarCancion(int posicion) {
        RandomAccessFile raf = null;

        try {
            raf = new RandomAccessFile(rutaArchivo, "rw");

            long tamanioArchivo = raf.length();
            long byteOffset = (long) posicion * TAMANIO_REGISTRO;

            if (byteOffset >= tamanioArchivo) {
                System.out.println("Posición fuera de rango");
                return false;
            }
            raf.seek(byteOffset + TAMANIO_REGISTRO - 1);

            raf.writeBoolean(false);

            System.out.println("Canción en posición " + posicion + " eliminada");
            return true;

        } catch (IOException e) {
            System.err.println("Error al eliminar canción: " + e.getMessage());
            return false;
        } finally {
            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException e) {
                    System.err.println("Error al cerrar archivo: " + e.getMessage());
                }
            }
        }
    }

    public boolean actualizarCancion(int posicion, Cancion cancion) {
        RandomAccessFile raf = null;

        try {
            raf = new RandomAccessFile(rutaArchivo, "rw");

            long tamanioArchivo = raf.length();
            long byteOffset = (long) posicion * TAMANIO_REGISTRO;

            if (byteOffset >= tamanioArchivo) {
                System.out.println("Posición fuera de rango");
                return false;
            }
            raf.seek(byteOffset);

            escribirCancion(raf, cancion);

            System.out.println("Canción en posición " + posicion + " actualizada");
            return true;

        } catch (IOException e) {
            System.err.println("Error al actualizar canción: " + e.getMessage());
            return false;
        } finally {
            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException e) {
                    System.err.println("Error al cerrar archivo: " + e.getMessage());
                }
            }
        }
    }

    public int contarCanciones() {
        try (RandomAccessFile raf = new RandomAccessFile(rutaArchivo, "r")) {
            return (int) (raf.length() / TAMANIO_REGISTRO);
        } catch (IOException e) {
            System.err.println("Error al contar canciones: " + e.getMessage());
            return 0;
        }
    }

    public void limpiarArchivo() {
        try (RandomAccessFile raf = new RandomAccessFile(rutaArchivo, "rw")) {
            raf.setLength(0);
            System.out.println("Archivo limpiado");
        } catch (IOException e) {
            System.err.println("Error al limpiar archivo: " + e.getMessage());
        }
    }

    private void escribirCancion(RandomAccessFile raf, Cancion cancion) throws IOException {

        escribirStringFijo(raf, cancion.getNombre(), TAMANIO_NOMBRE);
        escribirStringFijo(raf, cancion.getArtista(), TAMANIO_ARTISTA);

        raf.writeInt(cancion.getDuracionSegundos());

        escribirStringFijo(raf, cancion.getGenero().name(), TAMANIO_GENERO);

        escribirStringFijo(raf, cancion.getRutaImagen(), TAMANIO_RUTA_IMAGEN);

        escribirStringFijo(raf, cancion.getRutaAudio(), TAMANIO_RUTA_AUDIO);

        raf.writeLong(cancion.getFechaAgregado().getTime());

        raf.writeBoolean(true);
    }

    private Cancion leerCancionDesdeArchivo(RandomAccessFile raf) throws IOException {

    long inicioRegistro = raf.getFilePointer();

    String nombre = leerStringFijo(raf, TAMANIO_NOMBRE);
    String artista = leerStringFijo(raf, TAMANIO_ARTISTA);

    int duracion = raf.readInt();

    String generoStr = leerStringFijo(raf, TAMANIO_GENERO);

    GeneroMusical genero;
    try {
        genero = GeneroMusical.valueOf(generoStr.trim());
    } catch (Exception e) {
        genero = GeneroMusical.OTROS;
    }

    String rutaImagen = leerStringFijo(raf, TAMANIO_RUTA_IMAGEN);
    String rutaAudio = leerStringFijo(raf, TAMANIO_RUTA_AUDIO);

    long fechaMilis = raf.readLong();
    Date fechaAgregado = new Date(fechaMilis);

    boolean activo = raf.readBoolean();

    
    if (!activo) {
        return null;
    }

    Cancion cancion = new Cancion(
        nombre.trim(),
        artista.trim(),
        duracion,
        rutaImagen.trim(),
        rutaAudio.trim(),
        genero
    );

    cancion.setFechaAgregado(fechaAgregado);

    return cancion;
}

    private void escribirStringFijo(RandomAccessFile raf, String texto, int tamanio) throws IOException {
        if (texto == null) {
            texto = "";
        }
        if (texto.length() > tamanio) {
            texto = texto.substring(0, tamanio);
        }

        while (texto.length() < tamanio) {
            texto = texto + " ";
        }

        for (int i = 0; i < tamanio; i++) {
            raf.writeChar(texto.charAt(i));
        }

    }

    private String leerStringFijo(RandomAccessFile raf, int tamanio) throws IOException {
        String texto = "";
        for (int i = 0; i < tamanio; i++) {
            texto = texto + raf.readChar();
        }

        return texto;
    }

    public String obtenerInfoArchivo() {
        String info = "";
        info += "=== INFORMACION DEL ARCHIVO ===\n";
        info += "Ruta: " + rutaArchivo + "\n";
        try (RandomAccessFile raf = new RandomAccessFile(rutaArchivo, "r")) {
            long tamanioBytes = raf.length();
            int totalRegistros = (int) (tamanioBytes / TAMANIO_REGISTRO);

            info += "Tamaño del archivo: " + tamanioBytes + " bytes\n";
            info += "Tamaño por registro: " + TAMANIO_REGISTRO + " bytes\n";
            info += "Total de registros: " + totalRegistros + "\n";

        } catch (IOException e) {
            info += "Error al leer archivo: " + e.getMessage() + "\n";
        }
        return info;
    }

}
