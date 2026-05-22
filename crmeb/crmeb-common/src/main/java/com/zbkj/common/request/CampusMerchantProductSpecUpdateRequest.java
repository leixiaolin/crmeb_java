package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "CampusMerchantProductSpecUpdateRequest", description = "Campus merchant product sku updates")
public class CampusMerchantProductSpecUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Valid
    @NotEmpty(message = "商品规格不能为空")
    @ApiModelProperty(value = "Sku updates", required = true)
    private List<CampusMerchantProductAttrValueRequest> attrValueList;
}
