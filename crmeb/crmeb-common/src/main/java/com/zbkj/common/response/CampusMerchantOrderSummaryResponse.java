package com.zbkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@ApiModel(value = "CampusMerchantOrderSummaryResponse", description = "Campus merchant order summary")
public class CampusMerchantOrderSummaryResponse {

    @ApiModelProperty(value = "Today paid campus order count")
    private Integer todayOrderCount;

    @ApiModelProperty(value = "Today paid campus order amount")
    private BigDecimal todayPayAmount;

    @ApiModelProperty(value = "Today hot campus products")
    private List<CampusMerchantHotProductResponse> hotProductList;
}
