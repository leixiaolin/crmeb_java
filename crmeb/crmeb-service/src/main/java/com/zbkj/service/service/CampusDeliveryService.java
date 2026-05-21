package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zbkj.common.model.campus.CampusDeliveryConfig;
import com.zbkj.common.model.campus.CampusFloorDeliveryFee;
import com.zbkj.common.request.CampusDeliveryConfigRequest;
import com.zbkj.common.request.CampusFloorDeliveryFeeRequest;
import com.zbkj.common.response.CampusDeliveryQuoteResponse;

import java.util.List;

public interface CampusDeliveryService extends IService<CampusDeliveryConfig> {

    CampusDeliveryConfig getConfig(Integer schoolId);

    Boolean saveConfig(CampusDeliveryConfigRequest request);

    List<CampusFloorDeliveryFee> getFeeList(Integer buildingId);

    Boolean saveFee(CampusFloorDeliveryFeeRequest request);

    Boolean updateFee(Integer id, CampusFloorDeliveryFeeRequest request);

    Boolean deleteFee(Integer id);

    CampusDeliveryQuoteResponse quote(Integer buildingId, Integer floorNo);
}
