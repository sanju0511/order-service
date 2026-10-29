package com.ecommerce.orderservice.dto;

import java.time.LocalDate;
import java.util.Map;

public class ErrorResponse {
    private String message;
    private LocalDate timeStamp;
    private String error;
    private  String path;
    private int status;
    private Map<String,String> validationErrors;

    public ErrorResponse() {
    }

    public ErrorResponse(String message, LocalDate timeStamp, String error, String path, int status, Map<String, String> validationErrors) {
        this.message = message;
        this.timeStamp = timeStamp;
        this.error = error;
        this.path = path;
        this.status = status;
        this.validationErrors = validationErrors;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDate getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(LocalDate timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Map<String, String> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(Map<String, String> validationErrors) {
        this.validationErrors = validationErrors;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
