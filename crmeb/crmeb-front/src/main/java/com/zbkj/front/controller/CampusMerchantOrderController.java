package com.zbkj.front.controller;

import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.request.StoreOrderRefundRequest;
import com.zbkj.common.response.CampusMerchantOrderSummaryResponse;
import com.zbkj.common.response.OrderDetailResponse;
import com.zbkj.common.response.StoreOrderDetailInfoResponse;
import com.zbkj.common.result.CommonResult;
import com.zbkj.service.service.CampusMerchantOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/front/campus/merchant/order")
@Api(tags = "Campus merchant orders")
public class CampusMerchantOrderController {

    @Autowired
    private CampusMerchantOrderService campusMerchantOrderService;

    @ApiOperation(value = "Campus merchant access")
    @RequestMapping(value = "/access", method = RequestMethod.GET)
    public CommonResult<Boolean> access() {
        return CommonResult.success(campusMerchantOrderService.hasAccess());
    }

    @ApiOperation(value = "Campus merchant today order summary")
    @RequestMapping(value = "/summary/today", method = RequestMethod.GET)
    public CommonResult<CampusMerchantOrderSummaryResponse> todaySummary() {
        return CommonResult.success(campusMerchantOrderService.getTodaySummary());
    }

    @ApiOperation(value = "Campus merchant order list")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<OrderDetailResponse>> list(@RequestParam(required = false) Integer campusStatus,
                                                              @Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(CommonPage.restPage(campusMerchantOrderService.getList(campusStatus, pageParamRequest)));
    }

    @ApiOperation(value = "Campus merchant refund application list")
    @RequestMapping(value = "/refund/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<OrderDetailResponse>> refundList(@Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(CommonPage.restPage(campusMerchantOrderService.getRefundList(pageParamRequest)));
    }

    @ApiOperation(value = "Campus merchant order detail")
    @RequestMapping(value = "/detail/{orderNo}", method = RequestMethod.GET)
    public CommonResult<StoreOrderDetailInfoResponse> detail(@PathVariable String orderNo) {
        return CommonResult.success(campusMerchantOrderService.detail(orderNo));
    }

    @ApiOperation(value = "Campus merchant accept order")
    @RequestMapping(value = "/accept/{orderNo}", method = RequestMethod.POST)
    public CommonResult<Boolean> accept(@PathVariable String orderNo) {
        return CommonResult.success(campusMerchantOrderService.accept(orderNo));
    }

    @ApiOperation(value = "Campus merchant reject order")
    @RequestMapping(value = "/reject/{orderNo}", method = RequestMethod.POST)
    public CommonResult<Boolean> reject(@PathVariable String orderNo) {
        return CommonResult.success(campusMerchantOrderService.reject(orderNo));
    }

    @ApiOperation(value = "Campus merchant refund order")
    @RequestMapping(value = "/refund", method = RequestMethod.POST)
    public CommonResult<Boolean> refund(@RequestBody @Validated StoreOrderRefundRequest request) {
        return CommonResult.success(campusMerchantOrderService.refund(request));
    }

    @ApiOperation(value = "Campus merchant refuse refund")
    @RequestMapping(value = "/refund/refuse/{orderNo}", method = RequestMethod.POST)
    public CommonResult<Boolean> refundRefuse(@PathVariable String orderNo, @RequestParam String reason) {
        return CommonResult.success(campusMerchantOrderService.refundRefuse(orderNo, reason));
    }
}
