package cl.municipalidad.canchas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import cl.municipalidad.canchas.model.Recinto;

public interface RecintoRepository extends JpaRepository<Recinto, Long> {
}