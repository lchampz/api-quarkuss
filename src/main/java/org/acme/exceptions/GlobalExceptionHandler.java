package org.acme.exceptions;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.Set;

@Provider
public class GlobalExceptionHandler implements ExceptionMapper<Exception> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(Exception exception) {
        ApiError apiError = new ApiError();
        apiError.setPath(uriInfo.getPath());

        if (exception instanceof ConstraintViolationException) {
            return handleConstraintViolationException((ConstraintViolationException) exception, apiError);
        }

        // Tratamento padrão para outras exceções
        apiError.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        apiError.setError("Erro Interno do Servidor");
        apiError.setMessage(exception.getMessage());

        return Response.status(apiError.getStatus())
                .entity(apiError)
                .build();
    }

    private Response handleConstraintViolationException(ConstraintViolationException ex, ApiError apiError) {
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        
        apiError.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
        apiError.setError("Erro de Validação");
        apiError.setMessage("Dados de entrada inválidos");

        for (ConstraintViolation<?> violation : violations) {
            String field = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            apiError.addValidationError(field, message);
        }

        return Response.status(apiError.getStatus())
                .entity(apiError)
                .build();
    }
} 