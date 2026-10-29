package com.ecommerce.orderservice.dto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class CreateOrderRequest {
    @NotNull(message = "ProductId is required")
    @Positive(message = "ProductId cannot be Zero")
    private Long productId;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity cannot be Zero")
    private Integer quantity;

    @NotNull(message = "UnitPrice is required")
    @DecimalMin(value="0.01",message = "UnitPrice must be atleast 0.01")
    private BigDecimal unitPrice;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }
}
