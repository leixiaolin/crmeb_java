package com.zbkj.admin.controller;

import com.zbkj.common.model.campus.CampusBuilding;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.CampusBuildingRequest;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.result.CommonResult;
import com.zbkj.service.service.CampusBuildingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/admin/campus/building")
@Api(tags = "Campus -- Building")
public class CampusBuildingController {

    @Autowired
    private CampusBuildingService campusBuildingService;

    @PreAuthorize("hasAuthority('admin:campus:building:list')")
    @ApiOperation(value = "Campus building list")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<CampusBuilding>> list(@RequestParam(value = "schoolId", defaultValue = "0") Integer schoolId,
                                                          @RequestParam(value = "status", required = false) Boolean status,
                                                          @Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(CommonPage.restPage(campusBuildingService.getList(schoolId, status, pageParamRequest)));
    }

    @PreAuthorize("hasAuthority('admin:campus:building:save')")
    @ApiOperation(value = "Create campus building")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public CommonResult<String> save(@RequestBody @Validated CampusBuildingRequest request) {
        return campusBuildingService.create(request) ? CommonResult.success() : CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:campus:building:update')")
    @ApiOperation(value = "Update campus building")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public CommonResult<String> update(@RequestParam Integer id, @RequestBody @Validated CampusBuildingRequest request) {
        return campusBuildingService.update(id, request) ? CommonResult.success() : CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:campus:building:update:status')")
    @ApiOperation(value = "Update campus building status")
    @RequestMapping(value = "/update/status", method = RequestMethod.GET)
    public CommonResult<String> updateStatus(@RequestParam Integer id, @RequestParam Boolean status) {
        return campusBuildingService.updateStatus(id, status) ? CommonResult.success() : CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:campus:building:delete')")
    @ApiOperation(value = "Delete campus building")
    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    public CommonResult<String> delete(@RequestParam Integer id) {
        return campusBuildingService.delete(id) ? CommonResult.success() : CommonResult.failed();
    }
}
