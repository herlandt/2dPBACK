package com.example.demo.dto;

import lombok.Data;

/** Petición para crear un documento Office EN BLANCO en el trámite. */
@Data
public class CrearDocumentoBlancoRequest {
    private String tipo;          // "docx" | "xlsx"
    private String nombreLogico;
    private String nodoId;        // rama activa (paralelo); el backend resuelve la actividad
    private String actividadId;   // opcional
}
