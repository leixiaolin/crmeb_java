package com.zbkj.admin.controller;

import com.zbkj.common.model.campus.CampusDeliveryConfig;
import com.zbkj.common.model.campus.CampusFloorDeliveryFee;
import com.zbkj.common.request.CampusDeliveryConfigRequest;
import com.zbkj.common.request.CampusFloorDeliveryFeeRequest;
import com.zbkj.common.result.CommonResult;
import com.zbkj.service.service.CampusDeliveryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/admin/campus/delivery")
@Api(tags = "Campus -- Delivery")
public class CampusDeliveryController {

    @Autowired
    private CampusDeliveryService campusDeliveryService;

    @PreAuthorize("hasAuthority('admin:campus:delivery:config')")
    @ApiOperation(value = "Campus delivery config")
    @RequestMapping(value = "/config", method = RequestMethod.GET)
    public CommonResult<CampusDeliveryConfig> config(@RequestParam Integer schoolId) {
        return CommonResult.success(campusDeliveryService.getConfig(schoolId));
    }

    @PreAuthorize("hasAuthority('admin:campus:delivery:config:save')")
    @ApiOperation(value = "Save campus delivery config")
    @RequestMapping(value = "/config/save", method = RequestMethod.POST)
    public CommonResult<String> saveConfig(@RequestBody @Validated CampusDeliveryConfigRequest request) {
        return campusDeliveryService.saveConfig(request) ? CommonResult.success() : CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:campus:delivery:fee:list')")
    @ApiOperation(value = "Floor delivery fee list")
    @RequestMapping(value = "/fee/list", method = RequestMethod.GET)
    public CommonResult<List<CampusFloorDeliveryFee>> feeList(@RequestParam Integer buildingId) {
        return CommonResult.success(campusDeliveryService.getFeeList(buildingId));
    }

    @PreAuthorize("hasAuthority('admin:campus:delivery:fee:save')")
    @ApiOperation(value = "Save floor delivery fee")
    @RequestMapping(value = "/fee/save", method = RequestMethod.POST)
    public CommonResult<String> saveFee(@RequestBody @Validated CampusFloorDeliveryFeeRequest request) {
        return campusDeliveryService.saveFee(request) ? CommonResult.success() : CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:campus:delivery:fee:update')")
    @ApiOperation(value = "Update floor delivery fee")
    @RequestMapping(value = "/fee/update", method = RequestMethod.POST)
    public CommonResult<String> updateFee(@RequestParam Integer id,
                                          @RequestBody @Validated CampusFloorDeliveryFeeRequest request) {
        return campusDeliveryService.updateFee(id, request) ? CommonResult.success() : CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:campus:delivery:fee:delete')")
    @ApiOperation(value = "Delete floor delivery fee")
    @RequestMapping(value = "/fee/delete", method = RequestMethod.GET)
    public CommonResult<String> deleteFee(@RequestParam Integer id) {
        return campusDeliveryService.deleteFee(id) ? CommonResult.success() : CommonResult.failed();
    }
}
