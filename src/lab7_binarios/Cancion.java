/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lab7_binarios;

/**
 *
 * @author janinadiaz
 */
import java.util.Date;

public class Cancion {

    private String nombre;
    private String artista;
    private int duracionSegundos;
    private String rutaImagen;
    private String rutaAudio;
    private GeneroMusical genero;

    private Date fechaAgregado;
    private Date ultimaReproduccion;

    public Cancion(String nombre, String artista, int duracionSegundos, String rutaImagen, String rutaAudio, GeneroMusical genero) {
        this.nombre = nombre;
        this.artista = artista;
        this.duracionSegundos = duracionSegundos;
        this.rutaImagen = rutaImagen;
        this.rutaAudio = rutaAudio;
        this.fechaAgregado = new Date();
        this.ultimaReproduccion = null;
        this.genero = genero;
    }

    public Cancion(String nombre, String artista, int duracionSegundos, String rutaImagen, GeneroMusical genero) {
        this(nombre, artista, duracionSegundos, rutaImagen, "", genero);
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getArtista() {
        return artista;
    }

    public void setArtista(String artista) {
        this.artista = artista;
    }

    public int getDuracionSegundos() {
        return duracionSegundos;
    }

    public void setDuracionSegundos(int duracionSegundos) {
        this.duracionSegundos = duracionSegundos;
    }

    public String getRutaImagen() {
        return rutaImagen;
    }

    public void setRutaImagen(String rutaImagen) {
        this.rutaImagen = rutaImagen;
    }

    public String getRutaAudio() {
        return rutaAudio;
    }

    public void setRutaAudio(String rutaAudio) {
        this.rutaAudio = rutaAudio;
    }

    public Date getFechaAgregado() {
        return fechaAgregado;
    }

    public void setFechaAgregado(Date fechaAgregado) {
        this.fechaAgregado = fechaAgregado;
    }

    public Date getUltimaReproduccion() {
        return ultimaReproduccion;
    }

    public void setUltimaReproduccion(Date ultimaReproduccion) {
        this.ultimaReproduccion = ultimaReproduccion;
    }

    public String getDuracionFormateada() {
        int minutos = duracionSegundos / 60;
        int segundos = duracionSegundos % 60;
        if (segundos < 10) {
            return minutos + ":0" + segundos;
        }
        return minutos + ":" + segundos;
    }

    public void marcarReproducida() {
        this.ultimaReproduccion = new Date();
    }

    public boolean hasSidoReproducida() {
        return ultimaReproduccion != null;
    }

    public String toString() {
        return nombre + " - " + artista + " [" + getDuracionFormateada() + "]( " + genero + " )";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Cancion cancion = (Cancion) obj;

        return nombre.equalsIgnoreCase(cancion.nombre) && artista.equalsIgnoreCase(cancion.artista);
    }

    public GeneroMusical getGenero() {
        return genero;
    }

    public void setGenero(GeneroMusical genero) {
        this.genero = genero;
    }
}
