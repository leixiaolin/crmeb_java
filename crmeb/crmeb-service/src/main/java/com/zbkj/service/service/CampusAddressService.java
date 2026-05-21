package com.zbkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zbkj.common.model.campus.CampusAddress;
import com.zbkj.common.request.CampusAddressRequest;
import com.zbkj.common.request.PageParamRequest;

import java.util.List;

public interface CampusAddressService extends IService<CampusAddress> {

    List<CampusAddress> getList(PageParamRequest pageParamRequest);

    CampusAddress saveAddress(CampusAddressRequest request);

    Boolean delete(Integer id);

    CampusAddress getDetail(Integer id);

    CampusAddress getDefault();

    Boolean setDefault(Integer id);
}
