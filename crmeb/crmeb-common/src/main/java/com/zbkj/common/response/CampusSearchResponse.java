package com.zbkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@ApiModel(value = "CampusSearchResponse", description = "Campus store and product search response")
public class CampusSearchResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "Campus store search result")
    private List<CampusStoreRangeResponse> storeList;

    @ApiModelProperty(value = "Campus product search result")
    private List<StoreProductResponse> productList;
}
