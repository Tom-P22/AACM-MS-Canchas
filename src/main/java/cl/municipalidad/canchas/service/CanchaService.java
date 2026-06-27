package cl.municipalidad.canchas.service;

import java.util.List;
import java.util.stream.Collectors;

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
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

@Service
@Slf4j
public class CanchaService {

    private final CanchaRepository canchaRepository;
    private final RecintoRepository recintoRepository;
    private final RestClient restClient;

    public CanchaService(CanchaRepository canchaRepository, 
                         RecintoRepository recintoRepository, 
                         RestClient.Builder restClientBuilder) {
        this.canchaRepository = canchaRepository;
        this.recintoRepository = recintoRepository;
        this.restClient = restClientBuilder.build();
    }

    private CanchaResponse mapToDTO(Cancha cancha) {
        return new CanchaResponse(
                cancha.getIdCancha(),
                cancha.getNombre(),
                cancha.getTipoDeCancha(),
                cancha.getFechaRegistro(),
                cancha.getDireccion(),
                cancha.getRecinto() != null ? cancha.getRecinto().getNombreRecinto() : "Sin Recinto Asignado",
                cancha.getCapacidad(),
                cancha.getActivo()
        );
    }

    public CanchaResponse guardarCancha(CanchaCreateRequest request, String emailUsuario) {
        log.info("🕵️‍♂️ El usuario {} está creando una cancha.", emailUsuario);
        
        Recinto recinto = recintoRepository.findById(request.getRecintoId())
                .orElseThrow(() -> new ResourceNotFoundException("Recinto deportivo no encontrado con ID: " + request.getRecintoId()));

        Cancha cancha = new Cancha();
        cancha.setNombre(request.getNombre());
        cancha.setTipoDeCancha(request.getTipoDeCancha());
        cancha.setFechaRegistro(request.getFechaRegistro());
        cancha.setDireccion(request.getDireccion());
        cancha.setRecinto(recinto);
        cancha.setCapacidad(request.getCapacidad());
        cancha.setActivo(true);

        Cancha canchaGuardada = canchaRepository.save(cancha);

        log.info("[CREACIÓN] - Usuario: {} CREÓ la cancha '{}' en el recinto '{}' (ID Cancha: {})", 
            obtenerInfoUsuarioLog(), canchaGuardada.getNombre(), recinto.getNombreRecinto(), canchaGuardada.getIdCancha());

        return mapToDTO(canchaGuardada);
    }

    public List<CanchaResponse> obtenerTodasCanchas(String emailUsuario) {
        String nombreReal = "Desconocido";
        String rol = "Sin Rol";

        if (emailUsuario != null && !emailUsuario.isBlank() && !"Desconocido".equalsIgnoreCase(emailUsuario)) {
            try {
                UsuarioInfoDTO usuario = restClient.get()
                        .uri("http://ms-usuarios/api/v1/usuarios/internal/buscar/email/" + emailUsuario)
                        .retrieve()
                        .body(UsuarioInfoDTO.class);

                if (usuario != null) {
                    nombreReal = usuario.getNombre();
                    rol = usuario.getRolUsuario();
                    log.info("[Éxito] Comunicación directa con ms-usuarios vía Eureka resuelta.");
                }
            } catch (Exception e) {
                log.warn("No se pudo obtener info de ms-usuarios para: {}. Motivo: {}", emailUsuario, e.getMessage());
            }
        }

        log.info("El {} '{}' ({}) solicitó la lista de canchas", rol, nombreReal, emailUsuario);

        return canchaRepository.findAllEager()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public CanchaResponse obtenerUnaCancha(Integer idCancha) {
        Cancha cancha = canchaRepository.findById(idCancha)
                .orElseThrow(() -> new ResourceNotFoundException("Cancha no encontrada con id: " + idCancha));
        return mapToDTO(cancha);
    }

    public CanchaResponse actualizarCancha(Integer idCancha, CanchaUpdateRequest request) {
        Cancha cancha = canchaRepository.findById(idCancha)
            .orElseThrow(() -> new ResourceNotFoundException("No se puede actualizar. Cancha no encontrada con id: " + idCancha));

        cancha.setNombre(request.getNombre());
        cancha.setTipoDeCancha(request.getTipoDeCancha());
        cancha.setFechaRegistro(request.getFechaRegistro());
        cancha.setDireccion(request.getDireccion());
        cancha.setCapacidad(request.getCapacidad());

        Cancha canchaActualizada = canchaRepository.save(cancha);

        log.info("[MODIFICACIÓN] - Usuario: {} MODIFICÓ la cancha '{}' (ID Cancha: {})", 
            obtenerInfoUsuarioLog(), canchaActualizada.getNombre(), canchaActualizada.getIdCancha());

        return mapToDTO(canchaActualizada);
    }

    public void eliminarCancha(Integer idCancha) {
        Cancha cancha = canchaRepository.findById(idCancha)
                .orElseThrow(() -> new ResourceNotFoundException("No se puede dar de baja. Cancha no encontrada con ID: " + idCancha));
        
        cancha.setActivo(false);
        canchaRepository.save(cancha);
                
        log.info("[ELIMINACIÓN] - Usuario: {} DESACTIVÓ la cancha '{}' (ID Cancha: {})", 
                obtenerInfoUsuarioLog(), cancha.getNombre(), cancha.getIdCancha());    
    }

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