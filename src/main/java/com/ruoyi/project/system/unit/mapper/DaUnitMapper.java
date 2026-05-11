package com.ruoyi.project.system.unit.mapper;

import java.util.List;
import com.ruoyi.project.system.unit.domain.DaUnit;

public interface DaUnitMapper {
    /** 查询全宗单位列表 */
    public List<DaUnit> selectUnitList(DaUnit daUnit);

    /** 根据ID查询单位 */
    public DaUnit selectUnitById(String unitid);
}