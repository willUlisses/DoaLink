package com.doalink_api.exception;

import com.doalink_api.dtos.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflictException(ConflictException e) {
        return new ResponseEntity<>(
                ErrorResponse.from(e, HttpStatus.CONFLICT.value()),
                HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        String causa = e.getMostSpecificCause().getMessage();
        log.warn("Violação de integridade: {}", causa);
        String mensagem = "Conflito de integridade dos dados";
        if (causa != null && causa.contains("uk_reserva_entrega_oferta_ativa")) {
            mensagem = "Esta oferta já possui uma reserva ativa";
        }
        return new ResponseEntity<>(
                new ErrorResponse(mensagem, HttpStatus.CONFLICT.value()),
                HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLock(OptimisticLockingFailureException e) {
        log.warn("Conflito de concorrência otimista: {}", e.getMessage());
        return new ResponseEntity<>(
                new ErrorResponse("O registro foi alterado por outra operação. Atualize e tente novamente.",
                        HttpStatus.CONFLICT.value()),
                HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException e) {
        return new ResponseEntity<>(
                ErrorResponse.from(e, HttpStatus.NOT_FOUND.value()),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenException(ForbiddenException e) {
        return new ResponseEntity<>(
                ErrorResponse.from(e, HttpStatus.FORBIDDEN.value()),
                HttpStatus.FORBIDDEN
        );
    }

    @ExceptionHandler(EmailNaoVerificadoException.class)
    public ResponseEntity<ErrorResponse> handleEmailNaoVerificadoException(EmailNaoVerificadoException e) {
        return new ResponseEntity<>(
                ErrorResponse.from(e, HttpStatus.FORBIDDEN.value()),
                HttpStatus.FORBIDDEN
        );
    }

    @ExceptionHandler(CodigoInvalidoException.class)
    public ResponseEntity<ErrorResponse> handleCodigoInvalidoException(CodigoInvalidoException e) {
        return new ResponseEntity<>(
                ErrorResponse.from(e, HttpStatus.BAD_REQUEST.value()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(TokenInvalidoException.class)
    public ResponseEntity<ErrorResponse> handleTokenInvalidoException(TokenInvalidoException e) {
        return new ResponseEntity<>(
                ErrorResponse.from(e, HttpStatus.UNAUTHORIZED.value()),
                HttpStatus.UNAUTHORIZED
        );
    }

    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(Exception e) {
        return new ResponseEntity<>(
                new ErrorResponse("E-mail ou senha inválidos", HttpStatus.UNAUTHORIZED.value()),
                HttpStatus.UNAUTHORIZED
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        String errors = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        if (errors.isBlank()) {
            errors = e.getBindingResult().getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining("; "));
        }
        return new ResponseEntity<>(
                new ErrorResponse(errors, HttpStatus.BAD_REQUEST.value()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(MethodArgumentTypeMismatchException e) {
        String msg = String.format("Parâmetro '%s' recebeu um valor inválido: '%s'", e.getName(), e.getValue());
        return new ResponseEntity<>(
                new ErrorResponse(msg, HttpStatus.BAD_REQUEST.value()),
                HttpStatus.BAD_REQUEST
        );
    }
}
