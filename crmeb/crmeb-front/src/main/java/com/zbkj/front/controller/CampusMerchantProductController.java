package com.zbkj.front.controller;

import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.CampusMerchantProductUpdateRequest;
import com.zbkj.common.request.CampusMerchantProductSpecUpdateRequest;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.response.StoreProductAttrValueResponse;
import com.zbkj.common.response.StoreProductResponse;
import com.zbkj.common.result.CommonResult;
import com.zbkj.service.service.CampusMerchantProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/front/campus/merchant/product")
@Api(tags = "Campus merchant products")
public class CampusMerchantProductController {

    @Autowired
    private CampusMerchantProductService campusMerchantProductService;

    @ApiOperation(value = "Campus merchant product list")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<StoreProductResponse>> list(@RequestParam(required = false) String keywords,
                                                               @Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(CommonPage.restPage(campusMerchantProductService.getList(keywords, pageParamRequest)));
    }

    @ApiOperation(value = "Campus merchant put product on shelf")
    @RequestMapping(value = "/putOn/{productId}", method = RequestMethod.POST)
    public CommonResult<Boolean> putOn(@PathVariable Integer productId) {
        return CommonResult.success(campusMerchantProductService.putOn(productId));
    }

    @ApiOperation(value = "Campus merchant take product off shelf")
    @RequestMapping(value = "/offShelf/{productId}", method = RequestMethod.POST)
    public CommonResult<Boolean> offShelf(@PathVariable Integer productId) {
        return CommonResult.success(campusMerchantProductService.offShelf(productId));
    }

    @ApiOperation(value = "Campus merchant update simple product price and stock")
    @RequestMapping(value = "/simple/{productId}", method = RequestMethod.POST)
    public CommonResult<Boolean> updateSimple(@PathVariable Integer productId,
                                              @RequestBody @Validated CampusMerchantProductUpdateRequest request) {
        return CommonResult.success(campusMerchantProductService.updateSimple(productId, request));
    }

    @ApiOperation(value = "Campus merchant product sku list")
    @RequestMapping(value = "/spec/{productId}", method = RequestMethod.GET)
    public CommonResult<List<StoreProductAttrValueResponse>> specList(@PathVariable Integer productId) {
        return CommonResult.success(campusMerchantProductService.getSpecList(productId));
    }

    @ApiOperation(value = "Campus merchant update product sku price and stock")
    @RequestMapping(value = "/spec/{productId}", method = RequestMethod.POST)
    public CommonResult<Boolean> updateSpec(@PathVariable Integer productId,
                                            @RequestBody @Validated CampusMerchantProductSpecUpdateRequest request) {
        return CommonResult.success(campusMerchantProductService.updateSpec(productId, request));
    }
}
