package cl.municipalidad.canchas.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cl.municipalidad.canchas.dto.request.CanchaCreateRequest;
import cl.municipalidad.canchas.dto.request.CanchaUpdateRequest;
import cl.municipalidad.canchas.dto.response.CanchaResponse;
import cl.municipalidad.canchas.service.CanchaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/canchas")
@RequiredArgsConstructor
public class CanchaController {

    private final CanchaService canchaService;

    // 1. Crear una Cancha
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CanchaResponse> crearCancha(
            @Valid @RequestBody CanchaCreateRequest request,
            @RequestHeader(value = "X-User-Email", defaultValue = "Desconocido") String emailLogueado
    ) {
        CanchaResponse response = canchaService.guardarCancha(request, emailLogueado);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    

    // 2. Obtener todas las Canchas
    @GetMapping
    public ResponseEntity<List<CanchaResponse>> listarTodas(
        
        @RequestHeader(value = "X-User-Email", required = false) String emailLogueado) {
        System.out.println("Petición autorizada por el Gateway para el usuario: " + emailLogueado);

        List<CanchaResponse> response = canchaService.obtenerTodasCanchas(emailLogueado);
        return ResponseEntity.ok(response);
    }

    // 3. Obtener una Cancha por su ID
    @GetMapping("/{id}")
    public ResponseEntity<CanchaResponse> obtenerPorId(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(canchaService.obtenerUnaCancha(id));
    }

    // 4. Actualizar una Cancha
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<CanchaResponse> actualizarCancha(@PathVariable("id") Integer id, @Valid @RequestBody CanchaUpdateRequest request) {
        return ResponseEntity.ok(canchaService.actualizarCancha(id, request));
    }

    // 5. Eliminar una Cancha
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCancha(@PathVariable("id") Integer id) {
        canchaService.eliminarCancha(id);
        return ResponseEntity.noContent().build();
    }

    // ── ENDPOINTS DE BÚSQUEDAS PERSONALIZADAS ──

    // Buscar por nombre (Query Parameter: ?texto=pasto)
    @GetMapping("/buscar")
    public ResponseEntity<List<CanchaResponse>> buscarPorNombre(@RequestParam("texto") String texto) {
        List<CanchaResponse> response = canchaService.buscarPorTitulo(texto);
        return ResponseEntity.ok(response);
    }

    // Buscar por ID de Recinto
    @GetMapping("/recinto/{recintoId}")
    public ResponseEntity<List<CanchaResponse>> buscarPorRecinto(@PathVariable("recintoId") Long recintoId) {
        List<CanchaResponse> response = canchaService.buscarPorRecinto(recintoId);
        return ResponseEntity.ok(response);
    }

    // Buscar canchas de baja capacidad
    @GetMapping("/baja-capacidad")
    public ResponseEntity<List<CanchaResponse>> buscarBajaCapacidad(@RequestParam("max") Integer max) {
        List<CanchaResponse> response = canchaService.buscarBajoCapacidad(max);
        return ResponseEntity.ok(response);
    }
}