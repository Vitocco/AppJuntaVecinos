package com.zaror.reserva.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sede")
@Getter
@Setter
@NoArgsConstructor
public class Sede {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    private String ubicacion;

    @Column(nullable = false)
    private int capacidad;

    // Precio en CLP por evento
    @Column(nullable = false)
    private int precio;

    @Column(nullable = false)
    private boolean activa;

    // Rutas tipo "/api/sedes/fotos/salon-1.jpg" (tabla sede_foto). La primera es la portada.
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "sede_foto", joinColumns = @JoinColumn(name = "sede_id"))
    @OrderColumn(name = "orden")
    @Column(name = "ruta", nullable = false)
    private List<String> fotos = new ArrayList<>();
}
