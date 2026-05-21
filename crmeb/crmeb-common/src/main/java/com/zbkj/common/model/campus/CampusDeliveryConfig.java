package com.zbkj.common.model.campus;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_campus_delivery_config")
public class CampusDeliveryConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer schoolId;

    private BigDecimal startPrice;

    private Boolean rainFeeEnabled;

    private BigDecimal rainFee;

    @TableLogic
    private Boolean isDel;

    private Date createTime;

    private Date updateTime;
}
