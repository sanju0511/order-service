package com.ecommerce.orderservice.service.serviceImpl;

import com.ecommerce.orderservice.client.InventoryFeignClient;
import com.ecommerce.orderservice.dto.CreateOrderRequest;
import com.ecommerce.orderservice.dto.OrderResponse;
import com.ecommerce.orderservice.dto.StockOperationRequest;
import com.ecommerce.orderservice.entities.Order;
import com.ecommerce.orderservice.entities.OrderStatus;
import com.ecommerce.orderservice.exception.InvalidOrderStateException;
import com.ecommerce.orderservice.exception.OrderNotFoundException;
import com.ecommerce.orderservice.mapper.OrderMapperClass;
import com.ecommerce.orderservice.repository.OrderServiceRepository;
import com.ecommerce.orderservice.service.OrderService;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@org.springframework.stereotype.Service
public class OrderServiceImpl implements OrderService {
    private final OrderServiceRepository repository;
    private final InventoryFeignClient client;
    public OrderServiceImpl(OrderServiceRepository repository, InventoryFeignClient client) {
        this.repository = repository;
        this.client = client;
    }
    @Override
    public OrderResponse createOrder(CreateOrderRequest request) {
        StockOperationRequest quantity=new StockOperationRequest(request.getQuantity());
        client.reserveStock(request.getProductId(),quantity);
        Order order=new Order();
        order.setProductId(request.getProductId());
        order.setQuantity(request.getQuantity());
        order.setUnitPrice(request.getUnitPrice());
        order.setTotalAmount(request.getUnitPrice().multiply(BigDecimal.valueOf(request.getQuantity())));
        order.setOrderStatus(OrderStatus.CREATED);
        repository.save(order);
        return OrderMapperClass.toDto(order);
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
    @Transactional(readOnly = true)
    public OrderResponse updateOrder(Long id, CreateOrderRequest request) {
        OrderResponse order=getOrderById(id);
        if(order.getStatus()!=OrderStatus.CREATED){
            throw new InvalidOrderStateException("Only orders with created are updated:"+id);
        }
        Order order1=new Order(request.getProductId(),request.getQuantity(),request.getUnitPrice());
        return OrderMapperClass.toDto(order1);
    }


    @Override
    public OrderResponse updateStatus(Long id, OrderStatus status) {
        Order order=repository.findById(id).orElseThrow(()->
                new OrderNotFoundException("Order not Found :"+id));
        order.setOrderStatus(status);
        return OrderMapperClass.toDto(repository.save(order));
    }

    @Override
    public void deleteOrder(Long id) {
        OrderResponse order=getOrderById(id);
        if(order.getStatus()!=OrderStatus.CREATED || order.getStatus()!=OrderStatus.CANCELLED){
            throw new InvalidOrderStateException("Only Created or Cancelled Order can be deleted");
        }
        repository.deleteById(id);
    }
}
