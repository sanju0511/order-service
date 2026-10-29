package com.ecommerce.orderservice.exception;

import com.ecommerce.orderservice.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

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

    @ExceptionHandler(InvalidOrderStateException.class)
    public ResponseEntity<ErrorResponse> invalidOrderStateException(InvalidOrderStateException exception,
                                                                    HttpServletRequest request){
        return buildResponse( HttpStatus.CONFLICT,exception.getMessage(),request,Map.of());
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
        return new ResponseEntity<>(errorResponse,HttpStatus.BAD_REQUEST);
    }
}