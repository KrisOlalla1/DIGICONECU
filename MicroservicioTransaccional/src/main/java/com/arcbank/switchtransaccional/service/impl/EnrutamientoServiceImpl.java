package com.arcbank.switchtransaccional.service.impl;

import com.arcbank.switchtransaccional.model.dto.TransaccionResponse;
import com.arcbank.switchtransaccional.service.IEnrutamientoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class EnrutamientoServiceImpl implements IEnrutamientoService {

    @Override
    public TransaccionResponse procesarTransaccion(Integer idInstruccion) {
        log.info("Iniciando enrutamiento de transacción {}", idInstruccion);

        throw new UnsupportedOperationException("TODO: Implementar por Persona 2");
    }
}
