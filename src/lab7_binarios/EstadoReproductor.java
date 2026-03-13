/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package lab7_binarios;

/**
 *
 * @author janinadiaz
 */
public enum EstadoReproductor {
    PLAYING("Reproduciendo"),
    PAUSED("Pausado"),
    STOPPED("Detenido");
    
    private final String descripcion;
    EstadoReproductor(String descripcion) {
        this.descripcion = descripcion;
    }
    public String getDescripcion() {
        return descripcion;
    }
    @Override
    public String toString() {
        return descripcion;
    }
}