package com.zbkj.common.request;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class CampusBuildingRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "School id cannot be blank")
    @Min(value = 1, message = "School id is invalid")
    private Integer schoolId;

    @NotBlank(message = "Building name cannot be blank")
    @Length(max = 64, message = "Building name cannot exceed 64 characters")
    private String buildingName;

    @NotNull(message = "Maximum floor cannot be blank")
    @Min(value = 1, message = "Maximum floor must be greater than zero")
    private Integer maxFloor;

    private Boolean status = true;

    private Integer sort = 0;
}
