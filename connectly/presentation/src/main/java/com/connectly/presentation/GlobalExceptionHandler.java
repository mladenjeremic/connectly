package com.connectly.presentation;

import com.connectly.domain.exception.*;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import java.time.Instant;

@RestControllerAdvice
class GlobalExceptionHandler {

    record ErrorResponse(int status, String error, String message, Instant timestamp) {}

    private ResponseEntity<ErrorResponse> body(HttpStatus s, String msg) {
        return ResponseEntity.status(s).body(new ErrorResponse(s.value(), s.getReasonPhrase(), msg, Instant.now()));
    }

    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ErrorResponse> notFound(NotFoundException e) { return body(HttpStatus.NOT_FOUND, e.getMessage()); }

    @ExceptionHandler(ConflictException.class)
    ResponseEntity<ErrorResponse> conflict(ConflictException e) { return body(HttpStatus.CONFLICT, e.getMessage()); }

    @ExceptionHandler(InvalidCredentialsException.class)
    ResponseEntity<ErrorResponse> badCreds(InvalidCredentialsException e) { return body(HttpStatus.UNAUTHORIZED, e.getMessage()); }

    @ExceptionHandler(AccountBlockedException.class)
    ResponseEntity<ErrorResponse> blocked(AccountBlockedException e) { return body(HttpStatus.FORBIDDEN, e.getMessage()); }

    @ExceptionHandler(ForbiddenException.class)
    ResponseEntity<ErrorResponse> forbidden(ForbiddenException e) { return body(HttpStatus.FORBIDDEN, e.getMessage()); }

    // DomainException je roditelj NotFound/Forbidden; Spring bira najpreciznije podudaranje
    @ExceptionHandler(DomainException.class)
    ResponseEntity<ErrorResponse> domain(DomainException e) { return body(HttpStatus.BAD_REQUEST, e.getMessage()); }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> invalidBody(MethodArgumentNotValidException e) {
        var msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage()).toList().toString();
        return body(HttpStatus.BAD_REQUEST, msg);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    ResponseEntity<ErrorResponse> invalidParam(HandlerMethodValidationException e) {
        return body(HttpStatus.BAD_REQUEST, "Neispravni parametri zahteva.");
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class, MissingServletRequestPartException.class})
    ResponseEntity<ErrorResponse> badRequest(Exception e) { return body(HttpStatus.BAD_REQUEST, "Neispravan zahtev."); }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<ErrorResponse> tooLarge(MaxUploadSizeExceededException e) {
        return body(HttpStatus.PAYLOAD_TOO_LARGE, "Fajl je prevelik (najviše 5 MB po fotografiji).");
    }
}
