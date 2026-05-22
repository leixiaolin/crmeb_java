package com.zbkj.service.service;

import com.github.pagehelper.PageInfo;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.request.StoreProductReplyCommentRequest;
import com.zbkj.common.response.StoreProductReplyResponse;

public interface CampusMerchantReplyService {

    PageInfo<StoreProductReplyResponse> getList(Boolean replied, PageParamRequest pageParamRequest);

    Boolean comment(StoreProductReplyCommentRequest request);
}
