package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.model.campus.CampusStoreRange;
import com.zbkj.common.model.system.SystemStore;
import com.zbkj.common.request.CampusStoreRangeRequest;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.response.CampusStoreRangeResponse;

import java.util.List;

public interface CampusStoreRangeService extends IService<CampusStoreRange> {

    PageInfo<CampusStoreRangeResponse> getList(Integer schoolId, Boolean status, PageParamRequest pageParamRequest);

    List<CampusStoreRangeResponse> getEnabledList(Integer schoolId);

    List<SystemStore> getCandidateStores(String keywords, PageParamRequest pageParamRequest);

    Boolean create(CampusStoreRangeRequest request);

    Boolean update(Integer id, CampusStoreRangeRequest request);

    Boolean updateStatus(Integer id, Boolean status);

    Boolean delete(Integer id);
}
