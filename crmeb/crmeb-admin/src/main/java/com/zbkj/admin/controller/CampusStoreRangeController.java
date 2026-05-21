package com.zbkj.admin.controller;

import com.zbkj.common.model.system.SystemStore;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.CampusStoreRangeRequest;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.response.CampusStoreRangeResponse;
import com.zbkj.common.result.CommonResult;
import com.zbkj.service.service.CampusStoreRangeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/admin/campus/store")
@Api(tags = "Campus -- Store Range")
public class CampusStoreRangeController {

    @Autowired
    private CampusStoreRangeService campusStoreRangeService;

    @PreAuthorize("hasAuthority('admin:campus:store:list')")
    @ApiOperation(value = "Campus store range list")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<CampusStoreRangeResponse>> list(@RequestParam Integer schoolId,
                                                                    @RequestParam(value = "status", required = false) Boolean status,
                                                                    @Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(CommonPage.restPage(campusStoreRangeService.getList(schoolId, status, pageParamRequest)));
    }

    @PreAuthorize("hasAuthority('admin:campus:store:list')")
    @ApiOperation(value = "Available campus store candidates")
    @RequestMapping(value = "/candidate/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<SystemStore>> candidates(@RequestParam(value = "keywords", defaultValue = "") String keywords,
                                                             @Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(CommonPage.restPage(campusStoreRangeService.getCandidateStores(keywords, pageParamRequest)));
    }

    @PreAuthorize("hasAuthority('admin:campus:store:save')")
    @ApiOperation(value = "Create campus store range")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public CommonResult<String> save(@RequestBody @Validated CampusStoreRangeRequest request) {
        return campusStoreRangeService.create(request) ? CommonResult.success() : CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:campus:store:update')")
    @ApiOperation(value = "Update campus store range")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public CommonResult<String> update(@RequestParam Integer id, @RequestBody @Validated CampusStoreRangeRequest request) {
        return campusStoreRangeService.update(id, request) ? CommonResult.success() : CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:campus:store:update:status')")
    @ApiOperation(value = "Update campus store range status")
    @RequestMapping(value = "/update/status", method = RequestMethod.GET)
    public CommonResult<String> updateStatus(@RequestParam Integer id, @RequestParam Boolean status) {
        return campusStoreRangeService.updateStatus(id, status) ? CommonResult.success() : CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:campus:store:delete')")
    @ApiOperation(value = "Delete campus store range")
    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    public CommonResult<String> delete(@RequestParam Integer id) {
        return campusStoreRangeService.delete(id) ? CommonResult.success() : CommonResult.failed();
    }
}
