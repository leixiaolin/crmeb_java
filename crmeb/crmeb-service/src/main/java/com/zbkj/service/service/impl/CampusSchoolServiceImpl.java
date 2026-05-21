package com.zbkj.service.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.campus.CampusSchool;
import com.zbkj.common.request.CampusSchoolRequest;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.service.dao.CampusSchoolDao;
import com.zbkj.service.service.CampusSchoolService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CampusSchoolServiceImpl extends ServiceImpl<CampusSchoolDao, CampusSchool> implements CampusSchoolService {

    @Override
    public List<CampusSchool> getList(String keywords, Boolean status, PageParamRequest pageParamRequest) {
        PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        LambdaQueryWrapper<CampusSchool> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(CampusSchool::getIsDel, false);
        if (ObjectUtil.isNotNull(status)) {
            wrapper.eq(CampusSchool::getStatus, status);
        }
        if (StringUtils.isNotBlank(keywords)) {
            wrapper.and(query -> query.like(CampusSchool::getSchoolName, keywords)
                    .or().like(CampusSchool::getCampusName, keywords));
        }
        wrapper.orderByAsc(CampusSchool::getSort).orderByDesc(CampusSchool::getId);
        return list(wrapper);
    }

    @Override
    public List<CampusSchool> getEnabledList() {
        LambdaQueryWrapper<CampusSchool> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(CampusSchool::getStatus, true).eq(CampusSchool::getIsDel, false);
        wrapper.orderByAsc(CampusSchool::getSort).orderByDesc(CampusSchool::getId);
        return list(wrapper);
    }

    @Override
    public Boolean create(CampusSchoolRequest request) {
        CampusSchool school = new CampusSchool();
        BeanUtils.copyProperties(request, school);
        return save(school);
    }

    @Override
    public Boolean update(Integer id, CampusSchoolRequest request) {
        checkExist(id);
        CampusSchool school = new CampusSchool();
        BeanUtils.copyProperties(request, school);
        school.setId(id);
        school.setUpdateTime(DateUtil.date());
        return updateById(school);
    }

    @Override
    public Boolean updateStatus(Integer id, Boolean status) {
        CampusSchool school = checkExist(id);
        school.setStatus(status);
        school.setUpdateTime(DateUtil.date());
        return updateById(school);
    }

    @Override
    public Boolean delete(Integer id) {
        checkExist(id);
        return removeById(id);
    }

    @Override
    public CampusSchool getEnabledById(Integer id) {
        CampusSchool school = getById(id);
        if (ObjectUtil.isNull(school) || !school.getStatus()) {
            throw new CrmebException("Campus school is unavailable");
        }
        return school;
    }

    private CampusSchool checkExist(Integer id) {
        CampusSchool school = getById(id);
        if (ObjectUtil.isNull(school)) {
            throw new CrmebException("Campus school does not exist");
        }
        return school;
    }
}
