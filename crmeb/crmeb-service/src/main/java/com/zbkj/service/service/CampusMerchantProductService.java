package com.zbkj.service.service;

import com.github.pagehelper.PageInfo;
import com.zbkj.common.request.CampusMerchantProductUpdateRequest;
import com.zbkj.common.request.CampusMerchantProductSpecUpdateRequest;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.response.StoreProductAttrValueResponse;
import com.zbkj.common.response.StoreProductResponse;

import java.util.List;

public interface CampusMerchantProductService {

    PageInfo<StoreProductResponse> getList(String keywords, PageParamRequest pageParamRequest);

    Boolean putOn(Integer productId);

    Boolean offShelf(Integer productId);

    Boolean updateSimple(Integer productId, CampusMerchantProductUpdateRequest request);

    List<StoreProductAttrValueResponse> getSpecList(Integer productId);

    Boolean updateSpec(Integer productId, CampusMerchantProductSpecUpdateRequest request);
}
