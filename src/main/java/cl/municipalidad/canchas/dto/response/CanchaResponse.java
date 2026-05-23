package cl.municipalidad.canchas.dto.response;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import cl.municipalidad.canchas.model.TipoCancha;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CanchaResponse {

    private Integer idCancha;
    private String nombre;
    private TipoCancha tipoDeCancha;
    private LocalDate fechaRegistro;
    private String direccion;
    private String recinto; // El nombre del recinto deportivo en formato String directo
    private Integer capacidad;
    private Boolean activo;
}