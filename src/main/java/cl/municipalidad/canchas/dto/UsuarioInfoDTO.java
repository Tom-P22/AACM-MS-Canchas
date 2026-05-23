package cl.municipalidad.canchas.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UsuarioInfoDTO {
    private String nombre;
    private String email;
    @JsonProperty("rolUsuario") 
    private String rolUsuario;
    @JsonProperty("tipo usuario")
    private String tipoUsuario;
}