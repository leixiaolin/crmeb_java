package com.zbkj.common.response;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class CampusDeliveryQuoteResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer schoolId;

    private Integer buildingId;

    private Integer floorNo;

    private BigDecimal startPrice;

    private BigDecimal floorDeliveryFee;

    private Boolean rainFeeEnabled;

    private BigDecimal rainFee;

    private BigDecimal deliveryFee;
}
