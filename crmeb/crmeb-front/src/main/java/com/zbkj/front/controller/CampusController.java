package com.zbkj.front.controller;

import com.zbkj.common.model.campus.CampusAddress;
import com.zbkj.common.model.campus.CampusBuilding;
import com.zbkj.common.model.campus.CampusSchool;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.CampusAddressRequest;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.request.UserAddressDelRequest;
import com.zbkj.common.response.CampusDeliveryQuoteResponse;
import com.zbkj.common.response.CampusSearchResponse;
import com.zbkj.common.response.CampusStoreRangeResponse;
import com.zbkj.common.response.StoreProductReplyResponse;
import com.zbkj.common.result.CommonResult;
import com.zbkj.service.service.CampusAddressService;
import com.zbkj.service.service.CampusBuildingService;
import com.zbkj.service.service.CampusDeliveryService;
import com.zbkj.service.service.CampusSchoolService;
import com.zbkj.service.service.CampusStoreRangeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/front/campus")
@Api(tags = "Campus")
public class CampusController {

    @Autowired
    private CampusSchoolService campusSchoolService;

    @Autowired
    private CampusBuildingService campusBuildingService;

    @Autowired
    private CampusAddressService campusAddressService;

    @Autowired
    private CampusDeliveryService campusDeliveryService;

    @Autowired
    private CampusStoreRangeService campusStoreRangeService;

    @ApiOperation(value = "Enabled campus schools")
    @RequestMapping(value = "/school/list", method = RequestMethod.GET)
    public CommonResult<List<CampusSchool>> schoolList() {
        return CommonResult.success(campusSchoolService.getEnabledList());
    }

    @ApiOperation(value = "Enabled campus buildings")
    @RequestMapping(value = "/building/list", method = RequestMethod.GET)
    public CommonResult<List<CampusBuilding>> buildingList(@RequestParam Integer schoolId) {
        return CommonResult.success(campusBuildingService.getEnabledList(schoolId));
    }

    @ApiOperation(value = "Campus address list")
    @RequestMapping(value = "/address/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<CampusAddress>> addressList(@Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(CommonPage.restPage(campusAddressService.getList(pageParamRequest)));
    }

    @ApiOperation(value = "Save campus address")
    @RequestMapping(value = "/address/edit", method = RequestMethod.POST)
    public CommonResult<CampusAddress> saveAddress(@RequestBody @Validated CampusAddressRequest request) {
        return CommonResult.success(campusAddressService.saveAddress(request));
    }

    @ApiOperation(value = "Campus address detail")
    @RequestMapping(value = "/address/detail/{id}", method = RequestMethod.GET)
    public CommonResult<CampusAddress> addressDetail(@PathVariable Integer id) {
        return CommonResult.success(campusAddressService.getDetail(id));
    }

    @ApiOperation(value = "Delete campus address")
    @RequestMapping(value = "/address/del", method = RequestMethod.POST)
    public CommonResult<String> deleteAddress(@RequestBody UserAddressDelRequest request) {
        return campusAddressService.delete(request.getId()) ? CommonResult.success() : CommonResult.failed();
    }

    @ApiOperation(value = "Default campus address")
    @RequestMapping(value = "/address/default", method = RequestMethod.GET)
    public CommonResult<CampusAddress> defaultAddress() {
        return CommonResult.success(campusAddressService.getDefault());
    }

    @ApiOperation(value = "Set default campus address")
    @RequestMapping(value = "/address/default/set", method = RequestMethod.POST)
    public CommonResult<String> setDefaultAddress(@RequestBody UserAddressDelRequest request) {
        return campusAddressService.setDefault(request.getId()) ? CommonResult.success() : CommonResult.failed();
    }

    @ApiOperation(value = "Campus delivery quote")
    @RequestMapping(value = "/delivery/quote", method = RequestMethod.GET)
    public CommonResult<CampusDeliveryQuoteResponse> deliveryQuote(@RequestParam Integer buildingId,
                                                                   @RequestParam Integer floorNo) {
        return CommonResult.success(campusDeliveryService.quote(buildingId, floorNo));
    }

    @ApiOperation(value = "Enabled campus stores")
    @RequestMapping(value = "/store/list", method = RequestMethod.GET)
    public CommonResult<List<CampusStoreRangeResponse>> storeList(@RequestParam Integer schoolId) {
        return CommonResult.success(campusStoreRangeService.getEnabledList(schoolId));
    }

    @ApiOperation(value = "Campus store reply list")
    @RequestMapping(value = "/store/reply/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<StoreProductReplyResponse>> storeReplyList(@RequestParam Integer schoolId,
                                                                              @RequestParam Integer storeId,
                                                                              @Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(CommonPage.restPage(campusStoreRangeService.getReplyList(schoolId, storeId, pageParamRequest)));
    }

    @ApiOperation(value = "Campus store and product search")
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public CommonResult<CampusSearchResponse> search(@RequestParam Integer schoolId,
                                                    @RequestParam String keyword,
                                                    @Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(campusStoreRangeService.search(schoolId, keyword, pageParamRequest));
    }
}
