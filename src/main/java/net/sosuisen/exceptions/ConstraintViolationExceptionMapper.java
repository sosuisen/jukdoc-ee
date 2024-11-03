package net.sosuisen.exceptions;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.List;

@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {
    public record ErrorResponse(String type, List<String> errors) {
    }

    ;

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        var response = new ErrorResponse("constraint_error",
                exception.getConstraintViolations().stream().map(ConstraintViolation::getMessage).toList());
        return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
    }
}