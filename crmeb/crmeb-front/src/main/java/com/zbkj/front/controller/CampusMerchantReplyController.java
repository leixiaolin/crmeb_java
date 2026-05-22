package com.zbkj.front.controller;

import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.request.StoreProductReplyCommentRequest;
import com.zbkj.common.response.StoreProductReplyResponse;
import com.zbkj.common.result.CommonResult;
import com.zbkj.service.service.CampusMerchantReplyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/front/campus/merchant/reply")
@Api(tags = "Campus merchant replies")
public class CampusMerchantReplyController {

    @Autowired
    private CampusMerchantReplyService campusMerchantReplyService;

    @ApiOperation(value = "Campus merchant reply list")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<StoreProductReplyResponse>> list(@RequestParam(required = false) Boolean replied,
                                                                    @Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(CommonPage.restPage(campusMerchantReplyService.getList(replied, pageParamRequest)));
    }

    @ApiOperation(value = "Campus merchant reply comment")
    @RequestMapping(value = "/comment", method = RequestMethod.POST)
    public CommonResult<Boolean> comment(@RequestBody @Validated StoreProductReplyCommentRequest request) {
        return CommonResult.success(campusMerchantReplyService.comment(request));
    }
}
