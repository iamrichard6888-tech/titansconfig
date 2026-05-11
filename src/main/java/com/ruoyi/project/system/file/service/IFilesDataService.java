package com.ruoyi.project.system.file.service;

import com.ruoyi.project.system.file.domain.FilesData;
import com.ruoyi.project.system.user.domain.User;
import com.ruoyi.project.system.user.domain.UserRole;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 用户 业务层
 * 
 * @author ruoyi
 */
public interface IFilesDataService
{
    /**
     * 根据条件分页查询用户列表
     * 
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    public List<FilesData> selectFilesDataList(FilesData filesData);
    
    
    /**
     * 根据数据id打包文件
     * 
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    public String packagefile(String ids,String deptid);
    
    public String packagefileall(String unitid,String sortid,String carriertype,String strutstype);
    /**
     * 获取全局多线程导出状态
     */
    boolean getGlobalExportStatus();

    /**
     * 停止全局多线程导出
     */
    void stopGlobalExport();


}
