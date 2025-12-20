package com.example.bankcards.exception.handler;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception ex, WebRequest request) {
        ResponseStatus status = AnnotatedElementUtils.findMergedAnnotation(ex.getClass(), ResponseStatus.class);
        var httpStatus = status != null ? status.code() : HttpStatus.INTERNAL_SERVER_ERROR;
        var errorInfo = new ErrorInfo(httpStatus, ex);
        logError(errorInfo);
        return new ResponseEntity<>(errorInfo, new HttpHeaders(), httpStatus);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, WebRequest request) {
        var httpStatus = HttpStatus.BAD_REQUEST;
        var errorInfo = new ErrorInfo(httpStatus, ex, ex.getFieldErrors());
        logError(errorInfo);
        return new ResponseEntity<>(errorInfo, new HttpHeaders(), httpStatus);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, WebRequest request) {
        var httpStatus = HttpStatus.BAD_REQUEST;
        var errorInfo = new ErrorInfo(httpStatus, ex);
        logError(errorInfo);
        return new ResponseEntity<>(errorInfo, new HttpHeaders(), httpStatus);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Object> handleNoResourceFound(NoResourceFoundException ex, WebRequest request) {
        var httpStatus = HttpStatus.NOT_FOUND;
        var errorInfo = new ErrorInfo(httpStatus, ex);
        logError(errorInfo);
        return new ResponseEntity<>(errorInfo, new HttpHeaders(), httpStatus);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Object> handleHandlerMethodValidation(HandlerMethodValidationException ex,
            WebRequest request) {
        var httpStatus = HttpStatus.BAD_REQUEST;
        var errorInfo = new ErrorInfo(httpStatus, ex);
        logError(errorInfo);
        return new ResponseEntity<>(errorInfo, new HttpHeaders(), httpStatus);
    }

    @ExceptionHandler(InternalAuthenticationServiceException.class)
    public ResponseEntity<Object> handleNoResourceFound(InternalAuthenticationServiceException ex, WebRequest request) {
        var httpStatus = HttpStatus.UNAUTHORIZED;
        var errorInfo = new ErrorInfo(httpStatus, ex);
        logError(errorInfo);
        return new ResponseEntity<>(errorInfo, new HttpHeaders(), httpStatus);
    }

    @ExceptionHandler(value = { AuthorizationDeniedException.class, AccessDeniedException.class })
    public ResponseEntity<Object> handleNoResourceFound(Exception ex, WebRequest request) {
        var httpStatus = HttpStatus.FORBIDDEN;
        var errorInfo = new ErrorInfo(httpStatus, ex);
        logError(errorInfo);
        return new ResponseEntity<>(errorInfo, new HttpHeaders(), httpStatus);
    }

    private void logError(ErrorInfo errorInfo) {
        log.error("GlobalExceptionHandler processed the error: {}", errorInfo.toString());
    }

}
