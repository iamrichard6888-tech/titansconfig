package com.ruoyi.project.system.userconfig.service;

import com.ruoyi.framework.aspectj.lang.annotation.DataSource;
import com.ruoyi.framework.aspectj.lang.enums.DataSourceType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.uuid.IdUtils;
import com.ruoyi.project.system.userconfig.domain.UserImportTemp;
import com.ruoyi.project.system.userconfig.mapper.UserImportTempMapper;
import net.sourceforge.pinyin4j.PinyinHelper;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@DataSource(value = DataSourceType.SLAVE) // 强制使用从库(业务库)
public class UserConfigServiceImpl {

    @Autowired
    private UserImportTempMapper tempMapper;

    /** 解析 Word */
    /*public int importAndParseWord(MultipartFile[] files) throws Exception {
        int successCount = 0;
        for (MultipartFile file : files) {

            String fileName = file.getOriginalFilename();
            if (fileName == null || !fileName.endsWith(".docx")) continue;

            boolean isSanYuan = fileName.contains("三员");
            try (InputStream is = file.getInputStream(); XWPFDocument document = new XWPFDocument(is)) {

                String qzh = extractQzh(document.getParagraphs());
                // 【DM 视角】：不再直接抛出异常，而是设立一个标志位
                boolean isQzhMissing = StringUtils.isEmpty(qzh);

                List<XWPFTable> tables = document.getTables();
                if (tables.isEmpty()) continue;
                XWPFTable table = tables.get(0);
                List<XWPFTableRow> rows = table.getRows();

                for (int i = 1; i < rows.size(); i++) {
                    List<XWPFTableCell> cells = rows.get(i).getTableCells();
                    if (cells.size() < 6) continue;

                    UserImportTemp temp = new UserImportTemp();
                    temp.setId(IdUtils.simpleUUID());

                    String personType = getCellText(cells.get(0));
                    String originalName = getCellText(cells.get(1));
                    temp.setSource("Word解析");

                    temp.setUserName(personType + "_" + originalName);
                    temp.setDeptName(getCellText(cells.get(2)));
                    temp.setPostName(getCellText(cells.get(3)));
                    String mobile = getCellText(cells.get(5));
                    Map<String, Object> accountInfo = generateAccountWithCheck(isSanYuan, qzh, personType, originalName, mobile);
                    temp.setLoginAccount((String) accountInfo.get("account"));

                    if ((boolean) accountInfo.get("isAdjusted")) {
                        temp.setStatus("1"); // 设为异常/待核对状态
                        temp.setErrorMsg("账号存在重复，已根据规则自动关联手机尾号，请核对。");
                    } else if (StringUtils.isEmpty(mobile)) {
                        temp.setStatus("1");
                        temp.setErrorMsg("文档缺少手机号码");
                    }else {
                        temp.setStatus("0");
                    }
                    //temp.setLoginAccount(generateLoginAccount(isSanYuan, qzh, personType, originalName,mobile));
                    temp.setMobilePhone(mobile);
                    if (isSanYuan) {
                        temp.setUserType(personType);
                    }else {
                        temp.setUserType("普通用户");
                    }
                    temp.setPersonType(personType);

                    temp.setDescription(
                            tempMapper.selectOrganInfoByQzh(temp.getOrganQzh()).get("organfullname".toUpperCase())+"--"
                            +temp.getPersonType()+"--"
                            +originalName+"--"
                            +temp.getDeptName()+"--"
                            +temp.getPostName()+"--"
                            +temp.getMobilePhone());

                    tempMapper.insertUserImportTemp(temp);
                    successCount++;
                }
            }
        }
        return successCount;
    }*/

    /** 解析 Word (融合了自定义Description与全宗容错) */
    public int importAndParseWord(MultipartFile[] files) throws Exception {
        int successCount = 0;
        for (MultipartFile file : files) {

            String fileName = file.getOriginalFilename();
            if (fileName == null || !fileName.endsWith(".docx")) continue;


            try (InputStream is = file.getInputStream(); XWPFDocument document = new XWPFDocument(is)) {

                // 1. 先通过文件名判断，如果已经包含，直接为 true
                boolean isSanYuan = fileName.contains("三员");

                // 2. 【DM 视角优化】：只有当文件名没写"三员"时，我们才去文档内容里“拾遗补漏”
                if (!isSanYuan) {
                    List<XWPFParagraph> paragraphs = document.getParagraphs();
                    // 【核心防御】：取 段落总数 和 4 之间的最小值，绝对防止数组越界！
                    int checkLimit = Math.min(paragraphs.size(), 4);

                    for (int i = 0; i < checkLimit; i++) {
                        if (paragraphs.get(i).getText().contains("三员")) {
                            isSanYuan = true;
                            break; // 只要找到一个，立刻打断循环，提升性能
                        }
                    }
                }

                String qzh = extractQzh(document.getParagraphs());
                // 【DM 视角】：不再直接抛出异常，而是设立一个标志位
                boolean isQzhMissing = StringUtils.isEmpty(qzh);

                List<XWPFTable> tables = document.getTables();
                if (tables.isEmpty()) continue;
                XWPFTable table = tables.get(0);
                List<XWPFTableRow> rows = table.getRows();

                for (int i = 1; i < rows.size(); i++) {
                    List<XWPFTableCell> cells = rows.get(i).getTableCells();
                    if (cells.size() < 6) continue;

                    UserImportTemp temp = new UserImportTemp();
                    temp.setId(IdUtils.simpleUUID());

                    String personType = getCellText(cells.get(0));
                    String originalName = getCellText(cells.get(1));
                    String mobile = getCellText(cells.get(5));

                    temp.setSource("Word解析");

                    temp.setDeptName(getCellText(cells.get(2)));
                    temp.setPostName(getCellText(cells.get(3)));
                    temp.setMobilePhone(mobile);
                    temp.setPersonType(personType);

                    // 【保留你的修改】：用户类型逻辑
                    if (isSanYuan) {
                        temp.setUserType(personType);
                        temp.setUserName(personType + "_" + originalName);
                    } else {
                        temp.setUserType("普通用户");
                        temp.setUserName(originalName);
                    }

                    // ================= 核心容错分流逻辑 =================
                    if (isQzhMissing) {
                        // 1. 降级处理：没填全宗号的坏数据
                        temp.setOrganQzh("");
                        temp.setLoginAccount((String) generateAccountWithCheck(isSanYuan, qzh, personType, originalName, mobile,isQzhMissing).get("account"));
                        temp.setStatus("1");
                        temp.setErrorMsg("文档未规范填写全宗号，请点击[编辑]手动选择单位并补全账号。");

                        // 【安全拼接描述】：因为没全宗号查不到单位，做兜底拼接
                        temp.setDescription("未知单位--"
                                + temp.getPersonType() + "--"
                                + originalName + "--"
                                + temp.getDeptName() + "--"
                                + temp.getPostName() + "--"
                                + temp.getMobilePhone());
                    } else {
                        // 2. 正常处理：有全宗号的好数据
                        temp.setOrganQzh(qzh); // 修复了你原代码漏掉的赋值！
                        temp.setOrganId(tempMapper.selectOrganIdByQzh(qzh));
                        // 账号查重逻辑
                        Map<String, Object> accountInfo = generateAccountWithCheck(isSanYuan, qzh, personType, originalName, mobile,isQzhMissing);
                        temp.setLoginAccount((String) accountInfo.get("account"));

                        if ((boolean) accountInfo.get("isAdjusted")) {
                            temp.setStatus("1"); // 设为异常/待核对状态
                            temp.setErrorMsg("账号存在重复，已根据规则自动关联手机尾号，请核对。");
                        } else if (StringUtils.isEmpty(mobile)) {
                            temp.setStatus("1");
                            temp.setErrorMsg("文档缺少手机号码");
                        } else {
                            temp.setStatus("0");
                        }

                        // 【保留你的修改 & 增加防御编程】：查询并安全拼接描述
                        try {
                            Map<String, String> organInfo = tempMapper.selectOrganInfoByQzh(qzh);
                            // 防御：万一数据库里查不到这个全宗号对应的全称，拿全宗号 qzh 兜底，绝不让程序崩溃
                            String organFullName = (organInfo != null && organInfo.get("ORGANFULLNAME") != null)
                                    ? organInfo.get("ORGANFULLNAME").toString() : qzh;

                            temp.setDescription(organFullName + "--"
                                    + temp.getPersonType() + "--"
                                    + originalName + "--"
                                    + temp.getDeptName() + "--"
                                    + temp.getPostName() + "--"
                                    + temp.getMobilePhone());
                        } catch (Exception e) {
                            // 极端异常兜底
                            temp.setDescription(qzh + "--" + temp.getPersonType() + "--" + originalName);
                        }
                    }
                    // ================= 分流逻辑结束 =================

                    tempMapper.insertUserImportTemp(temp);
                    successCount++;
                }
            } catch (Exception e) {
                // 将外层打印堆栈，防止单个文件损坏导致整个循环中断
                e.printStackTrace();
            }
        }
        return successCount;
    }

    /**
     * 将待确认的数据写入正式表 (具备幂等性检验与动态角色克隆)
     */
    /*public String saveToFormal() {
        List<UserImportTemp> pendingList = tempMapper.selectPendingList();
        if (pendingList.isEmpty()) {
            return "没有找到需要写入的数据（请确保数据处于'待确认'状态）";
        }

        int successCount = 0;
        int failCount = 0;

        for (UserImportTemp temp : pendingList) {
            try {
                // 1. 验证单位和账号唯一性
                String organId = tempMapper.selectOrganIdByQzh(temp.getOrganQzh().trim());
                if (StringUtils.isEmpty(organId)) throw new Exception("系统中找不到全宗号为 [" + temp.getOrganQzh() + "] 的单位");
                if (tempMapper.countUserByCode(temp.getLoginAccount()) > 0) throw new Exception("账号 [" + temp.getLoginAccount() + "] 已在正式库存在，请修改");

                // 2. 插入正式人员表 T_S_USER 和 T_S_UO
                String newUserId = IdUtils.simpleUUID();
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("userId", newUserId);
                userMap.put("code", temp.getLoginAccount());
                userMap.put("name", temp.getUserName());
                userMap.put("organId", organId);
                userMap.put("pwd", "15de21c670ae7c3f6f3f1f37029303c9");// MD5加密的555
                userMap.put("phone",temp.getMobilePhone());
                userMap.put("usertype",temp.getUserType());
                userMap.put("description",temp.getDescription());

                tempMapper.insertTsUser(userMap);
                tempMapper.insertTsUo(userMap);

                // 3. 【核心角色流转逻辑】：幂等性检查与模板克隆
                String roleCode = extractRoleCode(temp.getPersonType());
                if (StringUtils.isNotEmpty(roleCode)) {

                    // a. 查重：这个单位以前有没有生成过这个角色？
                    String roleId = tempMapper.checkRoleExists(roleCode, organId);

                    if (StringUtils.isEmpty(roleId)) {
                        // b. 没有专属角色，去拿全局模板
                        Map<String, Object> templateRole = tempMapper.selectTemplateRole(roleCode);
                        if (templateRole == null) {
                            throw new Exception("系统缺少 [" + roleCode + "] 的全局模板角色，无法为其克隆权限！");
                        }

                        // c. 建立这个单位的新专属角色
                        roleId = IdUtils.simpleUUID();
                        Map<String, Object> newRole = new HashMap<>();
                        newRole.put("roleId", roleId);
                        newRole.put("code", roleCode);
                        newRole.put("name", templateRole.get("NAME"));               // 达梦大写取值
                        newRole.put("description", templateRole.get("DESCRIPTION")); // 达梦大写取值
                        newRole.put("unitId", organId);

                        tempMapper.insertTsRole(newRole);

                        // d. 连带克隆该角色的菜单权限
                        String templateRoleId = (String) templateRole.get("ROLEID");
                        tempMapper.cloneRolePrivileges(roleId, templateRoleId);

                        // e. 历史包袱补偿：如果是安全管理员，强行打上两个特权补丁
                        if ("AQGLY".equalsIgnoreCase(roleCode)) {
                            tempMapper.insertTsRp(roleId, "C9612BA9318BFF034B6AF6C452A9FDF2111");
                            tempMapper.insertTsRp(roleId, "1539AA684A59E1EF4FB7F8EA0B6E655577");
                        }
                    }

                    // f. 走到这里，一定拿到了安全且唯一的 roleId，绑定用户与角色
                    Map<String, Object> urMap = new HashMap<>();
                    urMap.put("userId", newUserId);
                    urMap.put("roleId", roleId);
                    tempMapper.insertTsUr(urMap);
                }

                // 4. 成功入库，改状态
                tempMapper.updateTempStatus(temp.getId(), "2", "");
                successCount++;

            } catch (Exception e) {
                // 单条失败，记录原因，不影响下一条继续执行
                tempMapper.updateTempStatus(temp.getId(), "1", e.getMessage());
                failCount++;
            }
        }

        return "操作完成！成功写入: " + successCount + " 条，失败: " + failCount + " 条。";
    }*/

    public String saveToFormal(String ids) throws Exception {
        List<UserImportTemp> targetList;

        if (StringUtils.isNotEmpty(ids)) {
            // 如果传入了 ID（单条或勾选），只处理这些特定的 ID
            String[] idArray = com.ruoyi.common.utils.text.Convert.toStrArray(ids);
            targetList = new ArrayList<>();
            for (String id : idArray) {
                UserImportTemp t = tempMapper.selectUserImportTempById(id);
                // 只有状态为 0 (待确认) 的才允许执行入库
                if (t != null && "0".equals(t.getStatus())) {
                    targetList.add(t);
                }
            }
        } else {
            // 如果没传 ID，保留原有逻辑：查询所有待确认条目
            targetList = tempMapper.selectPendingList();
        }

        if (targetList.isEmpty()) {
            return "没有可执行入库的数据（请确保条目状态为“待确认”）";
        }

        int successCount = 0;
        int failCount = 0;

        for (UserImportTemp temp : targetList) {
            try {
                // 1. 验证单位和账号唯一性 (这里如果报错，直接跳到最外层 catch，算作彻底失败)
                String organId = tempMapper.selectOrganIdByQzh(temp.getOrganQzh().trim());
                if (StringUtils.isEmpty(organId)) throw new Exception("系统中找不到全宗号为 [" + temp.getOrganQzh() + "] 的单位");
                if (tempMapper.countUserByCode(temp.getLoginAccount()) > 0) throw new Exception("账号 [" + temp.getLoginAccount() + "] 已在正式库存在，请勿重复导入");

                // 2. 插入正式人员表 T_S_USER 和 T_S_UO
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("userId", temp.getId());
                userMap.put("code", temp.getLoginAccount());
                userMap.put("name", temp.getUserName());
                userMap.put("organId", organId);
                userMap.put("pwd", "15de21c670ae7c3f6f3f1f37029303c9");
                userMap.put("phone", temp.getMobilePhone());
                userMap.put("usertype", temp.getUserType());
                userMap.put("description", temp.getDescription());
                userMap.put("department", temp.getDeptName());
                System.out.println("用户："+temp.toString());

                tempMapper.insertTsUser(userMap);
                tempMapper.insertTsUo(userMap);

                // 3. 【核心改造：角色赋权独立容错】
                String roleCode = extractRoleCode(temp.getPersonType());
                String roleWarningMsg = ""; // 用于记录角色分配的警告信息

                if (StringUtils.isNotEmpty(roleCode)) {
                    // ★ 业务精髓：给角色逻辑单独上个保险！哪怕角色搞砸了，也不影响账号已经建好的事实
                    try {
                        String roleId = tempMapper.checkRoleExists(roleCode, organId);

                        if (StringUtils.isEmpty(roleId)) {
                            Map<String, Object> templateRole = tempMapper.selectTemplateRole(roleCode);
                            if (templateRole == null) {
                                // 抛出异常，但只会被内部的 catch 抓住！
                                throw new Exception("缺少 [" + roleCode + "] 全局模板");
                            }

                            roleId = IdUtils.simpleUUID();
                            Map<String, Object> newRole = new HashMap<>();
                            newRole.put("roleId", roleId);
                            newRole.put("code", roleCode);
                            newRole.put("name", templateRole.get("NAME"));
                            newRole.put("description", templateRole.get("DESCRIPTION"));
                            newRole.put("unitId", organId);

                            tempMapper.insertTsRole(newRole);

                            String templateRoleId = (String) templateRole.get("ROLEID");
                            tempMapper.cloneRolePrivileges(roleId, templateRoleId);

                            if ("AQGLY".equalsIgnoreCase(roleCode)) {
                                tempMapper.insertTsRp(roleId, "C9612BA9318BFF034B6AF6C452A9FDF2111");
                                tempMapper.insertTsRp(roleId, "1539AA684A59E1EF4FB7F8EA0B6E655577");
                            }
                        }

                        Map<String, Object> urMap = new HashMap<>();
                        urMap.put("userId", temp.getId());
                        urMap.put("roleId", roleId);
                        tempMapper.insertTsUr(urMap);

                    } catch (Exception roleEx) {
                        // 内部 catch：捕获到了角色分配失败
                        // 我们把失败原因存起来，不中断外层逻辑
                        roleWarningMsg = " (注：账号已成功入库，但因" + roleEx.getMessage() + "，未自动分配角色，请去正式系统手动处理)";
                    }
                }

                // 4. 【完美收官】：不管角色成功还是降级，只要账号插进去了，状态就标为 2 (已入库)
                // 如果 roleWarningMsg 有值，前端鼠标悬浮时就会看到这句友好的提示
                tempMapper.updateTempStatus(temp.getId(), "2", roleWarningMsg);
                successCount++;

            } catch (Exception e) {
                // 最外层 catch：只有账号因为重名、没单位等致命原因插不进去，才会走到这里
                tempMapper.updateTempStatus(temp.getId(), "1", e.getMessage());
                failCount++;
            }
        }

        return "操作完成！成功处理: " + successCount + " 条，失败: " + failCount + " 条。";
    }





    /**
     * 辅助方法：生成登录账号 (完美匹配 3.4 需求)
     */
    /*private String generateLoginAccount(boolean isSanYuan, String qzh, String personType, String name, String mobilePhone) {
        if (isSanYuan) {
            // 三员账号不需要查重，直接拼接：全宗号_角色CODE
            String sanyuanAcount = qzh + "_" + (StringUtils.isEmpty(extractRoleCode(personType)) ? "QT" : extractRoleCode(personType));

            int formalCount = tempMapper.countUserByCode(sanyuanAcount);
            int tempCount = tempMapper.countTempByAccount(sanyuanAcount);

            if (formalCount > 0 || tempCount > 0) {
                // 尝试截取手机号后 4 位
                if (StringUtils.isNotEmpty(mobilePhone) && mobilePhone.length() >= 4) {
                    return sanyuanAcount + mobilePhone.substring(mobilePhone.length() - 4);
                } else {
                    // 极端容错：重名了，而且他还没填手机号，怎么办？为了保证系统不崩溃，拿时间戳的后4位顶替
                    return sanyuanAcount + (System.currentTimeMillis() % 10000);
                }
            }
            return qzh + "_" + (StringUtils.isEmpty(extractRoleCode(personType)) ? "QT" : extractRoleCode(personType));
        } else {
            // 普通账号：全宗号-姓名拼音全小写
            String baseAccount = qzh + "-" + getPinyin(name);

            // 【核心防重逻辑】：分别去正式表和暂存表查一下，这个账号有没有被人占了？
            int formalCount = tempMapper.countUserByCode(baseAccount);
            int tempCount = tempMapper.countTempByAccount(baseAccount);

            // 如果已经被占用了（重名）
            if (formalCount > 0 || tempCount > 0) {
                // 尝试截取手机号后 4 位
                if (StringUtils.isNotEmpty(mobilePhone) && mobilePhone.length() >= 4) {
                    return baseAccount + mobilePhone.substring(mobilePhone.length() - 4);
                } else {
                    // 极端容错：重名了，而且他还没填手机号，怎么办？为了保证系统不崩溃，拿时间戳的后4位顶替
                    return baseAccount + (System.currentTimeMillis() % 10000);
                }
            }
            // 如果没人用，直接返回原账号
            return baseAccount;
        }
    }*/

    /**
     * 增强版账号生成：支持三员查重，并返回是否发生了调整
     * 返回 Map 格式：{ "account": "最终账号", "isAdjusted": true/false }
     */
    private Map<String, Object> generateAccountWithCheck(boolean isSanYuan, String qzh, String personType, String name, String mobilePhone,boolean isQzhMissing) {
        Map<String, Object> result = new HashMap<>();
        String baseAccount;

        if (isSanYuan) {
            baseAccount = qzh + "_" + (StringUtils.isEmpty(extractRoleCode(personType)) ? "QT" : extractRoleCode(personType));
        } else {
            baseAccount = qzh + "-" + getPinyin(name);
        }

        if (isQzhMissing){
            if (isSanYuan) {
                baseAccount = "_" + (StringUtils.isEmpty(extractRoleCode(personType)) ? "QT" : extractRoleCode(personType));
            } else {
                baseAccount = "-" + getPinyin(name);
            }
            result.put("account", baseAccount);
            result.put("isAdjusted", false);
            return result;
        }

        // 全面查重：查正式表 + 查暂存表
        int formalCount = tempMapper.countUserByCode(baseAccount);
        int tempCount = tempMapper.countTempByAccount(baseAccount);

        if (formalCount > 0 || tempCount > 0) {
            // 发生冲突：执行手机号补全逻辑
            String finalAccount = baseAccount;
            if (StringUtils.isNotEmpty(mobilePhone) && mobilePhone.length() >= 4) {
                finalAccount = baseAccount + mobilePhone.substring(mobilePhone.length() - 4);
            } else {
                finalAccount = baseAccount + (System.currentTimeMillis() % 10000);
            }
            result.put("account", finalAccount);
            result.put("isAdjusted", true); // 标记：这个账号是自动修改过的
        } else {
            result.put("account", baseAccount);
            result.put("isAdjusted", false);
        }
        return result;
    }

    private String getPinyin(String src) {
        StringBuilder sb = new StringBuilder();
        for (char c : src.toCharArray()) {
            String[] py = PinyinHelper.toHanyuPinyinStringArray(c);
            sb.append(py != null ? py[0].replaceAll("\\d", "") : c);
        }
        return sb.toString().toLowerCase();
    }

    /**
     * 辅助方法：提取段落中的全宗号 (增强版正则容错)
     */
    private String extractQzh(List<XWPFParagraph> paragraphs) {
        // 正则升级：支持中文冒号、英文冒号，或者没有冒号直接跟空格的写法
        Pattern pattern = Pattern.compile("全宗号[:：\\s]*([A-Za-z0-9]+)");
        for (XWPFParagraph paragraph : paragraphs) {
            String text = paragraph.getText().replace(" ", ""); // 忽略空格
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) return matcher.group(1).toUpperCase();
        }
        return null;
    }

    /**
     * 辅助方法：推断应该赋予的角色Code (含新增单位领导角色映射)
     */
    private String extractRoleCode(String personType) {
        if (personType == null) return "";
        if (personType.contains("系统管理员")) return "XTGLY";
        if (personType.contains("安全管理员")) return "AQGLY";
        if (personType.contains("审计员") || personType.contains("安全审计员")) return "AQSJY";
        if (personType.contains("档案员") || personType.contains("经办人")) return "LDDWDDAY";

        // 【新增】：联系人名单中的领导审核角色
        if (personType.contains("单位审核人") || personType.contains("部门审核人")) return "DWLD";

        return "";
    }

    private String getCellText(XWPFTableCell cell) { return cell == null ? "" : cell.getText().trim(); }

    public List<UserImportTemp> selectTempList(UserImportTemp temp) {
        List<UserImportTemp> userImportTemps = tempMapper.selectTempList(temp);
        return userImportTemps;
    }

    /** 查询单条数据用于修改页面回显 */
    public UserImportTemp selectUserImportTempById(String id) {
        return tempMapper.selectUserImportTempById(id);
    }

    /** 手动录入新增用户 */
    public int insertUserImportTemp(UserImportTemp temp) {
        temp.setId(IdUtils.simpleUUID());
        temp.setSource("手动录入");
        temp.setStatus("0"); // 状态设为待确认
        return tempMapper.insertUserImportTemp(temp);
    }

    /** 修改用户数据 */
    public int updateUserImportTemp(UserImportTemp temp) {
        UserImportTemp old = tempMapper.selectUserImportTempById(temp.getId());
        // 【智能容错逻辑】：如果一开始因为没手机号报错(状态为1)，现在实施人员在页面上把手机号补全了，系统自动把状态改回待确认(0)
        if (StringUtils.isNotEmpty(old.getErrorMsg()) && old.getErrorMsg().equals("文档缺少手机号码") && StringUtils.isNotEmpty(temp.getMobilePhone())) {

            if ("1".equals(old.getStatus())) {
                temp.setStatus("0");
                temp.setErrorMsg("");// 清空报错标记
                temp.setDescription(old.getDescription()+temp.getMobilePhone());
            }
        }
        if (StringUtils.isNotEmpty(old.getErrorMsg()) && old.getErrorMsg().equals("文档未规范填写全宗号，请点击[编辑]手动选择单位并补全账号。") && StringUtils.isNotEmpty(temp.getOrganQzh())) {
            Map<String, String> organInfo = tempMapper.selectOrganInfoByQzh(temp.getOrganQzh());
            // 防御：万一数据库里查不到这个全宗号对应的全称，拿全宗号 qzh 兜底，绝不让程序崩溃
            String organFullName = (organInfo != null && organInfo.get("ORGANFULLNAME") != null)
                    ? organInfo.get("ORGANFULLNAME").toString() : temp.getOrganQzh();

            if ("1".equals(old.getStatus())) {
                temp.setStatus("0");
                temp.setErrorMsg("");// 清空报错标记
                temp.setDescription(old.getDescription().replace("未知单位",organFullName));
                temp.setLoginAccount(temp.getOrganQzh().trim()+old.getLoginAccount());
            }
        }

        return tempMapper.updateUserImportTemp(temp);
    }

    /** 批量删除 */
    /** * 批量删除：增加状态校验，禁止删除已入库数据
     */
    public int deleteUserImportTempByIds(String ids) throws Exception {
        String[] idArray = com.ruoyi.common.utils.text.Convert.toStrArray(ids);

        for (String id : idArray) {
            UserImportTemp temp = tempMapper.selectUserImportTempById(id);
            // 【核心防御】：如果状态已经是 2 (已入库)，直接抛出异常，中断删除
            if (temp != null && "2".equals(temp.getStatus())) {
                throw new Exception("账号 [" + temp.getLoginAccount() + "] 已正式入库，为了审计安全，不允许删除中间表记录！");
            }
        }
        return tempMapper.deleteUserImportTempByIds(idArray);
    }

    /** 获取全宗单位下拉列表 */
    public List<Map<String, String>> selectOrganList() {
        return tempMapper.selectOrganList();
    }

    /**
     * 批量审核：把选中的 ID 状态全部改为 0
     */
    public int auditTemp(String ids) {
        String[] idArray = com.ruoyi.common.utils.text.Convert.toStrArray(ids);
        int count = 0;
        for (String id : idArray) {
            // 只有当前状态是 1 (需核对) 的才允许通过审核
            UserImportTemp temp = tempMapper.selectUserImportTempById(id);
            if (temp != null && "1".equals(temp.getStatus())) {
                // 将状态置为 0 (待确认)，并清空错误消息
                count += tempMapper.updateTempStatus(id, "0", "");
            }
        }
        return count;
    }

    public String checkLoginAccountExist(UserImportTemp temp) {
        // 查正式表
        int formalCount = tempMapper.countUserByCode(temp.getLoginAccount());
        // 查暂存表（排除掉自己，防止编辑时报自己的重名）
        UserImportTemp existTemp = tempMapper.selectTempByAccount(temp.getLoginAccount());

        if (formalCount > 0 || (existTemp != null && !existTemp.getId().equals(temp.getId()))) {
            return "1"; // 代表不唯一
        }
        return "0"; // 代表唯一
    }


}