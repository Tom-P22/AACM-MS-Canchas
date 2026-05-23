package cl.municipalidad.canchas.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cl.municipalidad.canchas.model.Cancha;

@Repository
public interface CanchaRepository extends JpaRepository<Cancha, Integer> {

    // ── TIPO 1: QUERY METHODS ────────────────────────
    // Busca canchas que contengan una palabra en su nombre (ignorando mayúsculas/minúsculas)
    List<Cancha> findByNombreContainingIgnoreCaseAndActivoTrue(String nombre);

    // Busca canchas cuya capacidad de espectadores o jugadores sea menor que el parámetro
    List<Cancha> findByCapacidadLessThanAndActivoTrue(Integer capacidad);

    // Busca canchas cuya capacidad se encuentre en un rango específico
    List<Cancha> findByCapacidadBetweenAndActivoTrue(Integer min, Integer max);

    // ── TIPO 2: @QUERY CON JPQL ──────────────────────
    // Busca todas las canchas que pertenecen a un Recinto Deportivo específico
    @Query("SELECT c FROM Cancha c WHERE c.recinto.id = :recintoId AND c.activo = true")
    List<Cancha> findByRecintoId(@Param("recintoId") Long recintoId);

    // JPQL con ORDER BY para listar canchas con capacidad menor o igual a una máxima
    @Query("SELECT c FROM Cancha c WHERE c.capacidad <= :capacidadMax AND c.activo = true ORDER BY c.capacidad DESC")
    List<Cancha> findCanchaBajaCapacidad(@Param("capacidadMax") Integer capacidadMax);

    //Query que hace 1 consulta y trae todos los datos de una
    @Query("SELECT c FROM Cancha c JOIN FETCH c.recinto WHERE c.activo = true")
    List<Cancha> findAllEager();
}   