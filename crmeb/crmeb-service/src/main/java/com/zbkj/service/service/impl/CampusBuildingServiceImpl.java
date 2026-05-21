package com.zbkj.service.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.campus.CampusBuilding;
import com.zbkj.common.request.CampusBuildingRequest;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.service.dao.CampusBuildingDao;
import com.zbkj.service.service.CampusBuildingService;
import com.zbkj.service.service.CampusSchoolService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CampusBuildingServiceImpl extends ServiceImpl<CampusBuildingDao, CampusBuilding> implements CampusBuildingService {

    @Autowired
    private CampusSchoolService campusSchoolService;

    @Override
    public List<CampusBuilding> getList(Integer schoolId, Boolean status, PageParamRequest pageParamRequest) {
        PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        LambdaQueryWrapper<CampusBuilding> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(CampusBuilding::getIsDel, false);
        if (ObjectUtil.isNotNull(schoolId) && schoolId > 0) {
            wrapper.eq(CampusBuilding::getSchoolId, schoolId);
        }
        if (ObjectUtil.isNotNull(status)) {
            wrapper.eq(CampusBuilding::getStatus, status);
        }
        wrapper.orderByAsc(CampusBuilding::getSort).orderByAsc(CampusBuilding::getBuildingName);
        return list(wrapper);
    }

    @Override
    public List<CampusBuilding> getEnabledList(Integer schoolId) {
        campusSchoolService.getEnabledById(schoolId);
        LambdaQueryWrapper<CampusBuilding> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(CampusBuilding::getSchoolId, schoolId);
        wrapper.eq(CampusBuilding::getStatus, true).eq(CampusBuilding::getIsDel, false);
        wrapper.orderByAsc(CampusBuilding::getSort).orderByAsc(CampusBuilding::getBuildingName);
        return list(wrapper);
    }

    @Override
    public Boolean create(CampusBuildingRequest request) {
        campusSchoolService.getEnabledById(request.getSchoolId());
        CampusBuilding building = new CampusBuilding();
        BeanUtils.copyProperties(request, building);
        return save(building);
    }

    @Override
    public Boolean update(Integer id, CampusBuildingRequest request) {
        checkExist(id);
        campusSchoolService.getEnabledById(request.getSchoolId());
        CampusBuilding building = new CampusBuilding();
        BeanUtils.copyProperties(request, building);
        building.setId(id);
        building.setUpdateTime(DateUtil.date());
        return updateById(building);
    }

    @Override
    public Boolean updateStatus(Integer id, Boolean status) {
        CampusBuilding building = checkExist(id);
        building.setStatus(status);
        building.setUpdateTime(DateUtil.date());
        return updateById(building);
    }

    @Override
    public Boolean delete(Integer id) {
        checkExist(id);
        return removeById(id);
    }

    @Override
    public CampusBuilding getEnabledById(Integer id) {
        CampusBuilding building = checkExist(id);
        if (!building.getStatus()) {
            throw new CrmebException("Campus building is unavailable");
        }
        campusSchoolService.getEnabledById(building.getSchoolId());
        return building;
    }

    private CampusBuilding checkExist(Integer id) {
        CampusBuilding building = getById(id);
        if (ObjectUtil.isNull(building)) {
            throw new CrmebException("Campus building does not exist");
        }
        return building;
    }
}
