package cl.municipalidad.canchas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import cl.municipalidad.canchas.dto.request.CanchaCreateRequest;
import cl.municipalidad.canchas.dto.request.CanchaUpdateRequest;
import cl.municipalidad.canchas.dto.response.CanchaResponse;
import cl.municipalidad.canchas.exception.ResourceNotFoundException;
import cl.municipalidad.canchas.model.Cancha;
import cl.municipalidad.canchas.model.Recinto;
import cl.municipalidad.canchas.model.TipoCancha;
import cl.municipalidad.canchas.repository.CanchaRepository;
import cl.municipalidad.canchas.repository.RecintoRepository;

@ExtendWith(MockitoExtension.class)
class CanchaServiceTest {

    @Mock
    private CanchaRepository canchaRepository;

    @Mock
    private RecintoRepository recintoRepository;

    @Mock
    private RestClient.Builder restClientBuilder;
    
    @Mock
    private RestClient restClient;

    @InjectMocks
    private CanchaService canchaService;

    @BeforeEach
    void setUp() {
        Mockito.lenient().when(restClientBuilder.build()).thenReturn(restClient);
    }

    // ── MÉTODOS AUXILIARES DE DATOS ──
    private Recinto crearRecinto() {
        Recinto recinto = new Recinto();
        recinto.setId(1L);
        recinto.setNombreRecinto("Complejo Deportivo Norte");
        return recinto;
    }

    private Cancha crearCancha() {
        Cancha cancha = new Cancha();
        cancha.setIdCancha(1);
        cancha.setNombre("Cancha Central");
        if (TipoCancha.values().length > 0) {
            cancha.setTipoDeCancha(TipoCancha.values()[0]);
        }
        cancha.setFechaRegistro(LocalDate.now());
        cancha.setDireccion("Calle Falsa 123");
        cancha.setCapacidad(22);
        cancha.setActivo(true);
        cancha.setRecinto(crearRecinto());
        return cancha;
    }

    // ── TEST 1: GUARDAR CANCHA ÉXITO ──
    @Test
    void guardarCancha_debeCrearCanchaCuandoRecintoExiste() {
        CanchaCreateRequest request = new CanchaCreateRequest();
        request.setNombre("Cancha Central");
        if (TipoCancha.values().length > 0) {
            request.setTipoDeCancha(TipoCancha.values()[0]);
        }
        request.setFechaRegistro(LocalDate.now());
        request.setDireccion("Calle Falsa 123");
        request.setCapacidad(22);
        request.setRecintoId(1L);

        Recinto recinto = crearRecinto();
        Cancha canchaGuardada = crearCancha();

        when(recintoRepository.findById(1L)).thenReturn(Optional.of(recinto));
        when(canchaRepository.save(any(Cancha.class))).thenReturn(canchaGuardada);

        CanchaResponse resultado = canchaService.guardarCancha(request, "test@municipalidad.cl");

        assertThat(resultado).isNotNull();
        assertThat(resultado.getNombre()).isEqualTo("Cancha Central");
        verify(recintoRepository).findById(1L);
        verify(canchaRepository).save(any(Cancha.class));
    }

    // ── TEST 2: GUARDAR CANCHA FALLO (Recinto no existe) ──
    @Test
    void guardarCancha_debeLanzarExcepcionCuandoRecintoNoExiste() {
        CanchaCreateRequest request = new CanchaCreateRequest();
        request.setRecintoId(99L);

        when(recintoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> canchaService.guardarCancha(request, "test@municipalidad.cl"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Recinto no encontrado");

        verify(canchaRepository, never()).save(any(Cancha.class));
    }

    // ── TEST 3: OBTENER UNA CANCHA ÉXITO ──
    @Test
    void obtenerUnaCancha_debeRetornarCanchaCuandoExiste() {
        Cancha cancha = crearCancha();
        when(canchaRepository.findById(1)).thenReturn(Optional.of(cancha));

        CanchaResponse resultado = canchaService.obtenerUnaCancha(1);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getIdCancha()).isEqualTo(1);
        assertThat(resultado.getNombre()).isEqualTo("Cancha Central");
        verify(canchaRepository).findById(1);
    }

    // ── TEST 4: ACTUALIZAR CANCHA ÉXITO ──
    @Test
    void actualizarCancha_debeActualizarCuandoExiste() {
        Cancha canchaExistente = crearCancha();
        CanchaUpdateRequest request = new CanchaUpdateRequest();
        request.setNombre("Cancha Modificada");
        if (TipoCancha.values().length > 0) {
            request.setTipoDeCancha(TipoCancha.values()[0]);
        }
        request.setFechaRegistro(LocalDate.now());
        request.setDireccion("Nueva Dirección 456");
        request.setCapacidad(14);

        Cancha canchaActualizada = crearCancha();
        canchaActualizada.setNombre("Cancha Modificada");
        canchaActualizada.setCapacidad(14);

        when(canchaRepository.findById(1)).thenReturn(Optional.of(canchaExistente));
        when(canchaRepository.save(any(Cancha.class))).thenReturn(canchaActualizada);

        CanchaResponse resultado = canchaService.actualizarCancha(1, request);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getNombre()).isEqualTo("Cancha Modificada");
        assertThat(resultado.getCapacidad()).isEqualTo(14);
        verify(canchaRepository).save(canchaExistente);
    }

    // ── TEST 5: ELIMINACIÓN LÓGICA ──
    @Test
    void eliminarCancha_debeCambiarEstadoInactivoCuandoExiste() {
        Cancha cancha = crearCancha();
        assertThat(cancha.getActivo()).isTrue();

        when(canchaRepository.findById(1)).thenReturn(Optional.of(cancha));
        when(canchaRepository.save(any(Cancha.class))).thenAnswer(invocation -> invocation.getArgument(0));

        canchaService.eliminarCancha(1);

        assertThat(cancha.getActivo()).isFalse();
        verify(canchaRepository).save(cancha);
    }
}