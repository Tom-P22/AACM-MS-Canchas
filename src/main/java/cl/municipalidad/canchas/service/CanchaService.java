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
    private final RestClient restClient = RestClient.create();

    // ── MAPEO PRIVADO: Entidad → ResponseDTO ─────────
    private CanchaResponse mapToDTO(Cancha cancha) {
        return new CanchaResponse(
                cancha.getIdCancha(),
                cancha.getNombre(),
                cancha.getTipoDeCancha(),
                cancha.getFechaRegistro(),
                cancha.getDireccion(),
                cancha.getRecinto().getNombreRecinto(),
                cancha.getCapacidad(),
                cancha.getActivo()
        );
    }

    // Crear una cancha
    public CanchaResponse guardarCancha(CanchaCreateRequest request, String emailUsuario) {
        System.out.println("🕵️‍♂️ El usuario " + emailUsuario + " está creando una cancha.");
        
        Recinto recinto = recintoRepository.findById(request.getRecintoId())
                .orElseThrow(() -> new RuntimeException("Recinto deportivo no encontrado"));

        Cancha cancha = new Cancha();
        cancha.setNombre(request.getNombre());
        cancha.setTipoDeCancha(request.getTipoDeCancha());
        cancha.setFechaRegistro(request.getFechaRegistro());
        cancha.setDireccion(request.getDireccion());
        cancha.setRecinto(recinto);
        cancha.setCapacidad(request.getCapacidad());

        Cancha canchaGuardada = canchaRepository.save(cancha);

        log.info("[CREACIÓN] - Usuario: {} CREÓ la cancha '{}' en el recinto '{}' (ID Cancha: {})", 
            obtenerInfoUsuarioLog(), 
            canchaGuardada.getNombre(), 
            recinto.getNombreRecinto(), 
            canchaGuardada.getIdCancha());

        return mapToDTO(canchaGuardada);
    }

    // Obtener todas las canchas
    public List<CanchaResponse> obtenerTodasCanchas(String emailUsuario) {
        String nombreReal = "Desconocido";
        String rol = "Sin Rol";
        try {
            UsuarioInfoDTO usuario = restClient.get()
                    .uri("http://localhost:8081/api/v1/usuarios/internal/buscar/email/" + emailUsuario)
                    .retrieve()
                    .body(UsuarioInfoDTO.class);

            if (usuario != null) {
                nombreReal = usuario.getNombre();
                rol = usuario.getRolUsuario();
                log.info("[Exito] Se establecio conexion a MS-Usuarios correctamente");
            } else {
                log.warn("No hubo error al conectar, pero se devolvio el usuario vacio");
            }

        } catch (Exception e) {
            log.warn("No se pudo obtener la info para el email " + emailUsuario + " debido a " + e.getMessage());
        }

        log.info("Iniciando búsqueda de todas las canchas");
        log.info("El {} '{}' ({}) solicitó la lista de  canchas", rol, nombreReal, emailUsuario);

        return canchaRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

    }

    // Obtener una cancha por id
    public CanchaResponse obtenerUnaCancha(Integer idCancha) {
        Cancha cancha = canchaRepository.findById(idCancha)
                .orElseThrow(() -> {
                    log.warn("Cancha no encontrada id: {}", idCancha);
                    return new ResourceNotFoundException("Cancha no encontrada con id: " + idCancha);
                });

        return mapToDTO(cancha);
    }

    // Actualizar una cancha
    public CanchaResponse actualizarCancha(Integer idCancha, CanchaUpdateRequest request) {
        Cancha cancha = canchaRepository.findById(idCancha)
            .orElseThrow(() -> new ResourceNotFoundException("No se puede actualizar. Cancha no encontrada con id: " + idCancha));

        cancha.setNombre(request.getNombre());
        cancha.setTipoDeCancha(request.getTipoDeCancha());
        cancha.setFechaRegistro(request.getFechaRegistro());
        cancha.setDireccion(request.getDireccion());
        cancha.setCapacidad(request.getCapacidad());

        Cancha canchaActualizada = canchaRepository.save(cancha);

        log.info("[MODIFICACIÓN] - Usuario: {} MODIFICÓ la cancha '{}' del recinto '{}' (ID Cancha: {})", 
            obtenerInfoUsuarioLog(), 
            canchaActualizada.getNombre(), 
            canchaActualizada.getRecinto().getNombreRecinto(), 
            canchaActualizada.getIdCancha());

        return mapToDTO(canchaActualizada);
    }

    // Eliminar una cancha
    public void eliminarCancha(Integer idCancha) {
    Cancha cancha = canchaRepository.findById(idCancha)
            .orElseThrow(() -> new ResourceNotFoundException("No se puede dar de baja. Cancha no encontrada con ID: " + idCancha));
            cancha.setActivo(false);
            canchaRepository.save(cancha);
            
    log.info("[ELIMINACIÓN] - Usuario: {} DIO DE BAJA (Borrado Lógico) la cancha '{}' del recinto '{}' (ID Cancha: {})", 
            obtenerInfoUsuarioLog(), 
            cancha.getNombre(), 
            cancha.getRecinto().getNombreRecinto(), 
            cancha.getIdCancha());    
    log.info("La cancha con ID {} fue desactivada exitosamente", idCancha);
    }

    // ── BÚSQUEDAS PERSONALIZADAS ──
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
            String nombre = jwt.getClaimAsString("name");
            String correo = jwt.getClaimAsString("email");
            
            nombre = (nombre != null) ? nombre : auth.getName();
            correo = (correo != null) ? correo : "sin-correo@municipalidad.cl";
            
            return String.format("%s [%s]", nombre, correo);
        }
    } catch (Exception e) {
        log.warn("No se pudo extraer la info del usuario desde el contexto de seguridad", e);
    }
    return "Usuario Administrador [admin@municipalidad.cl]"; //User por defecto si no hay token
}
}