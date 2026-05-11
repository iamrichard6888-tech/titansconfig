package com.ruoyi.project.tool.download;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.pagehelper.PageHelper;
import com.ruoyi.common.utils.PageUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.sql.SqlUtil;
import com.ruoyi.common.utils.uuid.IdUtils;
import com.ruoyi.common.utils.uuid.UUID;
import com.ruoyi.framework.web.page.PageDomain;
import com.ruoyi.framework.web.page.TableSupport;
import com.ruoyi.project.system.checklist.CheckListHandler.MergeSameColumnHandler;
import com.ruoyi.project.system.migrationprogress.domain.MigrationProgress;
import com.ruoyi.project.system.migrationprogress.service.IMigrationProgressService;

import com.ruoyi.project.system.seperatetable.service.ISeperateTableService;
import org.springframework.core.io.ClassPathResource;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.enums.WriteDirectionEnum;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.fill.FillConfig;
import com.ruoyi.framework.config.RuoYiConfig;
import com.ruoyi.project.system.checklist.domain.CheckList;
import com.ruoyi.project.system.checklist.service.ICheckListService;
import org.apache.ibatis.annotations.Param;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.framework.aspectj.lang.annotation.Log;
import com.ruoyi.framework.aspectj.lang.enums.BusinessType;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.framework.web.domain.Ztree;
import com.ruoyi.framework.web.domain.ZtreeString;
import com.ruoyi.framework.web.page.TableDataInfo;
import com.ruoyi.project.system.dept.domain.Dept;
import com.ruoyi.project.system.dept.service.IDeptService;
import com.ruoyi.project.system.file.domain.FilesData;
import com.ruoyi.project.system.file.service.IFilesDataService;
import com.ruoyi.project.system.sort.domain.Sort;
import com.ruoyi.project.system.sort.service.ISortService;
import com.ruoyi.project.system.user.domain.User;
import com.ruoyi.project.system.user.service.IUserService;

/**
 * build 表单构建
 * 
 * @author ruoyi
 */
@Controller
@RequestMapping("/tool/download")
public class DownloadController extends BaseController
{
    private String prefix = "tool/download";
    
    @Autowired
    private ISortService sortService;
    
    @Autowired
    private IUserService userService;
    
    @Autowired
    private IFilesDataService filesDataService;

    @Autowired
    private ICheckListService checkListService;

    @Autowired
    private IMigrationProgressService migrationProgressService;

    @Autowired
    private ISeperateTableService seperateTableService;

    @RequiresPermissions("tool:download:view")
    @GetMapping()
    public String download()
    {
        return prefix + "/download";
    }
    
    /**
     * 加载部门列表树
     */
    @RequiresPermissions("tool:download:view")
    @GetMapping("/sortTreeData")
    @ResponseBody
    public List<ZtreeString> sortTreeData()
    {
        List<ZtreeString> ztrees = sortService.selectSortTree(new Sort());
        return ztrees;
    }
    
    @RequiresPermissions("tool:download:view")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(FilesData filesData)
    {
        //startPage();
        List<FilesData> lists = filesDataService.selectFilesDataList(filesData);
        return getDataTable(lists);
    }
    
    
    @RequiresPermissions("tool:download:view")
    @PostMapping("/downloadfiles")
    @ResponseBody
    public AjaxResult downloadfiles(String ids,String deptId)
    {
        //List<FilesData> list = userService.selectUserList(user);
        
        //ExcelUtil<FilesData> util = new ExcelUtil<FilesData>(FilesData.class);
    	if(ids == null || "".equals(ids)){
    		return AjaxResult.error("档案条目记录获取失败!");
    	}
    	
/*    	if(deptId == null || "".equals(deptId)){
    		return AjaxResult.error("档案门类获取失败!");
    	}*/
    	
    	//String username = getLoginName();
    	//System.out.println("==========username=========="+username);
    	
        String filename = filesDataService.packagefile(getIDstr(ids),deptId);
        return AjaxResult.success(filename);
        //return AjaxResult.error("测试");
    }
    
    /**
     * 加载部门列表树
     */
    @RequiresPermissions("tool:download:view")
    @GetMapping("/updateTree")
    @ResponseBody
    public boolean updateTree()
    {
    	sortService.updateTree();
        return true;
    }
    
    public static String getIDstr(String ids) {
		if (ids.indexOf("'") == -1) {
			ids = ids.replace(",", "','");
			ids = "'" + ids + "'";
		}
		return ids;
	}
    
    /*@RequiresPermissions("tool:download:view")
    @PostMapping("/downloadfilesall")
    @ResponseBody
    public AjaxResult downloadfilesall(String unitid,String sortid,String carriertype,String strutstype)
    {
    	
        String filename = filesDataService.packagefileall(unitid,sortid,carriertype,strutstype);
        return AjaxResult.success(filename);
        //return AjaxResult.error("测试");
    }*/

    /**
     * 启动全局多线程导出任务
     */
    @RequiresPermissions("tool:download:view")
    @PostMapping("/startExportAll")
    @ResponseBody
    public AjaxResult startExportAll(String unitid, String sortid, String carriertype, String strutstype)
    {
        try {
            // 调用 Service 层的方法，该方法内部会启动异步线程池并立即返回
            filesDataService.packagefileall(unitid, sortid, carriertype, strutstype);

            // 直接给前端返回成功提示，不用返回 filename
            return AjaxResult.success("全局导出任务已启动，正在后台多线程执行！");
        } catch (Exception e) {
            // 如果 Service 层抛出了 "当前已有导出任务正在后台运行..." 等异常，会在这里捕获并提示给前端
            return AjaxResult.error(e.getMessage());
        }
    }

    @RequiresPermissions("tool:download:view")
    @PostMapping("/checkListData")
    @ResponseBody
    public TableDataInfo getCheckListData(CheckList checkList)
    {
        List<CheckList> checkListByUnitid = checkListService.getCheckListByUnitid(checkList.getUnitid());
        return getDataTable(checkListByUnitid);
    }

    @RequiresPermissions("tool:download:view")
    @GetMapping("/checklist")
    public String getCheckList(@Param("unitid") String unitid, ModelMap mmap){
        Map unit = checkListService.getUnitByUnitid(unitid).get(0);
        mmap.put("unitid", unitid);
        mmap.put("unitfullname",unit.get("UNITFULLNAME"));
        mmap.put("qzh",unit.get("QZH"));
        System.out.println(unit.get("UNITFULLNAME"));
        return prefix + "/checklist2";
    }

    @RequiresPermissions("tool:download:view")
    @PostMapping("/exportCheckList")
    @ResponseBody
    public AjaxResult exportCheckList(CheckList checkList){
        try {
            System.out.println(checkList.toString());
            // 1. 从数据库查询你需要导出的数据列表（跟前端预览调用的查询逻辑一样）
            List<CheckList> checkListByUnitid = checkListService.getCheckListByUnitid(checkList.getUnitid());
            Map unit = checkListService.getUnitByUnitid(checkList.getUnitid()).get(0);

            // 2. 生成导出文件的名称和服务器保存路径（遵循若依标准下载机制）
            String realName = unit.get("UNITFULLNAME") + "数字档案室系统非声像数据迁移确认表.xlsx";
            String fileName = UUID.randomUUID().toString() + "_" + realName;
            System.out.println(fileName);
            String filePath = RuoYiConfig.getDownloadPath() + fileName;

            if (!new File(RuoYiConfig.getDownloadPath()).exists()) {
                new File(RuoYiConfig.getDownloadPath()).mkdirs();
            }
/*            // 3. 读取 resources/excel 目录下的模板文件
            InputStream templateInputStream =
                    new ClassPathResource("excel/checkList_template.xlsx").getInputStream();*/

            InputStream templateInputStream = new ClassPathResource("excel/checkList_template.xlsx").getInputStream();

            // 准备非列表的表头变量
            Map<String, Object> map = new HashMap<>();
            map.put("unitfullname", unit.get("UNITFULLNAME"));
            map.put("qzh", unit.get("QZH"));

            // ================= 【核心修复区：把对象转成 Map】 =================
            List<Map<String, Object>> exportData = new ArrayList<>();
            int totalOld = 0;
            int totalFileValue = 0;
            for (CheckList item : checkListByUnitid) {
                Map<String, Object> row = new HashMap<>();

                // 这里的 Key（如 "oldSysFunction"）必须和模板里 {.oldSysFunction} 完全一致！
                // 这里的 Value（如 item.getOldSysFunction()）请替换为你实体类里真实的 get 方法！
                row.put("serial", item .getSerial());
                row.put("oldSysFunction", item.getOldSysFunction());
                row.put("oldSysSort", item.getOldSysSort());
                row.put("oldSysDataValues", Integer.parseInt(item.getOldSysDataValues()));
                row.put("oldSysFileValues", Integer.parseInt(item.getOldSysFileValues()));
                row.put("newSysFunction", item.getNewSysFunction());
                row.put("newSysSort", item.getNewSysSort());
                row.put("newSysDataValues", Integer.parseInt(item.getNewSysDataValues()));
                row.put("newSysFileValues", Integer.parseInt(item.getNewSysFileValues()));
                row.put("remark", "");
                totalOld += Integer.parseInt(item.getOldSysDataValues());
                totalFileValue += Integer.parseInt(item.getOldSysFileValues());
                exportData.add(row);
            }
            map.put("totalOld", totalOld);
            map.put("totalNew", totalOld);
            map.put("totalFileOld", totalFileValue);
            map.put("totalFileNew", totalFileValue);

            // 3. 构建 EasyExcel Writer 并注册合并拦截器（使用刚刚更新过防御机制的拦截器）
            ExcelWriter excelWriter = EasyExcel.write(filePath)
                    .withTemplate(templateInputStream)
                    .registerWriteHandler(new MergeSameColumnHandler(4, new int[]{1, 5}))
                    .build();

            WriteSheet writeSheet = EasyExcel.writerSheet().build();

            // 4. 先填充普通的表头变量
            excelWriter.fill(map, writeSheet);

            // 5. 重点：这里传入的是我们刚刚转换好的 exportData，而不是原来的 checkListByUnitid！
            FillConfig fillConfig = FillConfig.builder()
                    .direction(WriteDirectionEnum.VERTICAL)
                    .forceNewRow(true)
                    .build();
            excelWriter.fill(exportData, fillConfig, writeSheet);

            excelWriter.finish();

            return AjaxResult.success(fileName);
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error("导出Excel失败，请联系管理员");
        }
    }

    @RequiresPermissions("tool:download:view")
    @GetMapping("/tomigrationprogress")
    public String toMigrationProgressUrl (){
        return prefix + "/migrationprogress";
    }

    @RequiresPermissions("tool:download:view")
    @PostMapping("/getMigrationProgress")
    @ResponseBody
    public TableDataInfo getMigrationProgress (MigrationProgress migrationProgress){
        startOrderByOnly();
        List<MigrationProgress> totalMigrationProcess = migrationProgressService.getTotalMigrationProcess(migrationProgress);
        return getDataTable(totalMigrationProcess);
    }

    protected void startOrderByOnly()
    {
        PageDomain pageDomain = TableSupport.buildPageRequest();

        if (StringUtils.isNotEmpty(pageDomain.getOrderBy()))
        {
            // 只设置排序，不设置分页
            PageHelper.orderBy(SqlUtil.escapeOrderBySql(pageDomain.getOrderBy().replace("_","")));
        }
    }


    /**
     * 1. 打开拆表进度监控弹窗页面
     */
    @GetMapping("/seperateModal")
    public String seperateModal() {
        return "tool/download/seperateModal";
    }

    /**
     * 2. 启动分表任务
     */
    @PostMapping("/startSeperate")
    @ResponseBody
    public AjaxResult startSeperate() {
        String batchId = "BATCH-" + IdUtils.simpleUUID();
        seperateTableService.startDevideTables();
        return AjaxResult.success();
    }

    /**
     * 3. 停止(中断并回滚)分表任务
     */
    @PostMapping("/stopSeperate")
    @ResponseBody
    public AjaxResult stopSeperate() {
        // 加这一行！如果点停止没看到这句话，说明前端 JS 有问题
        System.out.println("====== 后端已成功接收到停止分表指令 ======");
        seperateTableService.stopDevideTables();
        return AjaxResult.success();
    }

    /**
     * 4. 获取任务进度状态 (供前端进度条轮询)
     */
    @GetMapping("/seperateStatus")
    @ResponseBody
    public AjaxResult seperateStatus() {
        Map<String, Object> status = seperateTableService.getProgressStatus();
        return AjaxResult.success(status);
    }

    /**
     * 5. 获取下方的进度明细表格数据
     */
    @PostMapping("/seperateList")
    @ResponseBody
    public TableDataInfo seperateList() {
        startPage(); // 开启分页
        // 直接从 Service 中获取 List<Map> 数据返回给 Bootstrap-table
        List<Map<String, Object>> list = seperateTableService.getLogList();
        return getDataTable(list);
    }

    /**
     * 获取全局导出状态（供前端定时器轮询）
     */
    @GetMapping("/exportStatus")
    @ResponseBody
    public AjaxResult getExportStatus() {
        boolean isRunning = filesDataService.getGlobalExportStatus();
        return AjaxResult.success(isRunning ? "running" : "stopped");
    }

    /**
     * 停止全局导出任务
     */
    @PostMapping("/stopExportAll")
    @ResponseBody
    public AjaxResult stopExportAll() {
        filesDataService.stopGlobalExport();
        return AjaxResult.success("停止信号已发送");
    }

    /**
     * 查看页面
     */
    @GetMapping("/template")
    public String totemplate() {
        return "template";
    }

    /**
     * 查看页面
     */
    @GetMapping("/addtemplate")
    public String addtemplate() {
        return "add";
    }
}
