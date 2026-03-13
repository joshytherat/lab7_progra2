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

    private static final int TAMANIO_NOMBRE=100;
    private static final int TAMANIO_ARTISTA=100;
    private static final int TAMANIO_GENERO=30;
    private static final int TAMANIO_RUTA_IMAGEN=200;

    private static final int TAMANIO_REGISTRO = (TAMANIO_NOMBRE * 2)+(TAMANIO_ARTISTA * 2) + 4 + (TAMANIO_GENERO * 2) + (TAMANIO_RUTA_IMAGEN * 2) + 8 + 1;
    // total 873 bytes 

    private String rutaArchivo;

    public GestorCancionesArchivo(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
        try {
            File archivo = new File(rutaArchivo);
            if (!archivo.exists()){
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
        try{
            raf = new RandomAccessFile(rutaArchivo, "r");
            long tamanioArchivo = raf.length();
            long byteOffset = (long) posicion * TAMANIO_REGISTRO;

            if (byteOffset >= tamanioArchivo) {
                System.out.println("Posición fuera de rango");
                return null;
            }
            raf.seek(byteOffset);
            return leerCancionDesdeArchivo(raf);

        }catch(IOException e) {
            System.err.println("Error al leer canción: " + e.getMessage());
            throw e;
        } finally {
            if(raf != null) {
                try {
                    raf.close();
                } catch (IOException e) {
                    System.err.println("Error al cerrar archivo: " + e.getMessage());
                }
            }
        }
    }

    
}
