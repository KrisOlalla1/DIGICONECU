package com.microservices.errormapping.controller;

import com.microservices.errormapping.dto.ErrorRequestDTO;
import com.microservices.errormapping.dto.ErrorResponseDTO;
import com.microservices.errormapping.mapper.ErrorMapper;
import com.microservices.errormapping.service.ErrorMappingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/error-mapping")
public class ErrorMappingController {

    private final ErrorMappingService errorMappingService;
    private final ErrorMapper errorMapper;

    public ErrorMappingController(ErrorMappingService errorMappingService, ErrorMapper errorMapper) {
        this.errorMappingService = errorMappingService;
        this.errorMapper = errorMapper;
    }

    @PostMapping("/traducir")
    public ResponseEntity<ErrorResponseDTO> traducir(@RequestBody ErrorRequestDTO request) {
        return errorMappingService.traducirError(request.getBancoOrigen(), request.getCodigoExterno())
                .map(errorMapper::toResponseDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
