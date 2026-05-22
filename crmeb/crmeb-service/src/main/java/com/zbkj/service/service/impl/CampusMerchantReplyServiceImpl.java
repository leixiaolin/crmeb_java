package com.zbkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.product.StoreProduct;
import com.zbkj.common.model.product.StoreProductReply;
import com.zbkj.common.model.system.SystemStoreStaff;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.request.StoreProductReplyCommentRequest;
import com.zbkj.common.response.StoreProductReplyResponse;
import com.zbkj.common.utils.CrmebUtil;
import com.zbkj.service.service.CampusMerchantReplyService;
import com.zbkj.service.service.CampusStoreRangeService;
import com.zbkj.service.service.StoreProductReplyService;
import com.zbkj.service.service.StoreProductService;
import com.zbkj.service.service.SystemStoreStaffService;
import com.zbkj.service.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CampusMerchantReplyServiceImpl implements CampusMerchantReplyService {

    @Autowired
    private StoreProductService storeProductService;

    @Autowired
    private StoreProductReplyService storeProductReplyService;

    @Autowired
    private SystemStoreStaffService systemStoreStaffService;

    @Autowired
    private CampusStoreRangeService campusStoreRangeService;

    @Autowired
    private UserService userService;

    @Override
    public PageInfo<StoreProductReplyResponse> getList(Boolean replied, PageParamRequest pageParamRequest) {
        SystemStoreStaff staff = getStaff();
        List<Integer> productIds = getMerchantProductIds(staff.getStoreId());
        LambdaQueryWrapper<StoreProductReply> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(StoreProductReply::getIsDel, false);
        if (CollUtil.isEmpty(productIds)) {
            wrapper.eq(StoreProductReply::getProductId, -1);
        } else {
            wrapper.in(StoreProductReply::getProductId, productIds);
        }
        if (ObjectUtil.isNotNull(replied)) {
            wrapper.eq(StoreProductReply::getIsReply, replied);
        }
        wrapper.orderByDesc(StoreProductReply::getId);
        PageInfo<StoreProductReply> replyPage = PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit())
                .doSelectPageInfo(() -> storeProductReplyService.list(wrapper));
        List<StoreProductReplyResponse> responseList = CollUtil.newArrayList();
        replyPage.getList().forEach(reply -> responseList.add(assembleResponse(reply)));
        return CommonPage.copyPageInfo(replyPage, responseList);
    }

    @Override
    public Boolean comment(StoreProductReplyCommentRequest request) {
        StoreProductReply reply = getMerchantReply(request.getIds());
        if (Boolean.TRUE.equals(reply.getIsReply())) {
            throw new CrmebException("Reply has already been processed");
        }
        return storeProductReplyService.comment(request);
    }

    private StoreProductReplyResponse assembleResponse(StoreProductReply reply) {
        StoreProductReplyResponse response = new StoreProductReplyResponse();
        BeanUtils.copyProperties(reply, response);
        response.setStoreProduct(storeProductService.getById(reply.getProductId()));
        response.setPics(CrmebUtil.stringToArrayStr(reply.getPics()));
        return response;
    }

    private StoreProductReply getMerchantReply(Integer replyId) {
        SystemStoreStaff staff = getStaff();
        StoreProductReply reply = storeProductReplyService.getById(replyId);
        if (ObjectUtil.isNull(reply) || Boolean.TRUE.equals(reply.getIsDel())) {
            throw new CrmebException("Reply does not exist");
        }
        StoreProduct product = storeProductService.getById(reply.getProductId());
        if (ObjectUtil.isNull(product) || !staff.getStoreId().equals(product.getMerId())) {
            throw new CrmebException("Reply does not belong to current store");
        }
        return reply;
    }

    private List<Integer> getMerchantProductIds(Integer storeId) {
        LambdaQueryWrapper<StoreProduct> wrapper = Wrappers.lambdaQuery();
        wrapper.select(StoreProduct::getId);
        wrapper.eq(StoreProduct::getMerId, storeId);
        return storeProductService.list(wrapper).stream().map(StoreProduct::getId).collect(Collectors.toList());
    }

    private SystemStoreStaff getStaff() {
        SystemStoreStaff staff = systemStoreStaffService.getEnabledByUid(userService.getUserIdException());
        if (ObjectUtil.isNull(staff)) {
            throw new CrmebException("Current user is not an enabled store staff");
        }
        if (!campusStoreRangeService.hasEnabledRange(staff.getStoreId())) {
            throw new CrmebException("Current store is not enabled for campus merchant");
        }
        return staff;
    }
}
