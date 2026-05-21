package com.zbkj.common.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class CampusSchoolRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "School name", required = true)
    @NotBlank(message = "School name cannot be blank")
    @Length(max = 64, message = "School name cannot exceed 64 characters")
    private String schoolName;

    @ApiModelProperty(value = "Campus name", required = true)
    @NotBlank(message = "Campus name cannot be blank")
    @Length(max = 64, message = "Campus name cannot exceed 64 characters")
    private String campusName;

    private Boolean status = true;

    private Integer sort = 0;
}
