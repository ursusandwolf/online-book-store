# Documentation

## Global Exception Handling
The project uses `@ControllerAdvice` to handle exceptions globally, providing structured error responses to clients.

## Validation
Request payloads are validated using JSR-303 annotations on DTO fields and `@Valid` in Controller methods.
