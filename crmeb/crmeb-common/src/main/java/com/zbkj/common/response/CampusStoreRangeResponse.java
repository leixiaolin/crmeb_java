package com.zbkj.common.response;

import com.zbkj.common.model.system.SystemStore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@ApiModel(value = "CampusStoreRangeResponse", description = "Campus store service range response")
public class CampusStoreRangeResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private Integer schoolId;

    private Integer storeId;

    private Boolean status;

    private Integer sort;

    private Date createTime;

    private Date updateTime;

    @ApiModelProperty(value = "Campus store detail")
    private SystemStore systemStore;

    @ApiModelProperty(value = "Campus store is inside business time")
    private Boolean openNow;

    @ApiModelProperty(value = "Campus store rating aggregated from product replies")
    private BigDecimal replyScore;

    @ApiModelProperty(value = "Campus store reply count")
    private Integer replyCount;

    @ApiModelProperty(value = "Campus school delivery start price")
    private BigDecimal startPrice;
}
