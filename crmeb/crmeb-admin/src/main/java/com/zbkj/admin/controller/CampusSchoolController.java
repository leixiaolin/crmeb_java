package com.zbkj.admin.controller;

import com.zbkj.common.model.campus.CampusSchool;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.CampusSchoolRequest;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.result.CommonResult;
import com.zbkj.service.service.CampusSchoolService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/admin/campus/school")
@Api(tags = "Campus -- School")
public class CampusSchoolController {

    @Autowired
    private CampusSchoolService campusSchoolService;

    @PreAuthorize("hasAuthority('admin:campus:school:list')")
    @ApiOperation(value = "Campus school list")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<CampusSchool>> list(@RequestParam(value = "keywords", defaultValue = "") String keywords,
                                                        @RequestParam(value = "status", required = false) Boolean status,
                                                        @Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(CommonPage.restPage(campusSchoolService.getList(keywords, status, pageParamRequest)));
    }

    @PreAuthorize("hasAuthority('admin:campus:school:save')")
    @ApiOperation(value = "Create campus school")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public CommonResult<String> save(@RequestBody @Validated CampusSchoolRequest request) {
        return campusSchoolService.create(request) ? CommonResult.success() : CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:campus:school:update')")
    @ApiOperation(value = "Update campus school")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public CommonResult<String> update(@RequestParam Integer id, @RequestBody @Validated CampusSchoolRequest request) {
        return campusSchoolService.update(id, request) ? CommonResult.success() : CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:campus:school:update:status')")
    @ApiOperation(value = "Update campus school status")
    @RequestMapping(value = "/update/status", method = RequestMethod.GET)
    public CommonResult<String> updateStatus(@RequestParam Integer id, @RequestParam Boolean status) {
        return campusSchoolService.updateStatus(id, status) ? CommonResult.success() : CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:campus:school:delete')")
    @ApiOperation(value = "Delete campus school")
    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    public CommonResult<String> delete(@RequestParam Integer id) {
        return campusSchoolService.delete(id) ? CommonResult.success() : CommonResult.failed();
    }
}
