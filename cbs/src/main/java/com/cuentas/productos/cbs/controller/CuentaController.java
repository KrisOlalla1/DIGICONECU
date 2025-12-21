package com.cuentas.productos.cbs.controller;

import com.cuentas.productos.cbs.dto.CrearCuentaRequest;
import com.cuentas.productos.cbs.dto.CuentaResponse;
import com.cuentas.productos.cbs.service.CuentaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/cuentas")
public class CuentaController {

    private final CuentaService service;

    public CuentaController(CuentaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CuentaResponse> crear(@Valid @RequestBody CrearCuentaRequest req) {
        CuentaResponse creada = service.crear(req);
        URI location = URI.create("/api/cuentas/" + creada.getCuentaId());
        return ResponseEntity.created(location).body(creada);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CuentaResponse> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @GetMapping
    public ResponseEntity<List<CuentaResponse>> listarPorCliente(@RequestParam Integer clienteId) {
        return ResponseEntity.ok(service.listarPorCliente(clienteId));
    }

    @GetMapping("/por-numero/{numeroCuenta}")
    public ResponseEntity<CuentaResponse> buscarPorNumero(@PathVariable String numeroCuenta) {
        return service.buscarPorNumero(numeroCuenta)
                .map(com.cuentas.productos.cbs.mapper.CuentaMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
