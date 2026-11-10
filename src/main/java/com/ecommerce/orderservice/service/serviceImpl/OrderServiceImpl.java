package com.ecommerce.orderservice.service.serviceImpl;

import com.ecommerce.orderservice.client.InventoryFeignClient;
import com.ecommerce.orderservice.dto.CreateOrderRequest;
import com.ecommerce.orderservice.dto.OrderResponse;
import com.ecommerce.orderservice.dto.StockOperationRequest;
import com.ecommerce.orderservice.entities.IdempotencyRecord;
import com.ecommerce.orderservice.entities.Order;
import com.ecommerce.orderservice.entities.OrderStatus;
import com.ecommerce.orderservice.exception.InvalidOrderStateException;
import com.ecommerce.orderservice.exception.OrderNotFoundException;
import com.ecommerce.orderservice.mapper.OrderMapperClass;
import com.ecommerce.orderservice.repository.IdempotencyRepository;
import com.ecommerce.orderservice.repository.OrderServiceRepository;
import com.ecommerce.orderservice.service.OrderService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;

@org.springframework.stereotype.Service
public class OrderServiceImpl implements OrderService {
    private final OrderServiceRepository repository;
    private final InventoryFeignClient client;
    private final OrderSagaOrchestrator orchestrator;
    private final IdempotencyRepository idempotencyRepository;
    
    public OrderServiceImpl(OrderServiceRepository repository, InventoryFeignClient client, OrderSagaOrchestrator orchestrator, IdempotencyRepository idempotencyRepository) {
        this.repository = repository;
        this.client = client;
        this.orchestrator = orchestrator;
        this.idempotencyRepository = idempotencyRepository;
    }
    @Override
    public OrderResponse createOrder(CreateOrderRequest request,String idempotencyKey) {
        if(idempotencyKey==null || idempotencyKey.isBlank()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Idempotency-Key is missing in header");
        }

        var existingKey=idempotencyRepository.findByIdempotencyKey(idempotencyKey);

        if(existingKey.isPresent()){

            IdempotencyRecord record=existingKey.get();

            if(!Objects.equals(record.getRequestHash(), generateRequestHash(request))){
                throw new ResponseStatusException(HttpStatus.CONFLICT,"Key exist with different request");
            }

             if("COMPLETED".equals(record.getStatus())){
                 Order order=repository.findById(record.getOrderId()).orElseThrow(()->
                         new IllegalStateException("Previously created order is not found:"+record.getOrderId()));

                 return OrderMapperClass.toDto(order);
             }
             throw new ResponseStatusException(HttpStatus.CONFLICT,"This request is already in processed");

        }
        IdempotencyRecord idempotencyRecord=new IdempotencyRecord();
        idempotencyRecord.setIdempotencyKey(idempotencyKey);
        idempotencyRecord.setStatus("PROCESSING");
        idempotencyRecord.setRequestHash(generateRequestHash(request));
        try {
            idempotencyRepository.saveAndFlush(idempotencyRecord);
        }catch (DataIntegrityViolationException ex){
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Another request is already using this key");
        }

        OrderResponse orderResponse=orchestrator.createOrder(request);

        idempotencyRecord.setOrderId(orderResponse.getId());
        idempotencyRecord.setStatus("COMPLETED");


        idempotencyRepository.save(idempotencyRecord);
        return orderResponse;

    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order=repository.findById(id)
                .orElseThrow(()->new OrderNotFoundException("Order not found: "+id));
        return OrderMapperClass.toDto(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return repository.findAll().stream().map(OrderMapperClass::toDto).toList();
    }

    @Override
    @Transactional()
    public OrderResponse updateOrder(Long id, CreateOrderRequest request) {
        OrderResponse order=getOrderById(id);
        if(order.getStatus()!=OrderStatus.CREATED){
            throw new InvalidOrderStateException("Only orders with created are updated:"+id);
        }
        Order order1=new Order(request.getProductId(),request.getQuantity(),request.getUnitPrice());
        return OrderMapperClass.toDto(order1);
    }

    @Override
    public OrderResponse cancelOrder(Long id){
        Order order=repository.findById(id).orElseThrow(()->
                new OrderNotFoundException("Order not Found :"+id));
        if(order.getOrderStatus()!=OrderStatus.CREATED){
            throw new InvalidOrderStateException("Only CREATED orders can be cancelled :"+id);
        }
        client.releaseStock(order.getProductId(),order.getId(), new StockOperationRequest(order.getQuantity()));
        order.setOrderStatus(OrderStatus.CANCELLED);
        return OrderMapperClass.toDto(repository.save(order));
   }





    @Override
    public OrderResponse updateStatus(Long id, OrderStatus status) {
        Order order=repository.findById(id).orElseThrow(()->
                new OrderNotFoundException("Order not Found :"+id));
        if(status==OrderStatus.CONFIRMED){
            client.confirmStock(order.getProductId(),order.getId(),new StockOperationRequest(order.getQuantity()));
        }else if(status==OrderStatus.CANCELLED){
            client.releaseStock(order.getProductId(), order.getId(), new StockOperationRequest(order.getQuantity()));
        }else {
            throw new InvalidOrderStateException("Invalid status change: "+id);
        }
        order.setOrderStatus(status);
        return OrderMapperClass.toDto(repository.save(order));
    }

    @Override
    public void deleteOrder(Long id) {
        OrderResponse order=getOrderById(id);
        if(order.getStatus()!=OrderStatus.CREATED && order.getStatus()!=OrderStatus.CANCELLED){
            throw new InvalidOrderStateException("Only Created or Cancelled Order can be deleted");
        }
        repository.deleteById(id);
    }


    private String generateRequestHash(CreateOrderRequest request){
        String requestData=request.getProductId() +"|"+ request.getQuantity()+"|"
                +request.getUnitPrice().stripTrailingZeros().toPlainString();
        try {
            MessageDigest digest=MessageDigest.getInstance("SHA-256");
            byte[] hash= digest.digest(requestData.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm unavailable",e);
        }
    }
}
