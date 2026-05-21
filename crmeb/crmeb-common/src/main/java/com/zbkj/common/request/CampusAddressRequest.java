package com.zbkj.common.request;

import com.zbkj.common.constants.RegularConstants;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

@Data
public class CampusAddressRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    @NotNull(message = "School id cannot be blank")
    @Min(value = 1, message = "School id is invalid")
    private Integer schoolId;

    @NotNull(message = "Building id cannot be blank")
    @Min(value = 1, message = "Building id is invalid")
    private Integer buildingId;

    @NotNull(message = "Floor cannot be blank")
    @Min(value = 1, message = "Floor must be greater than zero")
    private Integer floorNo;

    @NotBlank(message = "Room number cannot be blank")
    @Length(max = 32, message = "Room number cannot exceed 32 characters")
    private String roomNo;

    @NotBlank(message = "Contact name cannot be blank")
    @Length(max = 32, message = "Contact name cannot exceed 32 characters")
    private String contactName;

    @NotBlank(message = "Contact phone cannot be blank")
    @Pattern(regexp = RegularConstants.PHONE_TWO, message = "Contact phone is invalid")
    private String contactPhone;

    private Boolean isDefault = false;

    @Length(max = 128, message = "Remark cannot exceed 128 characters")
    private String remark;
}
