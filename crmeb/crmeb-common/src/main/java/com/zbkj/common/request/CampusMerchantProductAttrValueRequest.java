package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "CampusMerchantProductAttrValueRequest", description = "Campus merchant product sku update item")
public class CampusMerchantProductAttrValueRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "Sku id", required = true)
    @NotNull(message = "规格ID不能为空")
    private Integer id;

    @ApiModelProperty(value = "Sku price", required = true)
    @NotNull(message = "规格价格不能为空")
    @DecimalMin(value = "0.01", message = "规格价格必须大于0")
    private BigDecimal price;

    @ApiModelProperty(value = "Sku stock", required = true)
    @NotNull(message = "规格库存不能为空")
    @Min(value = 0, message = "规格库存不能小于0")
    private Integer stock;
}
