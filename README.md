# Error Mapping Service

## Descripción General
Este microservicio traduce códigos de error externos de diferentes bancos a un formato interno estandarizado. Es 100% stateless: no usa base de datos, toda la lógica y configuración está en memoria.

## Tecnologías
- Java 21
- Spring Boot 3.5.9
- Maven
- Lombok
- MapStruct

## Arquitectura
- **Modelo de dominio:** POJO `ErrorDefinition` (sin anotaciones de persistencia)
- **DTOs:** `ErrorRequestDTO` y `ErrorResponseDTO` para entrada/salida
- **Mapper:** `ErrorMapper` (MapStruct) para convertir entre modelo y DTO
- **Servicio:** `ErrorMappingService` con un `Map<String, ErrorDefinition>` estático e inmutable
- **Controlador:** `ErrorMappingController` expone el endpoint REST

## Endpoint Principal
```
POST /api/v2/error-mapping/traducir
```
- **Request:** JSON con `bancoOrigen` y `codigoExterno`
- **Response:** JSON con los datos traducidos o 404 si no existe

## Ejemplo de Request
```json
{
  "bancoOrigen": "BANCO_A",
  "codigoExterno": "001"
}
```

## Ejemplo de Response
```json
{
  "bancoOrigen": "BANCO_A",
  "codigoExterno": "001",
  "codigoInterno": "ERR_FONDOS_INSUFICIENTES",
  "mensaje": "Fondos insuficientes en la cuenta de origen"
}
```

## Explicación del Código
- **ErrorDefinition:** POJO con campos para banco, código externo/interno y mensaje. Usa Lombok para reducir código repetitivo.
- **ErrorRequestDTO/ErrorResponseDTO:** Separan el contrato de la API del modelo interno.
- **ErrorMapper:** MapStruct genera el código de conversión entre modelo y DTO automáticamente.
- **ErrorMappingService:** Contiene el mapa de errores y el método `traducirError` que busca en el mapa usando la clave `bancoOrigen-codigoExterno`.
- **ErrorMappingController:** Expone el endpoint POST, usa inyección por constructor y programación funcional con Optional para responder 200 o 404.

## Stateless
- No hay base de datos ni archivos externos.
- El mapa de errores está hardcodeado y es inmutable.
- El servicio puede escalar horizontalmente sin problemas de sincronización.

## Ejecución
```
cd errormappingservice
mvn spring-boot:run
```

## Mejoras Futuras
- Cargar el mapa de errores desde un archivo externo o servicio de configuración.
- Validación de entrada con anotaciones Bean Validation.
- Documentación OpenAPI/Swagger.
- Tests unitarios y de integración.
