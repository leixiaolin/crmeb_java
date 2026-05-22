package com.zbkj.service.service;

import com.github.pagehelper.PageInfo;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.request.StoreOrderRefundRequest;
import com.zbkj.common.response.CampusMerchantOrderSummaryResponse;
import com.zbkj.common.response.OrderDetailResponse;
import com.zbkj.common.response.StoreOrderDetailInfoResponse;

public interface CampusMerchantOrderService {

    Boolean hasAccess();

    CampusMerchantOrderSummaryResponse getTodaySummary();

    PageInfo<OrderDetailResponse> getList(Integer campusStatus, PageParamRequest pageParamRequest);

    PageInfo<OrderDetailResponse> getRefundList(PageParamRequest pageParamRequest);

    StoreOrderDetailInfoResponse detail(String orderNo);

    Boolean accept(String orderNo);

    Boolean reject(String orderNo);

    Boolean refund(StoreOrderRefundRequest request);

    Boolean refundRefuse(String orderNo, String reason);
}
