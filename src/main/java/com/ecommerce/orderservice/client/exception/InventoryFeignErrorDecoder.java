
        package com.ecommerce.orderservice.client.exception;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class InventoryFeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {

        int statusCode = response.status();

        String responseBody = "";

        if (response.body() != null) {
            try {
                responseBody = new String(
                        response.body().asInputStream().readAllBytes(),
                        StandardCharsets.UTF_8
                );
            } catch (IOException e) {
                responseBody = "Unable to read Inventory Service error response";
            }
        }

        HttpStatus status = HttpStatus.resolve(statusCode);

        if (status == null) {
            status = HttpStatus.BAD_GATEWAY;
        }

        return new InventoryServiceException(
                responseBody.isBlank()
                        ? "Inventory Service request failed with HTTP " + statusCode
                        : responseBody,
                status
        );
    }
}