package com.zbkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "CampusMerchantHotProductResponse", description = "Campus merchant hot product")
public class CampusMerchantHotProductResponse {

    @ApiModelProperty(value = "Product id")
    private Integer productId;

    @ApiModelProperty(value = "Product name snapshot")
    private String productName;

    @ApiModelProperty(value = "Paid quantity")
    private Integer payNum;
}
