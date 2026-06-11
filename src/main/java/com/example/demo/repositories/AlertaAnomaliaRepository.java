package com.example.demo.repositories;

import com.example.demo.models.AlertaAnomalia;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertaAnomaliaRepository extends MongoRepository<AlertaAnomalia, String> {

    List<AlertaAnomalia> findByTramiteIdOrderByFechaDeteccionDesc(String tramiteId);

    List<AlertaAnomalia> findByFalsoPositivoFalseOrderByFechaDeteccionDesc();

    /** Dedup de detección: ¿ya hay CUALQUIER alerta (abierta o ya marcada falso
     *  positivo) para ese trámite + categoría? Incluir las falso-positivo evita
     *  re-detectar lo que el admin ya descartó. */
    boolean existsByTramiteIdAndCategoria(String tramiteId, String categoria);
}
