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
@TableName("eb_campus_school")
@ApiModel(value = "CampusSchool", description = "Campus school or campus area")
public class CampusSchool implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "School name")
    private String schoolName;

    @ApiModelProperty(value = "Campus name")
    private String campusName;

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
