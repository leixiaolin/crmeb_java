package com.zbkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.constants.Constants;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.product.StoreProduct;
import com.zbkj.common.model.product.StoreProductAttrValue;
import com.zbkj.common.model.system.SystemStoreStaff;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.CampusMerchantProductAttrValueRequest;
import com.zbkj.common.request.CampusMerchantProductSpecUpdateRequest;
import com.zbkj.common.request.CampusMerchantProductUpdateRequest;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.response.StoreProductAttrValueResponse;
import com.zbkj.common.response.StoreProductResponse;
import com.zbkj.service.service.CampusMerchantProductService;
import com.zbkj.service.service.CampusStoreRangeService;
import com.zbkj.service.service.StoreProductAttrValueService;
import com.zbkj.service.service.StoreProductService;
import com.zbkj.service.service.SystemStoreStaffService;
import com.zbkj.service.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CampusMerchantProductServiceImpl implements CampusMerchantProductService {

    @Autowired
    private StoreProductService storeProductService;

    @Autowired
    private StoreProductAttrValueService storeProductAttrValueService;

    @Autowired
    private SystemStoreStaffService systemStoreStaffService;

    @Autowired
    private CampusStoreRangeService campusStoreRangeService;

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Override
    public PageInfo<StoreProductResponse> getList(String keywords, PageParamRequest pageParamRequest) {
        SystemStoreStaff staff = getStaff();
        LambdaQueryWrapper<StoreProduct> wrapper = getMerchantProductWrapper(staff.getStoreId());
        if (StrUtil.isNotBlank(keywords)) {
            wrapper.and(i -> i.like(StoreProduct::getStoreName, keywords)
                    .or().eq(StoreProduct::getId, keywords)
                    .or().like(StoreProduct::getKeyword, keywords));
        }
        wrapper.orderByDesc(StoreProduct::getSort);
        wrapper.orderByDesc(StoreProduct::getId);
        PageInfo<StoreProduct> productPage = PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit())
                .doSelectPageInfo(() -> storeProductService.list(wrapper));
        if (CollUtil.isEmpty(productPage.getList())) {
            return CommonPage.copyPageInfo(productPage, CollUtil.newArrayList());
        }
        List<StoreProductResponse> responseList = CollUtil.newArrayList();
        productPage.getList().forEach(product -> {
            StoreProductResponse response = new StoreProductResponse();
            BeanUtils.copyProperties(product, response);
            responseList.add(response);
        });
        return CommonPage.copyPageInfo(productPage, responseList);
    }

    @Override
    public Boolean putOn(Integer productId) {
        StoreProduct product = getMerchantProduct(productId);
        return storeProductService.putOnShelf(product.getId());
    }

    @Override
    public Boolean offShelf(Integer productId) {
        StoreProduct product = getMerchantProduct(productId);
        return storeProductService.offShelf(product.getId());
    }

    @Override
    public Boolean updateSimple(Integer productId, CampusMerchantProductUpdateRequest request) {
        StoreProduct product = getMerchantProduct(productId);
        if (Boolean.TRUE.equals(product.getSpecType())) {
            throw new CrmebException("多规格商品请在规格维护中调整价格和库存");
        }
        List<StoreProductAttrValue> attrValues = storeProductAttrValueService
                .getListByProductIdAndType(product.getId(), Constants.PRODUCT_TYPE_NORMAL);
        if (CollUtil.isEmpty(attrValues) || attrValues.size() != 1) {
            throw new CrmebException("单规格商品规格数据异常，请刷新后重试");
        }
        StoreProductAttrValue attrValue = attrValues.get(0);
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        attrValue.setPrice(request.getPrice());
        attrValue.setStock(request.getStock());
        Boolean execute = transactionTemplate.execute(e -> {
            if (!storeProductService.updateById(product)) {
                throw new CrmebException("商品价格库存更新失败");
            }
            if (!storeProductAttrValueService.updateById(attrValue)) {
                throw new CrmebException("商品规格价格库存更新失败");
            }
            return Boolean.TRUE;
        });
        return Boolean.TRUE.equals(execute);
    }

    @Override
    public List<StoreProductAttrValueResponse> getSpecList(Integer productId) {
        StoreProduct product = getMerchantProduct(productId);
        List<StoreProductAttrValue> attrValues = getProductAttrValues(product);
        List<StoreProductAttrValueResponse> responseList = CollUtil.newArrayList();
        attrValues.forEach(attrValue -> {
            StoreProductAttrValueResponse response = new StoreProductAttrValueResponse();
            BeanUtils.copyProperties(attrValue, response);
            responseList.add(response);
        });
        return responseList;
    }

    @Override
    public Boolean updateSpec(Integer productId, CampusMerchantProductSpecUpdateRequest request) {
        StoreProduct product = getMerchantProduct(productId);
        List<StoreProductAttrValue> attrValues = getProductAttrValues(product);
        List<CampusMerchantProductAttrValueRequest> valueRequests = request.getAttrValueList();
        Set<Integer> requestIds = valueRequests.stream()
                .map(CampusMerchantProductAttrValueRequest::getId)
                .collect(Collectors.toSet());
        if (requestIds.size() != valueRequests.size() || requestIds.size() != attrValues.size()) {
            throw new CrmebException("商品规格数据不完整，请刷新后重试");
        }
        Map<Integer, CampusMerchantProductAttrValueRequest> requestMap = valueRequests.stream()
                .collect(Collectors.toMap(CampusMerchantProductAttrValueRequest::getId, Function.identity()));
        attrValues.forEach(attrValue -> {
            CampusMerchantProductAttrValueRequest valueRequest = requestMap.get(attrValue.getId());
            if (ObjectUtil.isNull(valueRequest)) {
                throw new CrmebException("商品规格不属于当前商品");
            }
            attrValue.setPrice(valueRequest.getPrice());
            attrValue.setStock(valueRequest.getStock());
        });
        product.setPrice(attrValues.stream()
                .map(StoreProductAttrValue::getPrice)
                .min((left, right) -> left.compareTo(right))
                .orElse(product.getPrice()));
        product.setStock(attrValues.stream().mapToInt(StoreProductAttrValue::getStock).sum());
        Boolean execute = transactionTemplate.execute(e -> {
            if (!storeProductAttrValueService.updateBatchById(attrValues)) {
                throw new CrmebException("商品规格价格库存更新失败");
            }
            if (!storeProductService.updateById(product)) {
                throw new CrmebException("商品价格库存汇总更新失败");
            }
            return Boolean.TRUE;
        });
        return Boolean.TRUE.equals(execute);
    }

    private StoreProduct getMerchantProduct(Integer productId) {
        SystemStoreStaff staff = getStaff();
        LambdaQueryWrapper<StoreProduct> wrapper = getMerchantProductWrapper(staff.getStoreId());
        wrapper.eq(StoreProduct::getId, productId);
        StoreProduct product = storeProductService.getOne(wrapper);
        if (ObjectUtil.isNull(product)) {
            throw new CrmebException("商品不存在或无权维护");
        }
        return product;
    }

    private LambdaQueryWrapper<StoreProduct> getMerchantProductWrapper(Integer storeId) {
        LambdaQueryWrapper<StoreProduct> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(StoreProduct::getMerId, storeId);
        wrapper.eq(StoreProduct::getIsRecycle, false);
        wrapper.eq(StoreProduct::getIsDel, false);
        return wrapper;
    }

    private List<StoreProductAttrValue> getProductAttrValues(StoreProduct product) {
        List<StoreProductAttrValue> attrValues = storeProductAttrValueService
                .getListByProductIdAndType(product.getId(), Constants.PRODUCT_TYPE_NORMAL);
        if (CollUtil.isEmpty(attrValues)) {
            throw new CrmebException("商品规格数据异常，请刷新后重试");
        }
        return attrValues;
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
