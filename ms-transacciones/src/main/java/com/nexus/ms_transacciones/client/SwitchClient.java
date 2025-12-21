package com.nexus.ms_transacciones.client;

import com.nexus.ms_transacciones.dto.SwitchTransaccionWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class SwitchClient {

    private final RestTemplate restTemplate;
    private static final String URL_SWITCH = "https://api.bancocentral.fin.ec/switch/v1/transacciones";

    public void enviar(SwitchTransaccionWrapper request) {
        // Si falla, el RestTemplate lanza excepción y activa el Catch del SAGA
        restTemplate.postForEntity(URL_SWITCH, request, Void.class);
    }
}