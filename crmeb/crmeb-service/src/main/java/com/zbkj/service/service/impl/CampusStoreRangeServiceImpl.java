package com.zbkj.service.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.campus.CampusStoreRange;
import com.zbkj.common.model.system.SystemStore;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.CampusStoreRangeRequest;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.response.CampusStoreRangeResponse;
import com.zbkj.service.dao.CampusStoreRangeDao;
import com.zbkj.service.service.CampusSchoolService;
import com.zbkj.service.service.CampusStoreRangeService;
import com.zbkj.service.service.SystemStoreService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CampusStoreRangeServiceImpl extends ServiceImpl<CampusStoreRangeDao, CampusStoreRange> implements CampusStoreRangeService {

    @Autowired
    private CampusSchoolService campusSchoolService;

    @Autowired
    private SystemStoreService systemStoreService;

    @Override
    public PageInfo<CampusStoreRangeResponse> getList(Integer schoolId, Boolean status, PageParamRequest pageParamRequest) {
        campusSchoolService.getEnabledById(schoolId);
        Page<CampusStoreRange> rangePage = PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        LambdaQueryWrapper<CampusStoreRange> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(CampusStoreRange::getSchoolId, schoolId).eq(CampusStoreRange::getIsDel, false);
        if (ObjectUtil.isNotNull(status)) {
            wrapper.eq(CampusStoreRange::getStatus, status);
        }
        wrapper.orderByAsc(CampusStoreRange::getSort).orderByDesc(CampusStoreRange::getId);
        return CommonPage.copyPageInfo(rangePage, assemble(list(wrapper)));
    }

    @Override
    public List<CampusStoreRangeResponse> getEnabledList(Integer schoolId) {
        campusSchoolService.getEnabledById(schoolId);
        LambdaQueryWrapper<CampusStoreRange> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(CampusStoreRange::getSchoolId, schoolId);
        wrapper.eq(CampusStoreRange::getStatus, true).eq(CampusStoreRange::getIsDel, false);
        wrapper.orderByAsc(CampusStoreRange::getSort).orderByDesc(CampusStoreRange::getId);
        return assemble(list(wrapper)).stream()
                .filter(item -> ObjectUtil.isNotNull(item.getSystemStore()))
                .filter(item -> item.getSystemStore().getIsShow() && !item.getSystemStore().getIsDel())
                .collect(Collectors.toList());
    }

    @Override
    public List<SystemStore> getCandidateStores(String keywords, PageParamRequest pageParamRequest) {
        return systemStoreService.getList(keywords, 1, pageParamRequest);
    }

    @Override
    public Boolean create(CampusStoreRangeRequest request) {
        campusSchoolService.getEnabledById(request.getSchoolId());
        checkStore(request.getStoreId());
        if (ObjectUtil.isNotNull(getRange(request.getSchoolId(), request.getStoreId()))) {
            throw new CrmebException("Campus store range already exists");
        }
        CampusStoreRange range = new CampusStoreRange();
        BeanUtils.copyProperties(request, range);
        return save(range);
    }

    @Override
    public Boolean update(Integer id, CampusStoreRangeRequest request) {
        CampusStoreRange range = checkExist(id);
        campusSchoolService.getEnabledById(request.getSchoolId());
        checkStore(request.getStoreId());
        CampusStoreRange exist = getRange(request.getSchoolId(), request.getStoreId());
        if (ObjectUtil.isNotNull(exist) && !exist.getId().equals(id)) {
            throw new CrmebException("Campus store range already exists");
        }
        BeanUtils.copyProperties(request, range);
        range.setUpdateTime(DateUtil.date());
        return updateById(range);
    }

    @Override
    public Boolean updateStatus(Integer id, Boolean status) {
        CampusStoreRange range = checkExist(id);
        range.setStatus(status);
        range.setUpdateTime(DateUtil.date());
        return updateById(range);
    }

    @Override
    public Boolean delete(Integer id) {
        checkExist(id);
        return removeById(id);
    }

    private List<CampusStoreRangeResponse> assemble(List<CampusStoreRange> ranges) {
        List<CampusStoreRangeResponse> responses = new ArrayList<>();
        if (ranges.isEmpty()) {
            return responses;
        }
        List<Integer> storeIds = ranges.stream().map(CampusStoreRange::getStoreId).collect(Collectors.toList());
        HashMap<Integer, SystemStore> storeMap = systemStoreService.getMapInId(storeIds);
        for (CampusStoreRange range : ranges) {
            CampusStoreRangeResponse response = new CampusStoreRangeResponse();
            BeanUtils.copyProperties(range, response);
            response.setSystemStore(storeMap.get(range.getStoreId()));
            responses.add(response);
        }
        return responses;
    }

    private CampusStoreRange getRange(Integer schoolId, Integer storeId) {
        LambdaQueryWrapper<CampusStoreRange> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(CampusStoreRange::getSchoolId, schoolId);
        wrapper.eq(CampusStoreRange::getStoreId, storeId);
        wrapper.eq(CampusStoreRange::getIsDel, false);
        return getOne(wrapper);
    }

    private CampusStoreRange checkExist(Integer id) {
        CampusStoreRange range = getById(id);
        if (ObjectUtil.isNull(range)) {
            throw new CrmebException("Campus store range does not exist");
        }
        return range;
    }

    private SystemStore checkStore(Integer storeId) {
        SystemStore store = systemStoreService.getById(storeId);
        if (ObjectUtil.isNull(store) || store.getIsDel() || !store.getIsShow()) {
            throw new CrmebException("Campus store is unavailable");
        }
        return store;
    }
}
