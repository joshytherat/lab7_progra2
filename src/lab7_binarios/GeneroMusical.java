/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package lab7_binarios;

/**
 *
 * @author janinadiaz
 */
public enum GeneroMusical {
    ROCK("Rock"),
    POP("Pop"),
    JAZZ("Jazz"),
    CLASSICAL("Clásica"),
    ELECTRONIC("Electrónica"),
    HIP_HOP("Hip Hop"),
    REGGAE("Reggae"),
    COUNTRY("Country"),
    BLUES("Blues"),
    OTROS("Otros");

    private final String descripcion;

    GeneroMusical(String descripcion) {
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
