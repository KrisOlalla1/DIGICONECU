package com.microservices.errormapping.service;

import com.microservices.errormapping.model.ErrorDefinition;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class ErrorMappingService {

    private static final Map<String, ErrorDefinition> ERROR_MAP = Map.of(
            "BANCO_A-001", ErrorDefinition.builder()
                    .bancoOrigen("BANCO_A")
                    .codigoExterno("001")
                    .codigoInterno("ERR_FONDOS_INSUFICIENTES")
                    .mensaje("Fondos insuficientes en la cuenta de origen")
                    .build(),
            "BANCO_A-002", ErrorDefinition.builder()
                    .bancoOrigen("BANCO_A")
                    .codigoExterno("002")
                    .codigoInterno("ERR_CUENTA_BLOQUEADA")
                    .mensaje("La cuenta se encuentra bloqueada")
                    .build(),
            "BANCO_B-100", ErrorDefinition.builder()
                    .bancoOrigen("BANCO_B")
                    .codigoExterno("100")
                    .codigoInterno("ERR_TIMEOUT")
                    .mensaje("Tiempo de espera agotado")
                    .build(),
            "BANCO_B-200", ErrorDefinition.builder()
                    .bancoOrigen("BANCO_B")
                    .codigoExterno("200")
                    .codigoInterno("ERR_FORMATO_INVALIDO")
                    .mensaje("Formato de mensaje inválido")
                    .build(),
            "BANCO_C-E01", ErrorDefinition.builder()
                    .bancoOrigen("BANCO_C")
                    .codigoExterno("E01")
                    .codigoInterno("ERR_AUTENTICACION")
                    .mensaje("Error de autenticación")
                    .build()
    );

    public Optional<ErrorDefinition> traducirError(String bancoOrigen, String codigoExterno) {
        String key = bancoOrigen + "-" + codigoExterno;
        return Optional.ofNullable(ERROR_MAP.get(key));
    }
}
