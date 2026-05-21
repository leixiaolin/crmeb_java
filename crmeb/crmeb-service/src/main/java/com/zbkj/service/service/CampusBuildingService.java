package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zbkj.common.model.campus.CampusBuilding;
import com.zbkj.common.request.CampusBuildingRequest;
import com.zbkj.common.request.PageParamRequest;

import java.util.List;

public interface CampusBuildingService extends IService<CampusBuilding> {

    List<CampusBuilding> getList(Integer schoolId, Boolean status, PageParamRequest pageParamRequest);

    List<CampusBuilding> getEnabledList(Integer schoolId);

    Boolean create(CampusBuildingRequest request);

    Boolean update(Integer id, CampusBuildingRequest request);

    Boolean updateStatus(Integer id, Boolean status);

    Boolean delete(Integer id);

    CampusBuilding getEnabledById(Integer id);
}
