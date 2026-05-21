package com.zbkj.common.model.campus;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_campus_store_range")
@ApiModel(value = "CampusStoreRange", description = "Campus store service range")
public class CampusStoreRange implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "Campus school id")
    private Integer schoolId;

    @ApiModelProperty(value = "System store id")
    private Integer storeId;

    @ApiModelProperty(value = "Enabled status")
    private Boolean status;

    @ApiModelProperty(value = "Sort value")
    private Integer sort;

    @TableLogic
    @ApiModelProperty(value = "Deleted flag")
    private Boolean isDel;

    private Date createTime;

    private Date updateTime;
}
