package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.campus.CampusStoreRange;
import com.zbkj.common.model.system.SystemStore;
import com.zbkj.common.request.CampusStoreRangeRequest;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.response.CampusStoreRangeResponse;
import com.zbkj.common.response.CampusSearchResponse;
import com.zbkj.common.response.StoreProductReplyResponse;

import java.util.List;

public interface CampusStoreRangeService extends IService<CampusStoreRange> {

    PageInfo<CampusStoreRangeResponse> getList(Integer schoolId, Boolean status, PageParamRequest pageParamRequest);

    List<CampusStoreRangeResponse> getEnabledList(Integer schoolId);

    PageInfo<StoreProductReplyResponse> getReplyList(Integer schoolId, Integer storeId, PageParamRequest pageParamRequest);

    CampusSearchResponse search(Integer schoolId, String keyword, PageParamRequest pageParamRequest);

    Boolean isEnabled(Integer schoolId, Integer storeId);

    Boolean hasEnabledRange(Integer storeId);

    List<SystemStore> getCandidateStores(String keywords, PageParamRequest pageParamRequest);

    Boolean create(CampusStoreRangeRequest request);

    Boolean update(Integer id, CampusStoreRangeRequest request);

    Boolean updateStatus(Integer id, Boolean status);

    Boolean delete(Integer id);
}
