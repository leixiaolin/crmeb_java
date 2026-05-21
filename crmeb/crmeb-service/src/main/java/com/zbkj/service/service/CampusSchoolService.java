package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zbkj.common.model.campus.CampusSchool;
import com.zbkj.common.request.CampusSchoolRequest;
import com.zbkj.common.request.PageParamRequest;

import java.util.List;

public interface CampusSchoolService extends IService<CampusSchool> {

    List<CampusSchool> getList(String keywords, Boolean status, PageParamRequest pageParamRequest);

    List<CampusSchool> getEnabledList();

    Boolean create(CampusSchoolRequest request);

    Boolean update(Integer id, CampusSchoolRequest request);

    Boolean updateStatus(Integer id, Boolean status);

    Boolean delete(Integer id);

    CampusSchool getEnabledById(Integer id);
}
