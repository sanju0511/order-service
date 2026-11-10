package com.ecommerce.orderservice.exception;

import com.ecommerce.orderservice.client.exception.InventoryServiceException;
import com.ecommerce.orderservice.dto.ErrorResponse;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.tomcat.util.json.JSONParser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    private final ObjectMapper objectMapper;

    public GlobalExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> methodArgumentNotValidException(MethodArgumentNotValidException ex, HttpServletRequest request){
        ErrorResponse errorResponse=new ErrorResponse();
        Map<String,String> errors=new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fieldError -> {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        });

     return  buildResponse( HttpStatus.BAD_REQUEST,"Validation Failed",request,errors);
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> orderNotFoundException(OrderNotFoundException exception,HttpServletRequest request){

        return buildResponse( HttpStatus.NOT_FOUND,exception.getMessage(),request,Map.of());
    }
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponse> feignException(FeignException exception,HttpServletRequest request){
         String responseBody=exception.contentUTF8();
        return buildResponse(HttpStatus.valueOf(exception.status()),responseBody,request,Map.of());
    }

    @ExceptionHandler(InvalidOrderStateException.class)
    public ResponseEntity<ErrorResponse> invalidOrderStateException(InvalidOrderStateException exception,
                                                                    HttpServletRequest request){
        return buildResponse( HttpStatus.CONFLICT,exception.getMessage(),request,Map.of());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException ex,HttpServletRequest request) {

        return buildResponse( HttpStatus.METHOD_NOT_ALLOWED,ex.getMessage(),request,Map.of());
    }



    @ExceptionHandler(InventoryServiceException.class)
    public ResponseEntity<ErrorResponse> invalidOrderStateException(InventoryServiceException exception,
                                                                    HttpServletRequest request){

        String message = exception.getMessage();

        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            message = jsonNode.path("message").asText(message);

        } catch (Exception ignored) {

        }

        return buildResponse(HttpStatus.valueOf(exception.getStatus().value()),message,request,Map.of());
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> exception(Exception ex,HttpServletRequest request){
        return buildResponse( HttpStatus.INTERNAL_SERVER_ERROR,ex.getMessage(),request,Map.of());
    }
    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status,String message
            ,HttpServletRequest request,Map<String,String> validationErrors){
ErrorResponse errorResponse=new ErrorResponse(message,LocalDate.now()
        ,status.getReasonPhrase(),request.getRequestURI()
        ,status.value(),validationErrors);
        return new ResponseEntity<>(errorResponse,status);
    }
}