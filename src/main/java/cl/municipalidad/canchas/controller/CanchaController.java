package cl.municipalidad.canchas.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import cl.municipalidad.canchas.dto.DtoApiError;
import cl.municipalidad.canchas.dto.request.CanchaCreateRequest;
import cl.municipalidad.canchas.dto.request.CanchaUpdateRequest;
import cl.municipalidad.canchas.dto.response.CanchaResponse;
import cl.municipalidad.canchas.service.CanchaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/canchas")
@RequiredArgsConstructor
@Tag(name = "Canchas", description = "Endpoints de operaciones CRUD y consultas avanzadas para la gestión de canchas municipales")
public class CanchaController {

    private final CanchaService canchaService;

    // 1. Crear una Cancha
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Registrar una nueva cancha", 
        description = "Permite registrar una cancha vinculada a un recinto deportivo específico dentro de la comuna. **Requiere rol ROLE_ADMIN.**"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "211", description = "Cancha creada exitosamente", 
                     content = @Content(schema = @Schema(implementation = CanchaResponse.class))),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o error en formato JSON (fechas/enums erróneos)", 
                     content = @Content(schema = @Schema(description = "Mapa de errores con el formato {campo: mensaje}"))), // CORREGIDO AQUÍ: Evitamos Map.class conflictivo
        @ApiResponse(responseCode = "401", description = "No autorizado - Token de autenticación JWT faltante o corrupto"),
        @ApiResponse(responseCode = "403", description = "Prohibido - El usuario no cuenta con los privilegios de Administrador"),
        @ApiResponse(responseCode = "404", description = "Recinto deportivo no encontrado con el ID especificado", 
                     content = @Content(schema = @Schema(implementation = DtoApiError.class)))
    })
    public ResponseEntity<CanchaResponse> crearCancha(
            @Valid @RequestBody CanchaCreateRequest request,
            @RequestHeader(value = "X-User-Email", required = false) 
            @Parameter(description = "Correo del usuario que ejecuta la acción (inyectado automáticamente por el API Gateway)", example = "admin@municipalidad.cl") 
            String emailUsuario) {
        return ResponseEntity.status(HttpStatus.CREATED).body(canchaService.guardarCancha(request, emailUsuario));
    }

    // 2. Obtener todas las canchas
    @GetMapping
    @Operation(
        summary = "Listar todas las canchas municipales", 
        description = "Retorna la lista completa de canchas registradas en el sistema. Si el correo del usuario es proporcionado, se enriquecerán los logs internos consultando dinámicamente a ms-usuarios."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de canchas obtenida exitosamente"),
        @ApiResponse(responseCode = "401", description = "No autorizado - Requiere estar autenticado en el ecosistema municipal")
    })
    public ResponseEntity<List<CanchaResponse>> obtenerTodasCanchas(
            @RequestHeader(value = "X-User-Email", required = false) 
            @Parameter(description = "Correo del usuario solicitante para auditoría interna", example = "vecino@correo.cl") 
            String emailUsuario) {
        return ResponseEntity.ok(canchaService.obtenerTodasCanchas(emailUsuario));
    }

    // 3. Obtener una Cancha por ID
    @GetMapping("/{id}")
    @Operation(
        summary = "Buscar una cancha por su ID", 
        description = "Permite consultar los datos específicos de una cancha utilizando su identificador numérico único."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cancha encontrada con éxito", 
                     content = @Content(schema = @Schema(implementation = CanchaResponse.class))),
        @ApiResponse(responseCode = "401", description = "No autorizado - Token JWT requerido"),
        @ApiResponse(responseCode = "404", description = "Cancha no encontrada con el ID ingresado", 
                     content = @Content(schema = @Schema(implementation = DtoApiError.class)))
    })
    public ResponseEntity<CanchaResponse> obtenerUnaCancha(
            @PathVariable("id") @Parameter(description = "ID único de la cancha a consultar", example = "1") Integer id) {
        return ResponseEntity.ok(canchaService.obtenerUnaCancha(id));
    }

    // 4. Actualizar una Cancha
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Actualizar los datos de una cancha", 
        description = "Permite modificar los atributos mutables de una cancha existente (nombre, dirección, tipo, capacidad). **Requiere rol ROLE_ADMIN.**"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cancha modificada exitosamente", 
                     content = @Content(schema = @Schema(implementation = CanchaResponse.class))),
        @ApiResponse(responseCode = "400", description = "Errores de validación en los campos del cuerpo de la petición"),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "403", description = "Prohibido - Acceso exclusivo para administradores"),
        @ApiResponse(responseCode = "404", description = "Cancha no encontrada para actualizar", 
                     content = @Content(schema = @Schema(implementation = DtoApiError.class)))
    })
    public ResponseEntity<CanchaResponse> actualizarCancha(
            @PathVariable("id") @Parameter(description = "ID de la cancha que se desea modificar", example = "2") Integer id, 
            @Valid @RequestBody CanchaUpdateRequest request) {
        return ResponseEntity.ok(canchaService.actualizarCancha(id, request));
    }

    // 5. Eliminar una Cancha
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Dar de baja una cancha (Eliminación lógica)", 
        description = "Cambia el estado de la cancha a inactiva (`activo = false`) para preservar el historial de reservas en base de datos. **Requiere rol ROLE_ADMIN.**"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Cancha desactivada correctamente (No Content)"),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "403", description = "Prohibido - Permiso denegado"),
        @ApiResponse(responseCode = "404", description = "Cancha no encontrada", 
                     content = @Content(schema = @Schema(implementation = DtoApiError.class)))
    })
    public ResponseEntity<Void> eliminarCancha(
            @PathVariable("id") @Parameter(description = "ID de la cancha a dar de baja", example = "1") Integer id) {
        canchaService.eliminarCancha(id);
        return ResponseEntity.noContent().build();
    }

    // ── ENDPOINTS DE BÚSQUEDAS PERSONALIZADAS Y FILTROS ──

    @GetMapping("/buscar")
    @Operation(
        summary = "Buscar canchas activas por coincidencia de nombre", 
        description = "Filtra e identifica de forma dinámica todas las canchas que contengan el texto ingresado, omitiendo mayúsculas/minúsculas."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Búsqueda procesada con éxito")
    })
    public ResponseEntity<List<CanchaResponse>> buscarPorNombre(
            @RequestParam("texto") @Parameter(description = "Texto parcial o completo a buscar", example = "fútbol") String texto) {
        List<CanchaResponse> response = canchaService.buscarPorTitulo(texto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/recinto/{recintoId}")
    @Operation(
        summary = "Listar canchas pertenecientes a un recinto", 
        description = "Devuelve el listado de canchas deportivas activas que se encuentran físicamente dentro de un mismo complejo o recinto municipal."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Listado de canchas por recinto recuperado con éxito")
    })
    public ResponseEntity<List<CanchaResponse>> buscarPorRecinto(
            @PathVariable("recintoId") @Parameter(description = "ID único del recinto deportivo", example = "1") Long recintoId) {
        List<CanchaResponse> response = canchaService.buscarPorRecinto(recintoId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/baja-capacidad")
    @Operation(
        summary = "Listar canchas con capacidad limitada o reducida", 
        description = "Recupera todas las canchas activas cuyo aforo de espectadores o jugadores permitidos sea inferior o igual al límite numérico indicado, ordenadas de mayor a menor."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Filtro por capacidad procesado correctamente")
    })
    public ResponseEntity<List<CanchaResponse>> buscarBajaCapacidad(
            @RequestParam("max") @Parameter(description = "Capacidad máxima permitida en el filtro", example = "15") Integer max) {
        List<CanchaResponse> response = canchaService.buscarBajoCapacidad(max);
        return ResponseEntity.ok(response);
    }
}