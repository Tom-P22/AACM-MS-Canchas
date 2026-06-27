package cl.municipalidad.canchas.dto.request;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import cl.municipalidad.canchas.model.TipoCancha;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CanchaUpdateRequest {

    @NotBlank(message = "El nombre de la cancha es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String nombre;

    @NotNull(message = "El tipo de cancha es obligatorio")
    private TipoCancha tipoDeCancha;

    @NotNull(message = "La fecha de registro es obligatoria")
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate fechaRegistro;

    @NotBlank(message = "La dirección de la ubicación es obligatoria")
    @Size(max = 150, message = "La dirección no puede superar los 150 caracteres")
    private String direccion;

    @NotNull(message = "La capacidad es obligatoria")
    @Positive(message = "La capacidad debe ser mayor a 0")
    @Max(value = 100000, message = "Capacidad demasiado alta para un recinto municipal")
    private Integer capacidad;
}