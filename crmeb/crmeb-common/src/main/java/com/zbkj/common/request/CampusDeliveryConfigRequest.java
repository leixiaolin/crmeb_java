package com.zbkj.common.request;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class CampusDeliveryConfigRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "School id cannot be blank")
    @Min(value = 1, message = "School id is invalid")
    private Integer schoolId;

    @NotNull(message = "Start price cannot be blank")
    @DecimalMin(value = "0.00", message = "Start price cannot be negative")
    private BigDecimal startPrice;

    private Boolean rainFeeEnabled = false;

    @NotNull(message = "Rain fee cannot be blank")
    @DecimalMin(value = "0.00", message = "Rain fee cannot be negative")
    private BigDecimal rainFee;
}
