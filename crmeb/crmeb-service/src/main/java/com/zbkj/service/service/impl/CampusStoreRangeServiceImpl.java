package com.zbkj.service.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.campus.CampusDeliveryConfig;
import com.zbkj.common.model.campus.CampusStoreRange;
import com.zbkj.common.model.product.StoreProduct;
import com.zbkj.common.model.product.StoreProductReply;
import com.zbkj.common.model.system.SystemStore;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.CampusStoreRangeRequest;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.response.CampusStoreRangeResponse;
import com.zbkj.common.response.CampusSearchResponse;
import com.zbkj.common.response.StoreProductReplyResponse;
import com.zbkj.common.response.StoreProductResponse;
import com.zbkj.common.utils.CrmebUtil;
import com.zbkj.service.dao.CampusStoreRangeDao;
import com.zbkj.service.service.CampusDeliveryService;
import com.zbkj.service.service.CampusSchoolService;
import com.zbkj.service.service.CampusStoreRangeService;
import com.zbkj.service.service.StoreProductReplyService;
import com.zbkj.service.service.StoreProductService;
import com.zbkj.service.service.SystemStoreService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CampusStoreRangeServiceImpl extends ServiceImpl<CampusStoreRangeDao, CampusStoreRange> implements CampusStoreRangeService {

    @Autowired
    private CampusSchoolService campusSchoolService;

    @Autowired
    private CampusDeliveryService campusDeliveryService;

    @Autowired
    private SystemStoreService systemStoreService;

    @Autowired
    private StoreProductService storeProductService;

    @Autowired
    private StoreProductReplyService storeProductReplyService;

    @Override
    public PageInfo<CampusStoreRangeResponse> getList(Integer schoolId, Boolean status, PageParamRequest pageParamRequest) {
        campusSchoolService.getEnabledById(schoolId);
        Page<CampusStoreRange> rangePage = PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        LambdaQueryWrapper<CampusStoreRange> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(CampusStoreRange::getSchoolId, schoolId).eq(CampusStoreRange::getIsDel, false);
        if (ObjectUtil.isNotNull(status)) {
            wrapper.eq(CampusStoreRange::getStatus, status);
        }
        wrapper.orderByAsc(CampusStoreRange::getSort).orderByDesc(CampusStoreRange::getId);
        return CommonPage.copyPageInfo(rangePage, assemble(list(wrapper)));
    }

    @Override
    public List<CampusStoreRangeResponse> getEnabledList(Integer schoolId) {
        campusSchoolService.getEnabledById(schoolId);
        LambdaQueryWrapper<CampusStoreRange> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(CampusStoreRange::getSchoolId, schoolId);
        wrapper.eq(CampusStoreRange::getStatus, true).eq(CampusStoreRange::getIsDel, false);
        wrapper.orderByAsc(CampusStoreRange::getSort).orderByDesc(CampusStoreRange::getId);
        return assemble(list(wrapper)).stream()
                .filter(item -> ObjectUtil.isNotNull(item.getSystemStore()))
                .filter(item -> item.getSystemStore().getIsShow() && !item.getSystemStore().getIsDel())
                .collect(Collectors.toList());
    }

    @Override
    public PageInfo<StoreProductReplyResponse> getReplyList(Integer schoolId, Integer storeId, PageParamRequest pageParamRequest) {
        campusSchoolService.getEnabledById(schoolId);
        checkStore(storeId);
        if (!isEnabled(schoolId, storeId)) {
            throw new CrmebException("Campus store is unavailable");
        }
        LambdaQueryWrapper<StoreProduct> productWrapper = Wrappers.lambdaQuery();
        productWrapper.select(StoreProduct::getId);
        productWrapper.eq(StoreProduct::getMerId, storeId);
        productWrapper.eq(StoreProduct::getIsDel, false);
        List<Integer> productIds = storeProductService.list(productWrapper).stream()
                .map(StoreProduct::getId)
                .collect(Collectors.toList());
        LambdaQueryWrapper<StoreProductReply> replyWrapper = Wrappers.lambdaQuery();
        replyWrapper.eq(StoreProductReply::getIsDel, false);
        if (productIds.isEmpty()) {
            replyWrapper.eq(StoreProductReply::getProductId, -1);
        } else {
            replyWrapper.in(StoreProductReply::getProductId, productIds);
        }
        replyWrapper.orderByDesc(StoreProductReply::getId);
        PageInfo<StoreProductReply> replyPage = PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit())
                .doSelectPageInfo(() -> storeProductReplyService.list(replyWrapper));
        List<StoreProductReplyResponse> responseList = new ArrayList<>();
        for (StoreProductReply reply : replyPage.getList()) {
            StoreProductReplyResponse response = new StoreProductReplyResponse();
            BeanUtils.copyProperties(reply, response);
            response.setNickname(maskNickname(reply.getNickname()));
            response.setStoreProduct(storeProductService.getById(reply.getProductId()));
            response.setPics(CrmebUtil.stringToArrayStr(reply.getPics()));
            responseList.add(response);
        }
        return CommonPage.copyPageInfo(replyPage, responseList);
    }

    @Override
    public CampusSearchResponse search(Integer schoolId, String keyword, PageParamRequest pageParamRequest) {
        if (StrUtil.isBlank(keyword)) {
            throw new CrmebException("Search keyword cannot be empty");
        }
        List<CampusStoreRangeResponse> enabledStores = getEnabledList(schoolId);
        List<Integer> storeIds = enabledStores.stream()
                .map(CampusStoreRangeResponse::getStoreId)
                .collect(Collectors.toList());
        CampusSearchResponse response = new CampusSearchResponse();
        response.setStoreList(enabledStores.stream()
                .filter(item -> matchStore(item.getSystemStore(), keyword))
                .collect(Collectors.toList()));

        LambdaQueryWrapper<StoreProduct> productWrapper = Wrappers.lambdaQuery();
        productWrapper.eq(StoreProduct::getIsDel, false);
        productWrapper.eq(StoreProduct::getIsRecycle, false);
        productWrapper.eq(StoreProduct::getIsShow, true);
        if (storeIds.isEmpty()) {
            productWrapper.eq(StoreProduct::getMerId, -1);
        } else {
            productWrapper.in(StoreProduct::getMerId, storeIds);
        }
        productWrapper.and(wrapper -> wrapper.like(StoreProduct::getStoreName, keyword)
                .or().like(StoreProduct::getKeyword, keyword));
        productWrapper.orderByDesc(StoreProduct::getSort).orderByDesc(StoreProduct::getId);
        PageInfo<StoreProduct> productPage = PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit())
                .doSelectPageInfo(() -> storeProductService.list(productWrapper));
        List<StoreProductResponse> productList = new ArrayList<>();
        for (StoreProduct product : productPage.getList()) {
            StoreProductResponse productResponse = new StoreProductResponse();
            BeanUtils.copyProperties(product, productResponse);
            productList.add(productResponse);
        }
        response.setProductList(productList);
        return response;
    }

    private Boolean matchStore(SystemStore store, String keyword) {
        if (ObjectUtil.isNull(store)) {
            return false;
        }
        return StrUtil.containsIgnoreCase(store.getName(), keyword)
                || StrUtil.containsIgnoreCase(store.getIntroduction(), keyword)
                || StrUtil.containsIgnoreCase(store.getNotice(), keyword)
                || StrUtil.containsIgnoreCase(store.getAddress(), keyword)
                || StrUtil.containsIgnoreCase(store.getDetailedAddress(), keyword);
    }

    private String maskNickname(String nickname) {
        if (StrUtil.isBlank(nickname)) {
            return nickname;
        }
        if (nickname.length() == 1) {
            return nickname.concat("**");
        }
        if (nickname.length() == 2) {
            return nickname.substring(0, 1) + "**";
        }
        return nickname.substring(0, 1) + "**" + nickname.substring(nickname.length() - 1);
    }

    @Override
    public Boolean isEnabled(Integer schoolId, Integer storeId) {
        CampusStoreRange range = getRange(schoolId, storeId);
        return ObjectUtil.isNotNull(range) && range.getStatus();
    }

    @Override
    public Boolean hasEnabledRange(Integer storeId) {
        LambdaQueryWrapper<CampusStoreRange> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(CampusStoreRange::getStoreId, storeId);
        wrapper.eq(CampusStoreRange::getStatus, true).eq(CampusStoreRange::getIsDel, false);
        return count(wrapper) > 0;
    }

    @Override
    public List<SystemStore> getCandidateStores(String keywords, PageParamRequest pageParamRequest) {
        return systemStoreService.getList(keywords, 1, pageParamRequest);
    }

    @Override
    public Boolean create(CampusStoreRangeRequest request) {
        campusSchoolService.getEnabledById(request.getSchoolId());
        checkStore(request.getStoreId());
        if (ObjectUtil.isNotNull(getRange(request.getSchoolId(), request.getStoreId()))) {
            throw new CrmebException("Campus store range already exists");
        }
        CampusStoreRange range = new CampusStoreRange();
        BeanUtils.copyProperties(request, range);
        return save(range);
    }

    @Override
    public Boolean update(Integer id, CampusStoreRangeRequest request) {
        CampusStoreRange range = checkExist(id);
        campusSchoolService.getEnabledById(request.getSchoolId());
        checkStore(request.getStoreId());
        CampusStoreRange exist = getRange(request.getSchoolId(), request.getStoreId());
        if (ObjectUtil.isNotNull(exist) && !exist.getId().equals(id)) {
            throw new CrmebException("Campus store range already exists");
        }
        BeanUtils.copyProperties(request, range);
        range.setUpdateTime(DateUtil.date());
        return updateById(range);
    }

    @Override
    public Boolean updateStatus(Integer id, Boolean status) {
        CampusStoreRange range = checkExist(id);
        range.setStatus(status);
        range.setUpdateTime(DateUtil.date());
        return updateById(range);
    }

    @Override
    public Boolean delete(Integer id) {
        checkExist(id);
        return removeById(id);
    }

    private List<CampusStoreRangeResponse> assemble(List<CampusStoreRange> ranges) {
        List<CampusStoreRangeResponse> responses = new ArrayList<>();
        if (ranges.isEmpty()) {
            return responses;
        }
        List<Integer> storeIds = ranges.stream().map(CampusStoreRange::getStoreId).collect(Collectors.toList());
        HashMap<Integer, SystemStore> storeMap = systemStoreService.getMapInId(storeIds);
        HashMap<Integer, CampusDeliveryConfig> deliveryConfigMap = new HashMap<>();
        HashMap<Integer, Integer> replyCountMap = new HashMap<>();
        HashMap<Integer, Integer> replyScoreTotalMap = new HashMap<>();
        assembleReplySummary(storeIds, replyCountMap, replyScoreTotalMap);
        for (CampusStoreRange range : ranges) {
            CampusStoreRangeResponse response = new CampusStoreRangeResponse();
            BeanUtils.copyProperties(range, response);
            SystemStore systemStore = storeMap.get(range.getStoreId());
            response.setSystemStore(systemStore);
            response.setOpenNow(systemStoreService.isOpenNow(systemStore));
            CampusDeliveryConfig deliveryConfig = deliveryConfigMap.get(range.getSchoolId());
            if (ObjectUtil.isNull(deliveryConfig) && !deliveryConfigMap.containsKey(range.getSchoolId())) {
                deliveryConfig = campusDeliveryService.getConfig(range.getSchoolId());
                deliveryConfigMap.put(range.getSchoolId(), deliveryConfig);
            }
            if (ObjectUtil.isNotNull(deliveryConfig)) {
                response.setStartPrice(deliveryConfig.getStartPrice());
            }
            fillReplySummary(response, replyCountMap, replyScoreTotalMap);
            responses.add(response);
        }
        return responses;
    }

    private void assembleReplySummary(List<Integer> storeIds, HashMap<Integer, Integer> replyCountMap,
                                      HashMap<Integer, Integer> replyScoreTotalMap) {
        LambdaQueryWrapper<StoreProduct> productWrapper = Wrappers.lambdaQuery();
        productWrapper.select(StoreProduct::getId, StoreProduct::getMerId);
        productWrapper.in(StoreProduct::getMerId, storeIds);
        productWrapper.eq(StoreProduct::getIsDel, false);
        List<StoreProduct> products = storeProductService.list(productWrapper);
        if (products.isEmpty()) {
            return;
        }
        HashMap<Integer, Integer> productStoreMap = new HashMap<>();
        products.forEach(product -> productStoreMap.put(product.getId(), product.getMerId()));
        List<Integer> productIds = products.stream().map(StoreProduct::getId).collect(Collectors.toList());
        LambdaQueryWrapper<StoreProductReply> replyWrapper = Wrappers.lambdaQuery();
        replyWrapper.select(StoreProductReply::getProductId, StoreProductReply::getProductScore, StoreProductReply::getServiceScore);
        replyWrapper.in(StoreProductReply::getProductId, productIds);
        replyWrapper.eq(StoreProductReply::getIsDel, false);
        List<StoreProductReply> replies = storeProductReplyService.list(replyWrapper);
        for (StoreProductReply reply : replies) {
            Integer storeId = productStoreMap.get(reply.getProductId());
            if (ObjectUtil.isNull(storeId)) {
                continue;
            }
            int score = ObjectUtil.defaultIfNull(reply.getProductScore(), 0)
                    + ObjectUtil.defaultIfNull(reply.getServiceScore(), 0);
            replyCountMap.put(storeId, ObjectUtil.defaultIfNull(replyCountMap.get(storeId), 0) + 1);
            replyScoreTotalMap.put(storeId, ObjectUtil.defaultIfNull(replyScoreTotalMap.get(storeId), 0) + score);
        }
    }

    private void fillReplySummary(CampusStoreRangeResponse response, HashMap<Integer, Integer> replyCountMap,
                                  HashMap<Integer, Integer> replyScoreTotalMap) {
        Integer replyCount = ObjectUtil.defaultIfNull(replyCountMap.get(response.getStoreId()), 0);
        response.setReplyCount(replyCount);
        if (replyCount <= 0) {
            response.setReplyScore(BigDecimal.ZERO);
            return;
        }
        BigDecimal score = BigDecimal.valueOf(replyScoreTotalMap.get(response.getStoreId()))
                .divide(BigDecimal.valueOf(replyCount * 2L), 1, RoundingMode.HALF_UP);
        response.setReplyScore(score);
    }

    private CampusStoreRange getRange(Integer schoolId, Integer storeId) {
        LambdaQueryWrapper<CampusStoreRange> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(CampusStoreRange::getSchoolId, schoolId);
        wrapper.eq(CampusStoreRange::getStoreId, storeId);
        wrapper.eq(CampusStoreRange::getIsDel, false);
        return getOne(wrapper);
    }

    private CampusStoreRange checkExist(Integer id) {
        CampusStoreRange range = getById(id);
        if (ObjectUtil.isNull(range)) {
            throw new CrmebException("Campus store range does not exist");
        }
        return range;
    }

    private SystemStore checkStore(Integer storeId) {
        SystemStore store = systemStoreService.getById(storeId);
        if (ObjectUtil.isNull(store) || store.getIsDel() || !store.getIsShow()) {
            throw new CrmebException("Campus store is unavailable");
        }
        return store;
    }
}
