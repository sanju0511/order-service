package com.ecommerce.orderservice.client.exception;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class InventoryFeignErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        int statusCode=response.status();
        HttpStatus status=HttpStatus.resolve(statusCode);
        if(status==null){
            status=HttpStatus.BAD_GATEWAY;
        }
        return new InventoryServiceException("Inventory Service returned HTTP : "
                + statusCode + " for operation:" + methodKey ,status);
    }
}
