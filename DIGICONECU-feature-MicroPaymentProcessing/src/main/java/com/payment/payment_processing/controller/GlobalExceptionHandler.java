package com.payment.payment_processing.controller;

import com.payment.payment_processing.dto.TransferResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

        private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<TransferResponse> handleValidationException(MethodArgumentNotValidException ex) {
                String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                                .collect(Collectors.joining(", "));

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(TransferResponse.builder()
                                                .success(false)
                                                .error(TransferResponse.ErrorBody.builder()
                                                                .code("VALIDATION_ERROR")
                                                                .message(errorMessage)
                                                                .build())
                                                .build());
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<TransferResponse> handleIllegalArgument(IllegalArgumentException ex) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(TransferResponse.builder()
                                                .success(false)
                                                .error(TransferResponse.ErrorBody.builder()
                                                                .code("BAD_REQUEST")
                                                                .message(ex.getMessage())
                                                                .build())
                                                .build());
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<TransferResponse> handleGeneralException(Exception ex) {
                log.error("Error interno no manejado: ", ex);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(TransferResponse.builder()
                                                .success(false)
                                                .error(TransferResponse.ErrorBody.builder()
                                                                .code("MS03")
                                                                .message("Error interno del servidor")
                                                                .build())
                                                .build());
        }
}
