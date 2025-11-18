//package com.intern.gateway.exception;
//
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//import org.springframework.web.reactive.function.client.WebClientResponseException;
//
//@RestControllerAdvice
//public class GlobalExceptionHandler {
//
//    @ExceptionHandler(WebClientResponseException.class)
//    public ResponseEntity<String> handleWebClientResponseException(WebClientResponseException ex) {
//        return ResponseEntity.status(ex.getStatusCode())
//                             .body(ex.getResponseBodyAsString());
//    }
//
//}
