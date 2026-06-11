package com.example.demo.services;

import com.example.demo.util.ByteArrayMultipartFile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Provee plantillas EN BLANCO de Office (Word/Excel) para "Crear documento".
 * Los archivos minimos validos viven en resources/templates/ y se cargan del
 * classpath; se entregan como MultipartFile para reutilizar el flujo de subida.
 */
@Component
public class BlankOfficeTemplates {

    private byte[] docx;
    private byte[] xlsx;

    public MultipartFile plantilla(String tipo, String nombreLogico) {
        String t = tipo == null ? "" : tipo.toLowerCase();
        if (t.equals("docx") || t.equals("word")) {
            if (docx == null) docx = cargar("templates/blank.docx");
            return new ByteArrayMultipartFile("archivo", conExt(nombreLogico, "docx"),
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document", docx);
        }
        if (t.equals("xlsx") || t.equals("excel")) {
            if (xlsx == null) xlsx = cargar("templates/blank.xlsx");
            return new ByteArrayMultipartFile("archivo", conExt(nombreLogico, "xlsx"),
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", xlsx);
        }
        throw new IllegalArgumentException("Tipo no soportado: " + tipo + " (usa 'docx' o 'xlsx').");
    }

    /** Tipo de documento del catálogo (WORD/EXCEL) según el tipo pedido. */
    public String tipoCatalogo(String tipo) {
        String t = tipo == null ? "" : tipo.toLowerCase();
        return (t.equals("docx") || t.equals("word")) ? "WORD" : "EXCEL";
    }

    private byte[] cargar(String path) {
        try {
            return new ClassPathResource(path).getInputStream().readAllBytes();
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo cargar la plantilla " + path, e);
        }
    }

    private static String conExt(String nombre, String ext) {
        String base = (nombre == null || nombre.isBlank()) ? "documento" : nombre;
        return base.toLowerCase().endsWith("." + ext) ? base : base + "." + ext;
    }
}
