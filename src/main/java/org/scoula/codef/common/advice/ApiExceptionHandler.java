package org.scoula.codef.common.advice;

import org.scoula.codef.common.exception.CodefApiException;
import org.scoula.codef.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(CodefApiException.class)
    public ResponseEntity<ErrorResponse> handleCodefException(CodefApiException ex) {
        System.out.println("🔥 [ApiExceptionHandler] CodefApiException 처리됨: " + ex.getErrorMessage());
        ErrorResponse response = new ErrorResponse(false, ex.getErrorCode(), ex.getErrorMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        System.out.println("🔥 [ApiExceptionHandler] General Exception 처리됨: " + ex.getMessage());
        ErrorResponse response = new ErrorResponse(false, "INTERNAL_ERROR", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}