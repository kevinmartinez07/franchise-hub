package com.kevinmartinez.franchise.presentation.error;

import java.net.URI;
import java.util.List;

import com.kevinmartinez.franchise.application.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;

@RestControllerAdvice
@Order(-2)
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    ProblemDetail handleNotFound(ResourceNotFoundException exception, ServerWebExchange exchange) {
        return problem(
                HttpStatus.NOT_FOUND,
                "Recurso no encontrado",
                exception.getMessage(),
                "RESOURCE_NOT_FOUND",
                exchange);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail handleDomainValidation(IllegalArgumentException exception, ServerWebExchange exchange) {
        return problem(
                HttpStatus.BAD_REQUEST,
                "Solicitud inválida",
                exception.getMessage(),
                "INVALID_REQUEST",
                exchange);
    }

    @ExceptionHandler(WebExchangeBindException.class)
    ProblemDetail handleValidation(WebExchangeBindException exception, ServerWebExchange exchange) {
        List<String> errors = exception.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        ProblemDetail detail = problem(
                HttpStatus.BAD_REQUEST,
                "Solicitud inválida",
                "La solicitud contiene campos inválidos",
                "BAD_REQUEST",
                exchange);
        detail.setProperty("errors", errors);
        return detail;
    }

    @ExceptionHandler(ServerWebInputException.class)
    ProblemDetail handleInput(ServerWebInputException exception, ServerWebExchange exchange) {
        return problem(
                HttpStatus.BAD_REQUEST,
                "Solicitud inválida",
                "No fue posible interpretar la solicitud",
                "INVALID_REQUEST",
                exchange);
    }

    @ExceptionHandler(BadCredentialsException.class)
    ProblemDetail handleBadCredentials(BadCredentialsException exception, ServerWebExchange exchange) {
        return problem(
                HttpStatus.UNAUTHORIZED,
                "No autenticado",
                "Usuario o contraseña inválidos",
                "UNAUTHORIZED",
                exchange);
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception exception, ServerWebExchange exchange) {
        LOGGER.error("Error no controlado procesando {} {}",
                exchange.getRequest().getMethod(),
                exchange.getRequest().getPath(),
                exception);

        return problem(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno",
                "Ocurrió un error inesperado al procesar la solicitud",
                "INTERNAL_ERROR",
                exchange);
    }

    private static ProblemDetail problem(
            HttpStatus status,
            String title,
            String message,
            String code,
            ServerWebExchange exchange) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(status, message);
        detail.setTitle(title);
        detail.setType(URI.create("urn:franchise-hub:error:" + code.toLowerCase()));
        detail.setInstance(exchange.getRequest().getURI());
        detail.setProperty("code", code);
        return detail;
    }
}
