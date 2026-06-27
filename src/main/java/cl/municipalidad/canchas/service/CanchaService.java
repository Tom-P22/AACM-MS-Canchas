package cl.municipalidad.canchas.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import cl.municipalidad.canchas.dto.UsuarioInfoDTO;
import cl.municipalidad.canchas.dto.request.CanchaCreateRequest;
import cl.municipalidad.canchas.dto.request.CanchaUpdateRequest;
import cl.municipalidad.canchas.dto.response.CanchaResponse;
import cl.municipalidad.canchas.exception.ResourceNotFoundException;
import cl.municipalidad.canchas.model.Cancha;
import cl.municipalidad.canchas.model.Recinto;
import cl.municipalidad.canchas.repository.CanchaRepository;
import cl.municipalidad.canchas.repository.RecintoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

@Service
@RequiredArgsConstructor
@Slf4j
public class CanchaService {

    private final CanchaRepository canchaRepository;
    private final RecintoRepository recintoRepository;
    
    @Qualifier("loadBalancedRestClientBuilder")
    private final RestClient.Builder restClientBuilder;

    // ── MAPEO PRIVADO: Entidad → ResponseDTO ─────────
    private CanchaResponse mapToDTO(Cancha cancha) {
        return new CanchaResponse(
                cancha.getIdCancha(),
                cancha.getNombre(),
                cancha.getTipoDeCancha(),
                cancha.getFechaRegistro(),
                cancha.getDireccion(),
                cancha.getRecinto() != null ? cancha.getRecinto().getNombreRecinto() : "Sin Recinto",
                cancha.getCapacidad(),
                cancha.getActivo()
        );
    }

    // 1. Guardar una Cancha
    public CanchaResponse guardarCancha(CanchaCreateRequest request, String emailUsuario) {
        log.info("Usuario ejecutando acción: {}", obtenerInfoUsuarioLog());

        Recinto recinto = recintoRepository.findById(request.getRecintoId())
                .orElseThrow(() -> new ResourceNotFoundException("Recinto no encontrado con ID: " + request.getRecintoId()));

        Cancha cancha = new Cancha();
        cancha.setNombre(request.getNombre());
        cancha.setTipoDeCancha(request.getTipoDeCancha());
        cancha.setFechaRegistro(request.getFechaRegistro());
        cancha.setDireccion(request.getDireccion());
        cancha.setCapacidad(request.getCapacidad());
        cancha.setRecinto(recinto);
        cancha.setActivo(true);

        Cancha guardada = canchaRepository.save(cancha);
        return mapToDTO(guardada);
    }

    // 2. Obtener todas las canchas
    public List<CanchaResponse> obtenerTodasCanchas(String emailUsuario) {
        if (emailUsuario != null && !emailUsuario.isBlank()) {
            try {
                UsuarioInfoDTO usuario = restClientBuilder.build().get()
                        .uri("http://ms-usuarios/api/v1/usuarios/internal/buscar/email/" + emailUsuario)
                        .retrieve()
                        .body(UsuarioInfoDTO.class);
                if (usuario != null) {
                    log.info("Consulta realizada por: {} (Rol: {})", usuario.getNombre(), usuario.getRolUsuario());
                }
            } catch (Exception e) {
                log.warn("No se pudo obtener la info extendida del usuario desde ms-usuarios: {}", e.getMessage());
            }
        }
        return canchaRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    // 3. Obtener una cancha por ID
    public CanchaResponse obtenerUnaCancha(Integer id) {
        Cancha cancha = canchaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cancha no encontrada con ID: " + id));
        return mapToDTO(cancha);
    }

    // 4. Actualizar una cancha
    public CanchaResponse actualizarCancha(Integer id, CanchaUpdateRequest request) {
        Cancha cancha = canchaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cancha no encontrada para actualizar con ID: " + id));

        cancha.setNombre(request.getNombre());
        cancha.setTipoDeCancha(request.getTipoDeCancha());
        cancha.setFechaRegistro(request.getFechaRegistro());
        cancha.setDireccion(request.getDireccion());
        cancha.setCapacidad(request.getCapacidad());

        Cancha actualizada = canchaRepository.save(cancha);
        return mapToDTO(actualizada);
    }

    // 5. Eliminar cancha (Baja lógica)
    public void eliminarCancha(Integer id) {
        Cancha cancha = canchaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cancha no encontrada para eliminar con ID: " + id));
        cancha.setActivo(false);
        canchaRepository.save(cancha);
    }

    // ── ENDPOINTS DE BÚSQUEDAS PERSONALIZADAS ──
    public List<CanchaResponse> buscarPorTitulo(String texto) {
        return canchaRepository.findByNombreContainingIgnoreCaseAndActivoTrue(texto)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<CanchaResponse> buscarPorRecinto(Long recintoId) {
        return canchaRepository.findByRecintoId(recintoId)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<CanchaResponse> buscarBajoCapacidad(Integer capacidadMax) {
        return canchaRepository.findCanchaBajaCapacidad(capacidadMax)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    private String obtenerInfoUsuarioLog() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof Jwt jwt) {
                String nombre = jwt.getSubject(); 
                String correo = jwt.getClaimAsString("email");
                
                nombre = (nombre != null) ? nombre : auth.getName();
                correo = (correo != null) ? correo : "sin-correo@municipalidad.cl";
                
                return String.format("%s [%s]", nombre, correo);
            }
        } catch (Exception e) {
            log.warn("Error extrayendo contexto de seguridad", e);
        }
        return "Usuario Administrador [admin@municipalidad.cl]";
    }
}