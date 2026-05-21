package com.zbkj.common.request;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class CampusStoreRangeRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "School id cannot be blank")
    @Min(value = 1, message = "School id is invalid")
    private Integer schoolId;

    @NotNull(message = "Store id cannot be blank")
    @Min(value = 1, message = "Store id is invalid")
    private Integer storeId;

    private Boolean status = true;

    private Integer sort = 0;
}
