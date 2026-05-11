package com.ruoyi.project.system.file.mapper;

import com.ruoyi.project.system.file.domain.FilesData;
import com.ruoyi.project.system.user.domain.User;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 用户表 数据层
 * 
 * @author ruoyi
 */
public interface FilesDataMapper
{
    /**
     * 根据条件分页查询用户列表
     * 
     * @param user 用户信息
     * @return 用户信息集合信息
     */
//	@Select("select id,DELETE_USER as isDelete,${titlecode} as title,${qzcode} as qzcode,${archiveCode} as archiveCode from ${tableName} where ${filter}")
//    public List<FilesData> selectFilesDataList(@Param("titlecode") String titlecode,@Param("qzcode") String qzcode,@Param("archiveCode") String archiveCode,@Param("tableName") String tableName,@Param("filter") String filter);
	
	@Select("select WG00 as id,ISCOPY,${titlecode} as title,${qzcode} as qzcode,${archiveCode} as archiveCode from ${tableName} where  ${filter}")
    public List<FilesData> selectFilesDataList(@Param("titlecode") String titlecode,@Param("qzcode") String qzcode,@Param("archiveCode") String archiveCode,@Param("tableName") String tableName,@Param("filter") String filter);
	
//	@Select("select column_name,show_name from T_XTPZ_COLUMN_DEFINE where show_name in ('档号','题名','全宗','全宗号','电子文件') and table_id = #{tableid}")
//    public List<Map<String,String>> selectColumnList(String tableid);
	
	@Select("select FIELDCODE as COLUMN_NAME,FIELDNAME as SHOW_NAME from T_DA_TEMPLATE where FIELDNAME in ('档号','题名','案卷题名','全宗号','电子文件') and SORTID = #{tableid}")
    public List<Map<String,String>> selectColumnList(String tableid);
	
	@Select("select column_name,show_name from T_XTPZ_COLUMN_DEFINE where show_name = '电子文件' and table_id = #{tableid}")
    public List<Map<String,String>> selectDzqwColumn(String tableid);
	
	@Select("select id,DELETE_USER as idDelete,${titlecode} as title,${dzwj} as dzwj,${archiveCode} as archiveCode from ${tableName} where ${filter}")
    public List<FilesData> selectFilesDataListByIds(@Param("titlecode") String titlecode,@Param("dzwj") String dzwj,@Param("archiveCode") String archiveCode,@Param("tableName") String tableName,@Param("filter") String filter);
	
	@Select("select id,path,filename from ${tableName} where mainid = #{mainid}")
    public List<Map<String,String>> selectDzwjList(@Param("tableName") String tableName,@Param("mainid") String mainid);
	
	@Update("update ${tableName} set DELETE_USER = '00' where ${filter} ")
	public void updatedownloadflag(@Param("tableName") String tableName,@Param("filter") String filter);
	
	@Select("select * from ${tableName} where ${filter}")
    public List<Map> selectDataVO(@Param("tableName") String tableName,@Param("filter") String filter);

	@Select("SELECT a.*,b.LOCATION FROM ${storeTableName} a LEFT JOIN T_ER_STORE b ON a.STOREID =b.ID WHERE a.id in(SELECT STOREITEMID FROM ${tablename} WHERE erid ='${erid}' )")
    public List<Map> getFileList(@Param("erid") String erid,@Param("tablename") String tablename,@Param("storeTableName") String storeTableName);
	
	@Update("update ${tablename} set iscopy = 'true' where erid = '${erid}' ")
	public void updateCopy(@Param("erid") String erid,@Param("tablename") String tablename);
	
	@Update("update ${storeTableName} set copyUrl = #{copyUrl} where id = '${id}' ")
	public void updateCopyUrl(@Param("id") String id,@Param("copyUrl") String copyUrl,@Param("storeTableName") String storeTableName);
	
	@Select("SELECT * FROM T_DA_KEYSET_TONEWSYS WHERE WG10 ='${sortid}' ORDER BY WG01 ASC")
	public List<Map> getKeysetList(@Param("sortid") String sortid);
	
	@Select("SELECT * FROM T_ALLUNIT WHERE unitid ='${unitid}' ")
	public List<Map> selectUnitList(@Param("unitid") String unitid);

	@Select("SELECT * FROM T_ALLUNIT_HAVEDATA order by TO_NUMBER(levelord)")
	public List<Map> selectAllUnitHavedataList();




}
