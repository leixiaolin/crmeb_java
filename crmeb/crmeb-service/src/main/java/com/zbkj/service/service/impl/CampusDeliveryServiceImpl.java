package com.zbkj.service.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.campus.CampusBuilding;
import com.zbkj.common.model.campus.CampusDeliveryConfig;
import com.zbkj.common.model.campus.CampusFloorDeliveryFee;
import com.zbkj.common.request.CampusDeliveryConfigRequest;
import com.zbkj.common.request.CampusFloorDeliveryFeeRequest;
import com.zbkj.common.response.CampusDeliveryQuoteResponse;
import com.zbkj.service.dao.CampusDeliveryConfigDao;
import com.zbkj.service.dao.CampusFloorDeliveryFeeDao;
import com.zbkj.service.service.CampusBuildingService;
import com.zbkj.service.service.CampusDeliveryService;
import com.zbkj.service.service.CampusSchoolService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

@Service
public class CampusDeliveryServiceImpl extends ServiceImpl<CampusDeliveryConfigDao, CampusDeliveryConfig> implements CampusDeliveryService {

    @Resource
    private CampusFloorDeliveryFeeDao campusFloorDeliveryFeeDao;

    @Autowired
    private CampusSchoolService campusSchoolService;

    @Autowired
    private CampusBuildingService campusBuildingService;

    @Override
    public CampusDeliveryConfig getConfig(Integer schoolId) {
        campusSchoolService.getEnabledById(schoolId);
        LambdaQueryWrapper<CampusDeliveryConfig> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(CampusDeliveryConfig::getSchoolId, schoolId).eq(CampusDeliveryConfig::getIsDel, false);
        return getOne(wrapper);
    }

    @Override
    public Boolean saveConfig(CampusDeliveryConfigRequest request) {
        campusSchoolService.getEnabledById(request.getSchoolId());
        CampusDeliveryConfig config = getConfig(request.getSchoolId());
        if (ObjectUtil.isNull(config)) {
            config = new CampusDeliveryConfig();
            BeanUtils.copyProperties(request, config);
            return save(config);
        }
        BeanUtils.copyProperties(request, config);
        config.setUpdateTime(DateUtil.date());
        return updateById(config);
    }

    @Override
    public List<CampusFloorDeliveryFee> getFeeList(Integer buildingId) {
        campusBuildingService.getEnabledById(buildingId);
        LambdaQueryWrapper<CampusFloorDeliveryFee> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(CampusFloorDeliveryFee::getBuildingId, buildingId);
        wrapper.eq(CampusFloorDeliveryFee::getIsDel, false);
        wrapper.orderByAsc(CampusFloorDeliveryFee::getFloorNo);
        return campusFloorDeliveryFeeDao.selectList(wrapper);
    }

    @Override
    public Boolean saveFee(CampusFloorDeliveryFeeRequest request) {
        CampusBuilding building = campusBuildingService.getEnabledById(request.getBuildingId());
        checkFloor(building, request.getFloorNo());
        CampusFloorDeliveryFee fee = getFee(request.getBuildingId(), request.getFloorNo());
        if (ObjectUtil.isNotNull(fee)) {
            throw new CrmebException("Floor delivery fee already exists");
        }
        fee = new CampusFloorDeliveryFee();
        BeanUtils.copyProperties(request, fee);
        return campusFloorDeliveryFeeDao.insert(fee) > 0;
    }

    @Override
    public Boolean updateFee(Integer id, CampusFloorDeliveryFeeRequest request) {
        CampusFloorDeliveryFee fee = campusFloorDeliveryFeeDao.selectById(id);
        if (ObjectUtil.isNull(fee)) {
            throw new CrmebException("Floor delivery fee does not exist");
        }
        CampusBuilding building = campusBuildingService.getEnabledById(request.getBuildingId());
        checkFloor(building, request.getFloorNo());
        CampusFloorDeliveryFee exist = getFee(request.getBuildingId(), request.getFloorNo());
        if (ObjectUtil.isNotNull(exist) && !exist.getId().equals(id)) {
            throw new CrmebException("Floor delivery fee already exists");
        }
        BeanUtils.copyProperties(request, fee);
        fee.setUpdateTime(DateUtil.date());
        return campusFloorDeliveryFeeDao.updateById(fee) > 0;
    }

    @Override
    public Boolean deleteFee(Integer id) {
        if (ObjectUtil.isNull(campusFloorDeliveryFeeDao.selectById(id))) {
            throw new CrmebException("Floor delivery fee does not exist");
        }
        return campusFloorDeliveryFeeDao.deleteById(id) > 0;
    }

    @Override
    public CampusDeliveryQuoteResponse quote(Integer buildingId, Integer floorNo) {
        CampusBuilding building = campusBuildingService.getEnabledById(buildingId);
        checkFloor(building, floorNo);
        CampusFloorDeliveryFee fee = getFee(buildingId, floorNo);
        if (ObjectUtil.isNull(fee)) {
            throw new CrmebException("Current floor delivery fee is not configured");
        }
        CampusDeliveryConfig config = getConfig(building.getSchoolId());
        if (ObjectUtil.isNull(config)) {
            throw new CrmebException("Campus delivery config is not configured");
        }
        BigDecimal rainFee = config.getRainFeeEnabled() ? config.getRainFee() : BigDecimal.ZERO;
        CampusDeliveryQuoteResponse response = new CampusDeliveryQuoteResponse();
        response.setSchoolId(building.getSchoolId());
        response.setBuildingId(buildingId);
        response.setFloorNo(floorNo);
        response.setStartPrice(config.getStartPrice());
        response.setFloorDeliveryFee(fee.getDeliveryFee());
        response.setRainFeeEnabled(config.getRainFeeEnabled());
        response.setRainFee(rainFee);
        response.setDeliveryFee(fee.getDeliveryFee().add(rainFee));
        return response;
    }

    private CampusFloorDeliveryFee getFee(Integer buildingId, Integer floorNo) {
        LambdaQueryWrapper<CampusFloorDeliveryFee> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(CampusFloorDeliveryFee::getBuildingId, buildingId);
        wrapper.eq(CampusFloorDeliveryFee::getFloorNo, floorNo);
        wrapper.eq(CampusFloorDeliveryFee::getIsDel, false);
        return campusFloorDeliveryFeeDao.selectOne(wrapper);
    }

    private void checkFloor(CampusBuilding building, Integer floorNo) {
        if (floorNo > building.getMaxFloor()) {
            throw new CrmebException("Floor exceeds campus building max floor");
        }
    }
}
