package com.ruoyi.project.system.unit.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.project.system.unit.domain.DaUnit;
import com.ruoyi.project.system.unit.mapper.DaUnitMapper;
import com.ruoyi.project.system.unit.service.IDaUnitService;

@Service
public class DaUnitServiceImpl implements IDaUnitService {

    @Autowired
    private DaUnitMapper daUnitMapper;

    @Override
    public List<DaUnit> selectUnitList(DaUnit daUnit) {
        return daUnitMapper.selectUnitList(daUnit);
    }
}