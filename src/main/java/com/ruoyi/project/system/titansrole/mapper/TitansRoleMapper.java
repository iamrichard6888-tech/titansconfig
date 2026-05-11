package com.ruoyi.project.system.titansrole.mapper;

import com.ruoyi.project.system.titansrole.domain.TitansRole;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TitansRoleMapper {
    List<TitansRole> selectRolesByCodes(@Param("codeList") List<String> codeList,
                                        @Param("roleIdList") List<String> roleIdList,
                                        @Param("filter") String filter);
}
