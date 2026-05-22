package com.zbkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.constants.Constants;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.order.StoreOrder;
import com.zbkj.common.model.order.StoreOrderInfo;
import com.zbkj.common.model.system.SystemStoreStaff;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.request.StoreOrderRefundRequest;
import com.zbkj.common.response.CampusMerchantOrderSummaryResponse;
import com.zbkj.common.response.CampusMerchantHotProductResponse;
import com.zbkj.common.response.OrderDetailResponse;
import com.zbkj.common.response.OrderInfoResponse;
import com.zbkj.common.response.StoreOrderDetailInfoResponse;
import com.zbkj.service.service.CampusMerchantOrderService;
import com.zbkj.service.service.CampusStoreRangeService;
import com.zbkj.service.service.StoreOrderInfoService;
import com.zbkj.service.service.StoreOrderService;
import com.zbkj.service.service.StoreOrderStatusService;
import com.zbkj.service.service.SystemStoreStaffService;
import com.zbkj.service.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class CampusMerchantOrderServiceImpl implements CampusMerchantOrderService {

    @Autowired
    private StoreOrderService storeOrderService;

    @Autowired
    private StoreOrderInfoService storeOrderInfoService;

    @Autowired
    private StoreOrderStatusService storeOrderStatusService;

    @Autowired
    private SystemStoreStaffService systemStoreStaffService;

    @Autowired
    private CampusStoreRangeService campusStoreRangeService;

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Override
    public Boolean hasAccess() {
        SystemStoreStaff staff = systemStoreStaffService.getEnabledByUid(userService.getUserIdException());
        return ObjectUtil.isNotNull(staff) && campusStoreRangeService.hasEnabledRange(staff.getStoreId());
    }

    @Override
    public CampusMerchantOrderSummaryResponse getTodaySummary() {
        SystemStoreStaff staff = getStaff();
        LambdaQueryWrapper<StoreOrder> wrapper = getMerchantOrderWrapper(staff.getStoreId(), null);
        wrapper.between(StoreOrder::getPayTime, DateUtil.beginOfDay(DateUtil.date()), DateUtil.endOfDay(DateUtil.date()));
        List<StoreOrder> todayOrders = storeOrderService.list(wrapper);
        CampusMerchantOrderSummaryResponse response = new CampusMerchantOrderSummaryResponse();
        response.setTodayOrderCount(todayOrders.size());
        response.setTodayPayAmount(todayOrders.stream()
                .map(StoreOrder::getPayPrice)
                .filter(ObjectUtil::isNotNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        response.setHotProductList(getTodayHotProducts(todayOrders));
        return response;
    }

    @Override
    public PageInfo<OrderDetailResponse> getList(Integer campusStatus, PageParamRequest pageParamRequest) {
        SystemStoreStaff staff = getStaff();
        return getOrderPage(getMerchantOrderWrapper(staff.getStoreId(), campusStatus), pageParamRequest);
    }

    @Override
    public PageInfo<OrderDetailResponse> getRefundList(PageParamRequest pageParamRequest) {
        SystemStoreStaff staff = getStaff();
        LambdaQueryWrapper<StoreOrder> wrapper = getMerchantOrderWrapper(staff.getStoreId(), null);
        wrapper.eq(StoreOrder::getRefundStatus, 1);
        return getOrderPage(wrapper, pageParamRequest);
    }

    private PageInfo<OrderDetailResponse> getOrderPage(LambdaQueryWrapper<StoreOrder> wrapper,
                                                       PageParamRequest pageParamRequest) {
        PageInfo<StoreOrder> orderPage = PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit())
                .doSelectPageInfo(() -> storeOrderService.list(wrapper));
        if (CollUtil.isEmpty(orderPage.getList())) {
            return CommonPage.copyPageInfo(orderPage, CollUtil.newArrayList());
        }
        List<OrderDetailResponse> responseList = CollUtil.newArrayList();
        orderPage.getList().forEach(order -> {
            OrderDetailResponse response = new OrderDetailResponse();
            BeanUtils.copyProperties(order, response);
            response.setOrderStatus(getCampusStatusText(order));
            response.setActivityType("校园外卖");
            response.setOrderInfoList(getOrderInfoList(order.getOrderId()));
            responseList.add(response);
        });
        return CommonPage.copyPageInfo(orderPage, responseList);
    }

    @Override
    public StoreOrderDetailInfoResponse detail(String orderNo) {
        StoreOrder order = getMerchantOrder(orderNo);
        StoreOrderDetailInfoResponse response = new StoreOrderDetailInfoResponse();
        BeanUtils.copyProperties(order, response);
        response.setOrderStatusMsg(getCampusStatusText(order));
        response.setOrderInfoList(getOrderInfoList(order.getOrderId()));
        return response;
    }

    @Override
    public Boolean accept(String orderNo) {
        StoreOrder order = getPendingOrder(orderNo);
        Boolean execute = transactionTemplate.execute(e -> {
            LambdaUpdateWrapper<StoreOrder> wrapper = Wrappers.lambdaUpdate();
            wrapper.set(StoreOrder::getCampusStatus, Constants.CAMPUS_ORDER_STATUS_DELIVERING);
            wrapper.set(StoreOrder::getUpdateTime, DateUtil.date());
            wrapper.eq(StoreOrder::getId, order.getId());
            wrapper.eq(StoreOrder::getCampusStatus, Constants.CAMPUS_ORDER_STATUS_PENDING_ACCEPT);
            if (!storeOrderService.update(wrapper)) {
                throw new CrmebException("Campus order has been processed");
            }
            storeOrderStatusService.createLog(order.getId(), Constants.ORDER_LOG_CAMPUS_ACCEPT,
                    Constants.ORDER_LOG_MESSAGE_CAMPUS_ACCEPT);
            return Boolean.TRUE;
        });
        return Boolean.TRUE.equals(execute);
    }

    @Override
    public Boolean reject(String orderNo) {
        StoreOrder order = getPendingOrder(orderNo);
        StoreOrderRefundRequest refundRequest = new StoreOrderRefundRequest();
        refundRequest.setOrderNo(order.getOrderId());
        refundRequest.setAmount(order.getPayPrice());
        if (!storeOrderService.refund(refundRequest)) {
            return Boolean.FALSE;
        }
        StoreOrder campusRejectUpdate = new StoreOrder();
        campusRejectUpdate.setId(order.getId());
        campusRejectUpdate.setCampusStatus(Constants.CAMPUS_ORDER_STATUS_CANCELED);
        campusRejectUpdate.setUpdateTime(DateUtil.date());
        if (!storeOrderService.updateById(campusRejectUpdate)) {
            throw new CrmebException("Campus order reject status update failed");
        }
        storeOrderStatusService.createLog(order.getId(), Constants.ORDER_LOG_CAMPUS_REJECT,
                Constants.ORDER_LOG_MESSAGE_CAMPUS_REJECT);
        return Boolean.TRUE;
    }

    @Override
    public Boolean refund(StoreOrderRefundRequest request) {
        StoreOrder order = getRefundApplyOrder(request.getOrderNo());
        request.setOrderNo(order.getOrderId());
        return storeOrderService.refund(request);
    }

    @Override
    public Boolean refundRefuse(String orderNo, String reason) {
        StoreOrder order = getRefundApplyOrder(orderNo);
        return storeOrderService.refundRefuse(order.getOrderId(), reason);
    }

    private StoreOrder getPendingOrder(String orderNo) {
        StoreOrder order = getMerchantOrder(orderNo);
        if (!order.getPaid() || !Objects.equals(Constants.CAMPUS_ORDER_STATUS_PENDING_ACCEPT, order.getCampusStatus())) {
            throw new CrmebException("Campus order is not pending acceptance");
        }
        if (!order.getRefundStatus().equals(0)) {
            throw new CrmebException("Campus order refund is already processing");
        }
        return order;
    }

    private StoreOrder getRefundApplyOrder(String orderNo) {
        StoreOrder order = getMerchantOrder(orderNo);
        if (!order.getPaid() || !order.getRefundStatus().equals(1)) {
            throw new CrmebException("Campus refund application has been processed");
        }
        return order;
    }

    private StoreOrder getMerchantOrder(String orderNo) {
        SystemStoreStaff staff = getStaff();
        LambdaQueryWrapper<StoreOrder> wrapper = getMerchantOrderWrapper(staff.getStoreId(), null);
        wrapper.eq(StoreOrder::getOrderId, orderNo);
        StoreOrder order = storeOrderService.getOne(wrapper);
        if (ObjectUtil.isNull(order)) {
            throw new CrmebException("Campus order not found");
        }
        return order;
    }

    private LambdaQueryWrapper<StoreOrder> getMerchantOrderWrapper(Integer storeId, Integer campusStatus) {
        LambdaQueryWrapper<StoreOrder> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(StoreOrder::getShippingType, 3);
        wrapper.eq(StoreOrder::getMerId, storeId);
        wrapper.eq(StoreOrder::getPaid, true);
        wrapper.eq(StoreOrder::getIsDel, false);
        wrapper.eq(StoreOrder::getIsSystemDel, false);
        if (ObjectUtil.isNotNull(campusStatus)) {
            wrapper.eq(StoreOrder::getCampusStatus, campusStatus);
        }
        wrapper.orderByDesc(StoreOrder::getId);
        return wrapper;
    }

    private SystemStoreStaff getStaff() {
        SystemStoreStaff staff = systemStoreStaffService.getEnabledByUid(userService.getUserIdException());
        if (ObjectUtil.isNull(staff)) {
            throw new CrmebException("Current user is not an enabled store staff");
        }
        return staff;
    }

    private List<OrderInfoResponse> getOrderInfoList(String orderNo) {
        List<StoreOrderInfo> infoList = storeOrderInfoService.getListByOrderNo(orderNo);
        List<OrderInfoResponse> responseList = CollUtil.newArrayList();
        infoList.forEach(info -> {
            OrderInfoResponse response = new OrderInfoResponse();
            response.setAttrId(info.getAttrValueId() + "");
            response.setProductId(info.getProductId());
            response.setCartNum(info.getPayNum());
            response.setImage(info.getImage());
            response.setStoreName(info.getProductName());
            response.setPrice(ObjectUtil.isNotNull(info.getVipPrice()) ? info.getVipPrice() : info.getPrice());
            response.setIsReply(info.getIsReply() ? 1 : 0);
            response.setSku(info.getSku());
            responseList.add(response);
        });
        return responseList;
    }

    private List<CampusMerchantHotProductResponse> getTodayHotProducts(List<StoreOrder> todayOrders) {
        List<CampusMerchantHotProductResponse> result = new ArrayList<>();
        if (CollUtil.isEmpty(todayOrders)) {
            return result;
        }
        List<String> orderNos = new ArrayList<>();
        todayOrders.forEach(order -> orderNos.add(order.getOrderId()));
        LambdaQueryWrapper<StoreOrderInfo> wrapper = Wrappers.lambdaQuery();
        wrapper.in(StoreOrderInfo::getOrderNo, orderNos);
        List<StoreOrderInfo> orderInfos = storeOrderInfoService.list(wrapper);
        Map<Integer, CampusMerchantHotProductResponse> productMap = new HashMap<>();
        orderInfos.forEach(info -> {
            CampusMerchantHotProductResponse item = productMap.get(info.getProductId());
            if (ObjectUtil.isNull(item)) {
                item = new CampusMerchantHotProductResponse();
                item.setProductId(info.getProductId());
                item.setProductName(info.getProductName());
                item.setPayNum(0);
                productMap.put(info.getProductId(), item);
            }
            item.setPayNum(item.getPayNum() + info.getPayNum());
        });
        result.addAll(productMap.values());
        result.sort(Comparator.comparing(CampusMerchantHotProductResponse::getPayNum).reversed()
                .thenComparing(CampusMerchantHotProductResponse::getProductId));
        return result.size() > 3 ? new ArrayList<>(result.subList(0, 3)) : result;
    }

    private String getCampusStatusText(StoreOrder order) {
        if (order.getRefundStatus().equals(1) || order.getRefundStatus().equals(3)) {
            return "退款中";
        }
        if (order.getRefundStatus().equals(2)) {
            return "已退款";
        }
        if (Objects.equals(Constants.CAMPUS_ORDER_STATUS_PENDING_ACCEPT, order.getCampusStatus())) {
            return "待接单";
        }
        if (Objects.equals(Constants.CAMPUS_ORDER_STATUS_DELIVERING, order.getCampusStatus())) {
            return "配送中";
        }
        if (Objects.equals(Constants.CAMPUS_ORDER_STATUS_DELIVERED, order.getCampusStatus())) {
            return "已送达";
        }
        if (Objects.equals(Constants.CAMPUS_ORDER_STATUS_CANCELED, order.getCampusStatus())) {
            return "已取消";
        }
        return "待支付";
    }
}
