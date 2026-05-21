package com.zbkj.service.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.campus.CampusAddress;
import com.zbkj.common.model.campus.CampusBuilding;
import com.zbkj.common.model.campus.CampusSchool;
import com.zbkj.common.request.CampusAddressRequest;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.service.dao.CampusAddressDao;
import com.zbkj.service.service.CampusAddressService;
import com.zbkj.service.service.CampusBuildingService;
import com.zbkj.service.service.CampusSchoolService;
import com.zbkj.service.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CampusAddressServiceImpl extends ServiceImpl<CampusAddressDao, CampusAddress> implements CampusAddressService {

    @Autowired
    private UserService userService;

    @Autowired
    private CampusSchoolService campusSchoolService;

    @Autowired
    private CampusBuildingService campusBuildingService;

    @Override
    public List<CampusAddress> getList(PageParamRequest pageParamRequest) {
        PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        LambdaQueryWrapper<CampusAddress> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(CampusAddress::getUid, userService.getUserIdException());
        wrapper.eq(CampusAddress::getIsDel, false);
        wrapper.orderByDesc(CampusAddress::getIsDefault).orderByDesc(CampusAddress::getId);
        return list(wrapper);
    }

    @Override
    public CampusAddress saveAddress(CampusAddressRequest request) {
        CampusSchool school = campusSchoolService.getEnabledById(request.getSchoolId());
        CampusBuilding building = campusBuildingService.getEnabledById(request.getBuildingId());
        if (!building.getSchoolId().equals(request.getSchoolId())) {
            throw new CrmebException("Campus building does not belong to campus school");
        }
        if (request.getFloorNo() > building.getMaxFloor()) {
            throw new CrmebException("Floor exceeds campus building max floor");
        }
        CampusAddress address = new CampusAddress();
        BeanUtils.copyProperties(request, address);
        address.setUid(userService.getUserIdException());
        address.setSchoolName(school.getSchoolName());
        address.setCampusName(school.getCampusName());
        address.setBuildingName(building.getBuildingName());
        if (ObjectUtil.isNotNull(request.getId())) {
            getOwnedAddress(request.getId());
            address.setUpdateTime(DateUtil.date());
        }
        if (address.getIsDefault()) {
            cancelDefault(address.getUid());
        }
        saveOrUpdate(address);
        return address;
    }

    @Override
    public Boolean delete(Integer id) {
        getOwnedAddress(id);
        return removeById(id);
    }

    @Override
    public CampusAddress getDetail(Integer id) {
        return getOwnedAddress(id);
    }

    @Override
    public CampusAddress getDefault() {
        LambdaQueryWrapper<CampusAddress> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(CampusAddress::getUid, userService.getUserId());
        wrapper.eq(CampusAddress::getIsDefault, true);
        wrapper.eq(CampusAddress::getIsDel, false);
        return getOne(wrapper);
    }

    @Override
    public Boolean setDefault(Integer id) {
        CampusAddress address = getOwnedAddress(id);
        cancelDefault(address.getUid());
        address.setIsDefault(true);
        address.setUpdateTime(DateUtil.date());
        return updateById(address);
    }

    private CampusAddress getOwnedAddress(Integer id) {
        LambdaQueryWrapper<CampusAddress> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(CampusAddress::getId, id);
        wrapper.eq(CampusAddress::getUid, userService.getUserIdException());
        wrapper.eq(CampusAddress::getIsDel, false);
        CampusAddress address = getOne(wrapper);
        if (ObjectUtil.isNull(address)) {
            throw new CrmebException("Campus address does not exist");
        }
        return address;
    }

    private void cancelDefault(Integer uid) {
        CampusAddress address = new CampusAddress();
        address.setIsDefault(false);
        LambdaQueryWrapper<CampusAddress> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(CampusAddress::getUid, uid);
        wrapper.eq(CampusAddress::getIsDel, false);
        update(address, wrapper);
    }
}
