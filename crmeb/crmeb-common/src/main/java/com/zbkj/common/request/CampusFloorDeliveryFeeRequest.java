package com.zbkj.common.request;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class CampusFloorDeliveryFeeRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "Building id cannot be blank")
    @Min(value = 1, message = "Building id is invalid")
    private Integer buildingId;

    @NotNull(message = "Floor cannot be blank")
    @Min(value = 1, message = "Floor must be greater than zero")
    private Integer floorNo;

    @NotNull(message = "Delivery fee cannot be blank")
    @DecimalMin(value = "0.00", message = "Delivery fee cannot be negative")
    private BigDecimal deliveryFee;
}
