package com.transswitch.returnmanagementservice.controller;

import com.transswitch.returnmanagementservice.dto.ApiResponse;
import com.transswitch.returnmanagementservice.service.ServiceException;
import java.time.OffsetDateTime;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ApiResponse<Object>> handleService(ServiceException ex) {
        int status = Integer.parseInt(ex.getHttpCode());
        String ts = OffsetDateTime.now().toString();
        return ResponseEntity.status(status).body(ApiResponse.fail(ex.getCode(), ex.getMessage(), ex.getDetails(), ts));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidation(MethodArgumentNotValidException ex) {
        String ts = OffsetDateTime.now().toString();
        return ResponseEntity.status(400).body(ApiResponse.fail("VALIDATION_ERROR", "VALIDATION_ERROR", "Datos inválidos", ts));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneric(Exception ex) {
        String ts = OffsetDateTime.now().toString();
        return ResponseEntity.status(500).body(ApiResponse.fail("INTERNAL_ERROR", "INTERNAL_ERROR", "Error inesperado", ts));
    }
}
