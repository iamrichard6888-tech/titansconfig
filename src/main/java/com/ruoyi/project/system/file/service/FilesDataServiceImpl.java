package com.ruoyi.project.system.file.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.ExceptionUtil;
import com.ruoyi.common.utils.Md5Utils;
import com.ruoyi.common.utils.PageUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.bean.BeanValidators;
import com.ruoyi.common.utils.file.FileUtils;
import com.ruoyi.common.utils.file.LinaglaZip;
import com.ruoyi.common.utils.html.EscapeUtil;
import com.ruoyi.common.utils.security.ShiroUtils;
import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.common.utils.text.Convert;
import com.ruoyi.framework.aspectj.lang.annotation.DataScope;
import com.ruoyi.framework.config.RuoYiConfig;
import com.ruoyi.framework.shiro.service.PasswordService;
import com.ruoyi.project.system.config.service.IConfigService;
import com.ruoyi.project.system.dept.service.IDeptService;
import com.ruoyi.project.system.file.domain.FilesData;
import com.ruoyi.project.system.file.mapper.FilesDataMapper;
import com.ruoyi.project.system.post.domain.Post;
import com.ruoyi.project.system.post.mapper.PostMapper;
import com.ruoyi.project.system.role.domain.Role;
import com.ruoyi.project.system.role.mapper.RoleMapper;
import com.ruoyi.project.system.sort.domain.Sort;
import com.ruoyi.project.system.sort.mapper.SortMapper;
import com.ruoyi.project.system.user.domain.User;
import com.ruoyi.project.system.user.domain.UserPost;
import com.ruoyi.project.system.user.domain.UserRole;
import com.ruoyi.project.system.user.mapper.UserMapper;
import com.ruoyi.project.system.user.mapper.UserPostMapper;
import com.ruoyi.project.system.user.mapper.UserRoleMapper;

/**
 * 用户 业务层处理
 * 
 * @author ruoyi
 */
@Service
public class FilesDataServiceImpl implements IFilesDataService
{
    private static final Logger log = LoggerFactory.getLogger(FilesDataServiceImpl.class);

    @Autowired
    private FilesDataMapper filesDataMapper;

    @Autowired
    private SortMapper sortMapper;

	// 1. 全局运行状态（所有用户共享，保证并发安全）
	private final AtomicBoolean globalIsRunning = new AtomicBoolean(false);

	// 2. 全局线程池（根据你的 I/O 测试，建议设置为 4-8 个线程）
	private ExecutorService exportThreadPool;

	@Override
	public boolean getGlobalExportStatus() {
		return globalIsRunning.get();
	}
    /**
     * 根据条件分页查询用户列表
     * 
     * @param user 用户信息
     * @return 用户信息集合信息
     *//*
    @Override
    public List<FilesData> selectFilesDataList(FilesData filesData)
    {
    	if("1".equals(filesData.getDeptId()) || "".equals(filesData.getDeptId())){
    		return new ArrayList<FilesData>();
    	}
    	//分类id
    	//String sortid = filesData.getDeptId();
    	
    	Sort sort = new Sort();
    	String depId = filesData.getDeptId();
    	sort.setId(filesData.getDeptId());
    	//根据分类获取分类对象
    	List<Sort> sortList = sortMapper.selectSortList(sort);
    	
    	if(sortList != null && sortList.size()>0){
    		sort = sortList.get(0);
    	}
    	
    	String sortid = "";
    	String unitid = "";
    	String[] depIds = depId.split("-");
    	sortid = depIds[0];
    	if(depIds.length>1){
    		unitid = depIds[1];
    	}
    	
    	
    	
    	String tableName = sort.getTableName();
    	String tableId = sort.getTableId();
    	
    	if(tableId == null || "".equals(tableId)){
    		return new ArrayList<FilesData>();
    	}
    	
    	//根据tableId查询字段定义列名
    	List<Map<String,String>> columnMaps = filesDataMapper.selectColumnList(sortid);
    	
    	if(columnMaps == null || columnMaps.size()==0){
    		return new ArrayList<FilesData>();
    	}
    	
    	//Map<String,String> columnMap = columnMaps.get(0);
    	String titlecode = "NULL",qzcode="NULL",dh = "NULL";
    	
    	for(Map<String,String> columnMap : columnMaps){
    		
    		//String columnName = columnMap.get("column_name");
    		//String showName = columnMap.get("show_name");
    		String columnName = columnMap.get("COLUMN_NAME");
    		String showName = columnMap.get("SHOW_NAME");
    		
    		if("档号".equals(showName)){
    			dh = columnName;
    		}else if("题名".equals(showName)){
    			titlecode = columnName;
    		}else if("全宗".equals(showName)||"全宗号".equals(showName)){
    			qzcode = columnName;
    		}
    		
    	}
    	
//    	String filter = " IS_DELETE = '0' ";
    	String filter = " sortid = '"+sortid+"' and unitid = '"+unitid+"' ";
    	if(sort.getLevel()==2){
    		filter += " and " + qzcode +" = '"+sort.getCode()+"'";
    	}
    	
    	if(filesData.getArchiveCode() != null && !"".equals(filesData.getArchiveCode())){
    		filter += " and "+dh+ " like '%"+filesData.getArchiveCode()+"%' " ;
    	}
    	
    	if(filesData.getQzcode() != null && !"".equals(filesData.getQzcode())){
    		filter += " and "+qzcode+ " like '%"+filesData.getQzcode()+"%' " ;
    	}
    	
        // 生成数据权限过滤条件
    	
    	//设置分页查询
    	PageUtils.startPage();
        return filesDataMapper.selectFilesDataList(titlecode,qzcode,dh,tableName,filter);
    	//return new ArrayList<FilesData>();
    }*/
    
    //@Override
    public List<FilesData> selectFilesDataList2(FilesData filesData)
    {
    	if("1".equals(filesData.getDeptId()) || "".equals(filesData.getDeptId())){
    		return new ArrayList<FilesData>();
    	}
    	//分类id
    	//String sortid = filesData.getDeptId();
    	
    	String carriertype = filesData.getCarriertype();
    	String strutstype = filesData.getStrutstype();
    	String unitid = filesData.getUnitid();
    	String sortid = filesData.getSortid();
    	
    	//根据tableId查询字段定义列名
    	List<Map<String,String>> columnMaps = filesDataMapper.selectColumnList(sortid);
    	
    	if(columnMaps == null || columnMaps.size()==0){
    		return new ArrayList<FilesData>();
    	}
    	
    	String titlecode = "WG35",qzcode="WG03",dh = "WG02";
    	
/*    	for(Map<String,String> columnMap : columnMaps){
    		String columnName = columnMap.get("COLUMN_NAME");
    		String showName = columnMap.get("SHOW_NAME");
    		
    		if("档号".equals(showName)){
    			dh = columnName;
    		}else if("题名".equals(showName)){
    			titlecode = columnName;
    		}else if("全宗".equals(showName)||"全宗号".equals(showName)){
    			qzcode = columnName;
    		}
    		
    	}*/
    	
    	String filter = " sortid = '"+sortid+"' and unitid = '"+unitid+"' and carriertype = '"+carriertype+"' and strutstype = '"+strutstype+"' ";
    	
    	if("ERRECORD".equalsIgnoreCase(carriertype)){
    		filter += " and GLSTATE = 'GL' ";
    	}
    	
        // 生成数据权限过滤条件
    	
    	String qzcodeData = filesData.getQzcode();
    	if(qzcodeData!=null&&!qzcodeData.isEmpty()){
    		filter += " and "+qzcode+" like '%"+qzcodeData+"%' ";
    	}
    	String archiveCodeData = filesData.getArchiveCode();
    	if(archiveCodeData!=null&&!archiveCodeData.isEmpty()){
    		filter += " and "+dh+" like '%"+archiveCodeData+"%' ";
    	}
		String titleData = filesData.getTitle();
		if(titleData!=null&&!titleData.isEmpty()&&!titleData.equals("")){
			filter += " and "+titlecode+" like '%"+titleData+"%' ";
		}
    	
    	List<Map> sortMaps = filesDataMapper.selectUnitList(unitid);
    	String tablename = "t_da_data_TONEWSYS";
/*		if(sortMaps.size()>0){
			tablename += "_TONEWSYS";
		}*/
    	
    	//设置分页查询
    	PageUtils.startPage();
        return filesDataMapper.selectFilesDataList(titlecode,qzcode,dh,tablename,filter);
    	//return new ArrayList<FilesData>();
    }

	@Override
	public List<FilesData> selectFilesDataList(FilesData filesData)
	{
		/*if("1".equals(filesData.getDeptId()) || "".equals(filesData.getDeptId())){
			return new ArrayList<FilesData>();
		}*/
		//分类id
		//String sortid = filesData.getDeptId();


		String carriertype = filesData.getCarriertype();
		String strutstype = filesData.getStrutstype();
		String unitid = filesData.getUnitid();
		String sortid = filesData.getSortid();

		String filter = "1 = 1";

		if (!unitid.isEmpty() && unitid != null ) {
			filter+=" and unitid = '"+unitid+"'";
		}
		if (!carriertype.isEmpty() && carriertype != null ) {
			filter+=" and carriertype = '"+carriertype+"'";
		}
		if("ERRECORD".equalsIgnoreCase(carriertype)){
			filter += " and GLSTATE = 'GL' ";
		}
		if (!strutstype.isEmpty() && strutstype != null ) {
			filter+=" and strutstype = '"+strutstype+"'";
		}
		if (!sortid.isEmpty() && sortid != null ) {
			filter+=" and sortid = '"+sortid+"'";
		}

		//根据tableId查询字段定义列名
//		List<Map<String,String>> columnMaps = filesDataMapper.selectColumnList(sortid);

/*		if(columnMaps == null || columnMaps.size()==0){
			return new ArrayList<FilesData>();
		}*/

		String titlecode = "WG35",qzcode="WG03",dh = "WG02";

/*    	for(Map<String,String> columnMap : columnMaps){
    		String columnName = columnMap.get("COLUMN_NAME");
    		String showName = columnMap.get("SHOW_NAME");

    		if("档号".equals(showName)){
    			dh = columnName;
    		}else if("题名".equals(showName)){
    			titlecode = columnName;
    		}else if("全宗".equals(showName)||"全宗号".equals(showName)){
    			qzcode = columnName;
    		}

    	}*/



		// 生成数据权限过滤条件



/*		String qzcodeData = filesData.getQzcode();
		if(qzcodeData!=null&&!qzcodeData.isEmpty()){
			if (qzcodeData.indexOf(",")>0){
				String[] qzcodeSplit = qzcodeData.split(",");
				int qzcodeLength = qzcodeSplit.length;
				if (qzcodeLength > 1){
					filter += " and ( ";
					for (int i = 0; i < qzcodeLength; i++) {
						String qzhfilter =  qzcode+" like '%"+qzcodeSplit[i]+"%' ";
						filter += qzhfilter;
						if (i != qzcodeLength - 1){
							filter += " or ";
						}
					}
					filter += " )";
				}else{
					filter += " and "+qzcode+" like '%"+qzcodeSplit[0]+"%' ";
				}
			} else if (qzcodeData.indexOf("，")>0) {

			}else {
				filter += " and "+qzcode+" like '%"+qzcodeData+"%' ";
			}
		}*/

		//搜索框的全宗号搜索
		String qzcodeData = filesData.getQzcode();
		String selectUnitidByQzcodeFilter = " 1 = 1 ";
		if(qzcodeData!=null&&!qzcodeData.isEmpty()){
			if (qzcodeData.indexOf(",")>0){
				String[] qzcodeSplit = qzcodeData.split(",");
				int qzcodeLength = qzcodeSplit.length;
				if (qzcodeLength > 1){
					selectUnitidByQzcodeFilter += " and ( ";
					for (int i = 0; i < qzcodeLength; i++) {
						String qzhfilter =  " qzh like '%"+qzcodeSplit[i]+"%' ";
						selectUnitidByQzcodeFilter += qzhfilter;
						if (i != qzcodeLength - 1){
							selectUnitidByQzcodeFilter += " or ";
						}
					}
					selectUnitidByQzcodeFilter += " )";
				}else{
					selectUnitidByQzcodeFilter += " and qzh like '%"+qzcodeSplit[0]+"%' ";
				}
			} else if (qzcodeData.indexOf("，")>0) {

			}else {
				selectUnitidByQzcodeFilter += " and qzh like '%"+qzcodeData+"%' ";
			}
			List<Map> searchUnits = filesDataMapper.selectDataVO("T_ALLUNIT", selectUnitidByQzcodeFilter);
			String searchUnitidfilter = "";
			for (int i = 0; i < searchUnits.size(); i++) {
				searchUnitidfilter += "'"+searchUnits.get(i).get("UNITID").toString()+"'";
				if (i != searchUnits.size() - 1){
					searchUnitidfilter += ",";
				}
			}
			filter += " and unitid in ("+searchUnitidfilter+") ";
		}


		String archiveCodeData = filesData.getArchiveCode();
		if(archiveCodeData!=null&&!archiveCodeData.isEmpty()){
			if (archiveCodeData.indexOf(",")>0){
				String[] archiveCodeDataSplit = archiveCodeData.split(",");
				int archiveCodeLength = archiveCodeDataSplit.length;
				if (archiveCodeLength > 1){
					filter += " and ( ";
					for (int i = 0; i < archiveCodeLength; i++) {
						String archiveCodefilter =  dh+" like '%"+archiveCodeDataSplit[i]+"%' ";
						filter += archiveCodefilter;
						if (i != archiveCodeLength - 1){
							filter += " or ";
						}
					}
					filter += " )";
				}else{
					filter += " and "+dh+" like '%"+archiveCodeDataSplit[0]+"%' ";
				}
			} else if (archiveCodeData.indexOf("，")>0) {

			}else {
				filter += " and "+dh+" like '%"+archiveCodeData+"%' ";
			}
		}

		String titleData = filesData.getTitle();
		if(titleData!=null&&!titleData.isEmpty()){
			filter += " and "+titlecode+" like '%"+titleData+"%' ";
		}


		List<Map> sortMaps = filesDataMapper.selectUnitList(unitid);//查询unit
		String tablename = "t_da_data_TONEWSYS";
/*		if(sortMaps.size()>0){
			tablename += "_TONEWSYS";
		}*/
		if (RuoYiConfig.isSeparateEnabled()){
			if (unitid!=null&&!unitid.isEmpty()){
				System.out.println(unitid);
				List<Map> maps = filesDataMapper.selectUnitList(unitid);
				tablename = "t_da_data_for_"+maps.get(0).get("QZH");
			}else {
				return new ArrayList<FilesData>();
			}
		}

		//设置分页查询
		PageUtils.startPage();
		System.out.println(filter);
		List<FilesData> result = filesDataMapper.selectFilesDataList(titlecode, qzcode, dh, tablename, filter);
		return result;
		//return new ArrayList<FilesData>();
	}


/*	@Override
	public String packagefile(String ids,String deptid) {
		// TODO Auto-generated method stub
		//创建批次文件夹
		String username = ShiroUtils.getSysUser().getLoginName();
		String downloadPrePath = "Z:\\download\\"+username+"\\";
		//Date now = new Date();
		//String time = now.getYear()+1900+""+now.getMonth()+1+""+now.getDate()+""+now.getHours()+""+now.getMinutes()+""+now.getSeconds();
		
		LocalDateTime now = LocalDateTime.now();
		String time = now.getYear()+""+now.getMonthValue()+""+now.getDayOfMonth()+""+now.getHour()+""+now.getMinute()+""+now.getSecond();
		//String filename = UUID.randomUUID() + "_电子原文.zip";
		String downloadPath =  downloadPrePath+"电子原文"+time;
        File desc = new File(downloadPath);
        if (!desc.getParentFile().exists())
        {
            desc.getParentFile().mkdirs();
        }
		
		//根据分类id 获取分类对象 拿到表名
        
       

    	Sort sort = new Sort();
    	sort.setId(deptid);
    	//根据分类获取分类对象
    	List<Sort> sortList = sortMapper.selectSortList(sort);
    	
    	if(sortList != null && sortList.size()>0){
    		sort = sortList.get(0);
    	}
    	
    	String tableName = sort.getTableName();
    	String tableId = sort.getTableId();
		
    	//根据tableId查询电子文件列名
    	//List<Map<String,String>> columnMaps = filesDataMapper.selectDzqwColumn(tableId);
    	
    	//根据tableId查询字段定义列名
    	List<Map<String,String>> columnMaps = filesDataMapper.selectColumnList(tableId);
    	
    	String titlecode = "NULL",dh = "NULL",dzwj = "NULL";
    	
    	for(Map<String,String> columnMap : columnMaps){
    		
    		String columnName = columnMap.get("column_name");
    		String showName = columnMap.get("show_name");
    		
    		if("档号".equals(showName)){
    			dh = columnName;
    		}else if("题名".equals(showName)){
    			titlecode = columnName;
    		}else if("电子文件".equals(showName)){
    			dzwj = columnName;
    		}
    		
    	}
    	
    	if("NULL".equals(dzwj)){
    		return null;
    	}
    	
    	String filter = " IS_DELETE ='0' and id in("+ids+")";
    	
    	
    	List<FilesData> datalist = filesDataMapper.selectFilesDataListByIds(titlecode,dzwj,dh,tableName,filter);
    	
    	if(datalist == null || datalist.size() == 0){
    		return null;
    	}
    	
    	//遍历数据查询文件表
    	int index = tableName.lastIndexOf("_");
    	String documenttable = tableName.substring(0, index)+"_DOCUMENT";
    	try{
    		for(FilesData data : datalist){
        		List<Map<String,String>> dzwjlist = filesDataMapper.selectDzwjList(documenttable,data.getDzwj());
        		if(dzwjlist != null && dzwjlist.size()>0){
        			//创建文件夹 用档号命名 没有则用题名 没有再用id创建文件夹 
        			String foldername = data.getArchiveCode();
        			if(foldername == null || "".equals(foldername)){
        				foldername = data.getTitle();
        			}
        			
        			if(foldername == null || "".equals(foldername)){
        				foldername = data.getId();
        			}
        			
        			//创建目录
        			String filepath = downloadPath+"/"+foldername;
        					
        			File descpath = new File(filepath);
        	        if (!descpath.exists())
        	        {
        	        	descpath.mkdirs();
        	        }
        	        
        	        
        	        String prepath = "Z:\\局域网电子文件\\";
        	        
        	        for(Map<String,String> filemap:dzwjlist ){
        	        	String sourceFilePath = prepath + filemap.get("PATH");
        	        	
        	        	Path sourcePath = Paths.get(sourceFilePath);
        	        	
        	        	Path targetPath = Paths.get(filepath+"\\"+filemap.get("filename"));
        	        	
        	        	//复制文件
        	        	Files.copy(sourcePath, targetPath,StandardCopyOption.REPLACE_EXISTING);
        	        }
        	        
        	        //复制文件到路径下
        		}
        	}
    		
    		//更新下载标记
    		filesDataMapper.updatedownloadflag(tableName, filter);
    		//压缩文件
    		//LinaglaZip.zip(downloadPath, RuoYiConfig.getDownloadPath()+filename, null);
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	
    	
		return downloadPath;
	}
*/
	public String packagefile(String ids,String deptid) {
		// TODO Auto-generated method stub
		String dataFilter = "wg00 in ("+ids+") and (ISCOPY != 'true' OR ISCOPY IS NULL)";
		

    	String tablename = "t_da_data_TONEWSYS";
/*		if(sortMaps.size()>0){
			tablename += "_TONEWSYS";
		}*/
		if (RuoYiConfig.isSeparateEnabled()){
			if (deptid!=null&&!deptid.isEmpty()){
				System.out.println(deptid);
				List<Map> maps = filesDataMapper.selectUnitList(deptid);
				tablename = "t_da_data_for_"+maps.get(0).get("QZH");
			}
		}
		List<Map> dataMap = filesDataMapper.selectDataVO(tablename,dataFilter);

		for(Map m:dataMap){
			List<Map> sortMaps = filesDataMapper.selectUnitList(m.get("UNITID").toString());
			String qzh = sortMaps.get(0).get("QZH").toString();
			System.out.println(m.get("ERID"));
			System.out.println(RuoYiConfig.getCopyPath());
			boolean copySrcFlag = copyFile(m,"T_ER_SRCLIST_FOR_"+qzh,"T_ER_STOREITEM_FOR_"+qzh);
//			boolean copySlyFlag = copyFile(m,"T_ER_SLYLIST");
			//boolean copyStdFlag = copyFile(m,"T_ER_STDLIST_FOR_P040","T_ER_STOREITEM_TONEWSYS"); 不迁移固化了 所以注释掉
			boolean copyStdFlag = true;
			if(copySrcFlag&&copyStdFlag){
				//update data表的iscopy字段
				filesDataMapper.updateCopy((String) m.get("ERID"),tablename);
			}
		}
		
		return RuoYiConfig.getProfile();
	}
	
	/**
	 * 复制文件
	 * @param data
	 * @param tableName
	 * @return
	 */
	private boolean copyFile(Map data,String tableName,String storeTableName){
		boolean success = true;
		List<File> fileList = new ArrayList<File>();
		List<Map> storeMap = filesDataMapper.getFileList((String) data.get("ERID"), tableName,storeTableName);
		for(Map m:storeMap){
			String filePath = (String)m.get("LOCATION")+File.separatorChar+(String)m.get("URL");//原路径
			String fileName = FileUtils.getName(filePath);
			String rootPath = RuoYiConfig.getProfile()+ File.separatorChar+tableName;
			String copyUrl = (String) data.get("UNITFULLNAME")+File.separatorChar+//全宗
					(String)data.get("SORTFULLNAME")+File.separatorChar+//分类
					getDahaoPath(data)+File.separatorChar+//档号
					DateUtils.datePath()+File.separatorChar+//日期
					(String )data.get("ERID")+File.separatorChar+//ERID
					fileName; 
			/*String copyUrl = (String) data.get("UNITFULLNAME")+File.separatorChar+//全宗
					(String )data.get("SORTFULLNAME")+File.separatorChar+//分类
					getDahaoPath(data)+File.separatorChar+//档号
					(String )m.get("URL");*/
			String outputPath = rootPath+File.separatorChar+copyUrl;
			System.out.println("原文件路径："+filePath);
			System.out.println("新文件路径："+outputPath);
			try {
				FileUtils.copyFile(filePath,outputPath);
				File inputf = new File(filePath);
				File outputf = new File(outputPath);
				if(inputf.exists()&&!outputf.exists()){//源文件有，复制文件没有
					success=false;
				}else{
					//复制成功，更新storeitem的copyurl字段
					filesDataMapper.updateCopyUrl((String)m.get("ID"),copyUrl.replace("/", "\\"),storeTableName);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				success=false;
			}
		}
		return success;
	}
	
	/**
	 * 获取档号层级路径
	 * @param m
	 * @return
	 */
	private String getDahaoPath(Map m){
		String result = "";
		List<Map> keysetMap = filesDataMapper.getKeysetList((String)m.get("SORTID"));
		for(int i=0;i<keysetMap.size();i++){
			if(i>0){
				result+=File.separatorChar;
			}
			result += m.get(((String)keysetMap.get(i).get("WG09")).toUpperCase());
		}
		return result;
	}


	/*@Override
	public String packagefileall(String unitid, String sortid, String carriertype, String strutstype) {
		// TODO Auto-generated method stub

		//String dataFilter = "unitid = '"+unitid+"' and sortid = '"+sortid+"' and carriertype='"+carriertype+"' and strutstype='"+strutstype+"' and iscopy is null";
		String dataFilter = " 1 = 1";
		if (unitid!=null&&!unitid.isEmpty()){
			dataFilter += " and unitid = '"+unitid+"'";
		}
		if (sortid!=null&&!sortid.isEmpty()){
			dataFilter += " and sortid = '"+sortid+"'";
		}
		if (carriertype!=null&&!carriertype.isEmpty()){
			dataFilter += " and carriertype = '"+carriertype+"'";
		}
		if (strutstype!=null&&!strutstype.isEmpty()){
			dataFilter += " and strutstype = '"+strutstype+"'";
		}
		dataFilter += " and iscopy is null";

		//String dataFilter = "unitid = '"+unitid+"' and sortid = '"+sortid+"' and carriertype='"+carriertype+"' and strutstype='"+strutstype+"' and iscopy is null";
		if("ERRECORD".equalsIgnoreCase(carriertype)){
			dataFilter += " and GLSTATE = 'GL' ";
    	}
		String tablename = "t_da_data_TONEWSYS";
		if (RuoYiConfig.isSeparateEnabled()){
			if (unitid!=null&&!unitid.isEmpty()){
				System.out.println(unitid);
				List<Map> maps = filesDataMapper.selectUnitList(unitid);
				tablename = "t_da_data_for_"+maps.get(0).get("QZH");
			}
		}
		//List<Map> sortMaps = filesDataMapper.selectUnitList(unitid);
		if (unitid == null || unitid.isEmpty()){
			List<Map> havedataUnitList = filesDataMapper.selectAllUnitHavedataList();
			for (Map unit : havedataUnitList) {
				String unitfilter = " and UNITID = '"+unit.get("UNITID")+"'";
				String unitDataFilter = dataFilter + unitfilter;
				System.out.println("当前全宗："+unit.get("QZH"));

				List<Map> dataMap = filesDataMapper.selectDataVO(tablename,unitDataFilter);

				for(Map m:dataMap){
					System.out.println(m.get("ERID"));
					System.out.println(RuoYiConfig.getCopyPath());
					boolean copySrcFlag = copyFile(m,"T_ER_SRCLIST_FOR_"+unit.get("QZH"),"T_ER_STOREITEM_FOR_"+unit.get("QZH"));
//					boolean copySrcFlag = copyFile(m,"T_ER_SRCLIST_TONEWSYS","T_ER_STOREITEM_TONEWSYS");
					//			boolean copySlyFlag = copyFile(m,"T_ER_SLYLIST");
					//boolean copyStdFlag = copyFile(m,"T_ER_STDLIST_FOR_P001","T_ER_STOREITEM_TONEWSYS"); 不迁移固化了 所以注释掉
					boolean copyStdFlag = true;
					if(copySrcFlag&&copyStdFlag){
						//update data表的iscopy字段
						filesDataMapper.updateCopy((String) m.get("ERID"),tablename);
					}
				}

			}
		}else {
			List<Map> dataMap = filesDataMapper.selectDataVO(tablename,dataFilter);
			List<Map> unit = filesDataMapper.selectUnitList(unitid);
			for(Map m:dataMap){
				System.out.println(m.get("ERID"));
				System.out.println(RuoYiConfig.getCopyPath());
				boolean copySrcFlag = copyFile(m,"T_ER_SRCLIST_FOR_"+unit.get(0).get("QZH"),"T_ER_STOREITEM_FOR_"+unit.get(0).get("QZH"));
//			boolean copySlyFlag = copyFile(m,"T_ER_SLYLIST");
				//boolean copyStdFlag = copyFile(m,"T_ER_STDLIST_FOR_P001","T_ER_STOREITEM_TONEWSYS"); 不迁移固化了 所以注释掉
				boolean copyStdFlag = true;
				if(copySrcFlag&&copyStdFlag){
					//update data表的iscopy字段
					filesDataMapper.updateCopy((String) m.get("ERID"),tablename);
				}
			}
		}


*//*    	if(sortMaps.size()>0){
    		tablename += "_TONEWSYS";
    	}*//*

		return RuoYiConfig.getProfile();
	}*/

	@Override
	public String packagefileall(String unitid, String sortid, String carriertype, String strutstype) {
		// 1. 拦截重复点击
		if (!globalIsRunning.compareAndSet(false, true)) {
			throw new ServiceException("当前已有导出任务正在后台运行，请勿重复点击！");
		}

		// 2. 初始化线程池（4个并发线程）
		exportThreadPool = Executors.newFixedThreadPool(4);

		String dataFilter = " 1 = 1";
		if (unitid!=null&&!unitid.isEmpty()){
			dataFilter += " and unitid = '"+unitid+"'";
		}
		if (sortid!=null&&!sortid.isEmpty()){
			dataFilter += " and sortid = '"+sortid+"'";
		}
		if (carriertype!=null&&!carriertype.isEmpty()){
			dataFilter += " and carriertype = '"+carriertype+"'";
		}
		if (strutstype!=null&&!strutstype.isEmpty()){
			dataFilter += " and strutstype = '"+strutstype+"'";
		}
		dataFilter += " and iscopy is null";

/*		if("ERRECORD".equalsIgnoreCase(carriertype)){
			dataFilter += " and GLSTATE = 'GL' ";
		}*/

		final String finalDataFilter = dataFilter; // 给多线程传参用

		// 3. 异步启动任务
		CompletableFuture.runAsync(() -> {
			try {
				if (unitid == null || unitid.isEmpty()){
					// 【分支A】: 没有指定全宗，查询所有有数据的全宗
					List<Map> havedataUnitList = filesDataMapper.selectAllUnitHavedataList();

					// 将每个全宗包装成一个独立的线程任务
					CompletableFuture<?>[] futures = havedataUnitList.stream().map(unit ->
							CompletableFuture.runAsync(() -> {
								String qzh = unit.get("QZH").toString();
								Thread.currentThread().setName("Thread-"+Thread.currentThread().getId() +"_"+ qzh); // 线程绑定全宗名
								String unitDataFilter = finalDataFilter + " and UNITID = '" + unit.get("UNITID") + "'";
								processSingleUnitTask(qzh, unitDataFilter);
							}, exportThreadPool)
					).toArray(CompletableFuture[]::new);

					// 等待所有全宗跑完
					CompletableFuture.allOf(futures).join();

				} else {
					// 【分支B】: 指定了单个全宗
					List<Map> unit = filesDataMapper.selectUnitList(unitid);
					if(unit != null && unit.size() > 0) {
						String qzh = unit.get(0).get("QZH").toString();
						Thread.currentThread().setName(Thread.currentThread().getName() +"_"+ qzh);
						processSingleUnitTask(qzh, finalDataFilter);
					}
				}
			} catch (Exception e) {
				log.error("全局多线程导出发生异常: ", e);
			} finally {
				// 4. 全部处理完成后，恢复全局状态并关闭线程池
				globalIsRunning.set(false);
				if (exportThreadPool != null && !exportThreadPool.isShutdown()) {
					exportThreadPool.shutdown();
				}
				log.info("====== 导全部任务结束（正常完成或被中断） ======");
			}
		});
		return RuoYiConfig.getProfile(); // 异步启动，立即返回
	}

	/**
	 * 单个全宗数据的处理逻辑（抽离出来复用）
	 */
	private void processSingleUnitTask(String qzh, String filter) {
		System.out.println("线程："+Thread.currentThread().getName()+">>>>>>>>全宗："+qzh);
		String tablename = "t_da_data_TONEWSYS";
		if (RuoYiConfig.isSeparateEnabled()) {
			tablename = "t_da_data_for_" + qzh;
		}

		List<Map> dataMap = filesDataMapper.selectDataVO(tablename, filter);

		for (Map m : dataMap) {
			// 【核心安全检查】：每次处理一个条目之前，检查是否被喊停
			if (Thread.currentThread().isInterrupted() || !globalIsRunning.get()) {
				log.warn("检测到停止信号，全宗[{}]的数据迁移已中断", qzh);
				break; // 跳出循环，不再处理该全宗剩余数据
			}

			boolean moveSrcFlag = moveFile(m, "T_ER_SRCLIST_FOR_" + qzh, "T_ER_STOREITEM_FOR_" + qzh,qzh);
			// boolean copyStdFlag = moveFile(m,"T_ER_STDLIST_FOR_P001","T_ER_STOREITEM_TONEWSYS"); 不迁移固化了
			boolean moveStdFlag = true;

			if (moveSrcFlag && moveStdFlag) {
				// update data表的iscopy字段
				filesDataMapper.updateCopy((String) m.get("ERID"), tablename);
			}
		}
	}

	/**
	 * 使用 NIO Move 移动文件（替换原有的 copyFile）
	 */
	private boolean moveFile(Map data, String tableName, String storeTableName,String qzh) {
		boolean success = true;
		List<Map> storeMap = filesDataMapper.getFileList((String) data.get("ERID"), tableName, storeTableName);

		for (Map m : storeMap) {
			if (Thread.currentThread().isInterrupted() || !globalIsRunning.get()) {
				return false;
			}

			String filePath = (String) m.get("LOCATION") + File.separatorChar + (String) m.get("URL");
			String fileName = FileUtils.getName(filePath);
			String rootPath = RuoYiConfig.getProfile() + File.separatorChar; //20260331 去掉tablename

			String copyUrl = qzh+data.get("UNITFULLNAME").toString().trim() + File.separatorChar +
					(String) data.get("SORTFULLNAME") + File.separatorChar +
					getDahaoPath(data) + File.separatorChar +
					DateUtils.datePath() + File.separatorChar +
					(String) data.get("ERID") + File.separatorChar +
					fileName;

			String outputPath = rootPath + /*File.separatorChar +*/ copyUrl;

			try {
				Path sourcePath = Paths.get(filePath);
				Path targetPath = Paths.get(outputPath);

				if (!Files.exists(targetPath.getParent())) {
					Files.createDirectories(targetPath.getParent());
				}

				File inputf = new File(filePath);
				if (inputf.exists()) {
					// 1. 执行文件移动
					try {
						// 注意：ATOMIC_MOVE 通常不需要伴随 COPY_ATTRIBUTES，加了在某些系统可能报错，建议去掉 COPY_ATTRIBUTES
						Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
					} catch (AtomicMoveNotSupportedException e) {
						log.info("路径跨磁盘分区，不支持原子移动，降级为普通移动：{}", sourcePath);
						Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
					}

					// 2. 核心改进：单独捕获数据库异常，实现文件回滚
					try {
						// 移动成功，准备更新数据库
						filesDataMapper.updateCopyUrl((String) m.get("ID"), copyUrl.replace("/", "\\"), storeTableName);
					} catch (Exception dbException) {
						log.error("文件移动成功，但数据库更新失败！准备将文件回滚至原路径: {}", sourcePath, dbException);

						// 【补偿机制】：把目标文件重新移回原路径，恢复案发现场
						try {
							Files.move(targetPath, sourcePath, StandardCopyOption.REPLACE_EXISTING);
							log.info("文件已成功回滚至原路径。");
						} catch (Exception rollbackEx) {
							// 极端情况：回滚也失败了，必须打印严重日志人工介入
							log.error("【严重告警】数据库更新失败，且文件回滚失败！文件遗留在: {}", targetPath, rollbackEx);
						}

						// 标记当前条目失败，触发外层业务逻辑处理
						throw new RuntimeException("数据库更新失败，已阻断该文件的迁移");
					}

				} else {
					log.warn("源文件不存在: {}", filePath);
					filesDataMapper.updateCopyUrl((String) m.get("ID"), "NOSOURCE", storeTableName);
					success = false; // 源文件本来就不存在
				}
			} catch (Exception e) {
				log.error("文件处理流程失败，原路径：{}", filePath, e);
				filesDataMapper.updateCopyUrl((String) m.get("ID"), "EXCEPTION", storeTableName);
				success = false;
			}
			System.out.println("源文件路径："+filePath);
			System.out.println("目标路径："+outputPath);
		}
		return success;
	}

	@Override
	public void stopGlobalExport() {
		if (globalIsRunning.compareAndSet(true, false)) {
			log.info("====== 接收到停止指令，正在中断底层导出线程 ======");
			if (exportThreadPool != null && !exportThreadPool.isShutdown()) {
				exportThreadPool.shutdownNow(); // 发送中断信号
			}
		}
	}



}
