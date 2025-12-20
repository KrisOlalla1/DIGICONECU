package com.digiconecu.network_management_service.controller;

import com.digiconecu.network_management_service.dto.BancoDto;
import com.digiconecu.network_management_service.dto.EnrutamientoSolicitudDto;
import com.digiconecu.network_management_service.dto.EnrutamientoRespuestaDto;
import com.digiconecu.network_management_service.service.RedServicio;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v2/enrutamiento")
public class RedControlador {

    private final RedServicio redServicio;

    public RedControlador(RedServicio redServicio) {
        this.redServicio = redServicio;
    }

    /**
     * RF-02: Punto de entrada crítico para resolver la ruta de una transferencia.
     * Es consumido principalmente por el microservicio de Procesamiento de Pagos.
     */
    @PostMapping("/resolver")
    public ResponseEntity<EnrutamientoRespuestaDto> resolver(@RequestBody EnrutamientoSolicitudDto solicitud) {
        return ResponseEntity.ok(redServicio.resolverEnrutamiento(solicitud));
    }

    /**
     * Obtiene el directorio completo de bancos participantes de la red.
     */
    @GetMapping("/bancos")
    public ResponseEntity<List<BancoDto>> obtenerTodos() {
        return ResponseEntity.ok(redServicio.obtenerTodosLosBancos());
    }

    /**
     * Permite registrar una nueva institución financiera en el ecosistema.
     */
    @PostMapping("/bancos")
    public ResponseEntity<BancoDto> crear(@RequestBody BancoDto bancoDto) {
        return ResponseEntity.ok(redServicio.crearBanco(bancoDto));
    }

    /**
     * RF-02: Permite poner un banco en modo 'Solo recibir' o 'Mantenimiento'.
     * Esto actualiza el estado operativo sin dar de baja al banco.
     */
    @PatchMapping("/bancos/{codigo}/estado")
    public ResponseEntity<Void> actualizarEstado(
            @PathVariable String codigo,
            @RequestParam String nuevoEstado) {
        redServicio.actualizarEstadoBanco(codigo, nuevoEstado);
        return ResponseEntity.noContent().build();
    }
}