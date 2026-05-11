package com.ruoyi.project.system.userconfig.mapper;

import com.ruoyi.project.system.userconfig.domain.UserImportTemp;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

public interface UserImportTempMapper {
    List<UserImportTemp> selectTempList(UserImportTemp temp);
    int insertUserImportTemp(UserImportTemp temp);
    List<UserImportTemp> selectPendingList();
    String selectOrganIdByQzh(String qzh);
    int countUserByCode(String code);
    String selectRoleIdByCode(String roleCode);
    int insertTsUser(Map<String, Object> map);
    int insertTsUo(Map<String, Object> map);
    int insertTsUr(Map<String, Object> map);
    int updateTempStatus(@Param("id") String id, @Param("status") String status, @Param("errorMsg") String errorMsg);
    // 查询单条详情（用于修改回显）
    UserImportTemp selectUserImportTempById(String id);
    // 修改中间表数据
    int updateUserImportTemp(UserImportTemp temp);
    // 批量删除数据
    int deleteUserImportTempByIds(String[] ids);
    /** 查询所有带全宗号的单位列表（用于前端下拉框） */
    List<Map<String, String>> selectOrganList();
    /** 检查暂存表中是否已存在该账号 */
    int countTempByAccount(String loginAccount);
    UserImportTemp selectTempByAccount(String loginAccount);
    // 1. 查重：检查该单位下是否已经存在该角色
    String checkRoleExists(@Param("roleCode") String roleCode, @Param("unitId") String unitId);

    // 2. 查询：获取全局模板角色（UNITID 为 NULL 的角色）
    Map<String, Object> selectTemplateRole(String roleCode);

    // 3. 插入：克隆生成新的单位专属角色
    int insertTsRole(Map<String, Object> roleMap);

    // 4. 插入：克隆模板角色的所有菜单权限（排除掉特定的业务线权限）
    int cloneRolePrivileges(@Param("newRoleId") String newRoleId, @Param("templateRoleId") String templateRoleId);

    // 5. 插入：单条权限硬编码补偿（打补丁专用）
    int insertTsRp(@Param("roleId") String roleId, @Param("privilegeId") String privilegeId);
    Map<String, String> selectOrganInfoByQzh(String qzh);
}