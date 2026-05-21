package com.zbkj.common.model.campus;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_campus_address")
public class CampusAddress implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer uid;

    private Integer schoolId;

    private Integer buildingId;

    private String schoolName;

    private String campusName;

    private String buildingName;

    private Integer floorNo;

    private String roomNo;

    private String contactName;

    private String contactPhone;

    private Boolean isDefault;

    private String remark;

    @TableLogic
    private Boolean isDel;

    private Date createTime;

    private Date updateTime;
}
