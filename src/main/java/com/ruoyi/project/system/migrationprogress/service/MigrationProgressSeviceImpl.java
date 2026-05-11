package com.ruoyi.project.system.migrationprogress.service;

import com.ruoyi.project.system.file.mapper.FilesDataMapper;
import com.ruoyi.project.system.migrationprogress.domain.MigrationProgress;
import com.ruoyi.project.system.migrationprogress.mapper.MigrationProgressMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MigrationProgressSeviceImpl implements IMigrationProgressService{
    @Autowired
    private MigrationProgressMapper migrationProgressMapper;

    @Autowired
    private FilesDataMapper filesDataMapper;

    @Override
    public List<MigrationProgress> getTotalMigrationProcess(MigrationProgress migrationProgress) {
        String filter = "1=1";
        String qzcodeData = migrationProgress.getQzh();
        String selectUnitidByQzcodeFilter = " 1 = 1 ";
        if(qzcodeData!=null&&!qzcodeData.isEmpty()){
            if (qzcodeData.indexOf(",")>0){
                String[] qzcodeSplit = qzcodeData.split(",");
                int qzcodeLength = qzcodeSplit.length;
                if (qzcodeLength > 1){
                    selectUnitidByQzcodeFilter += " and ( ";
                    for (int i = 0; i < qzcodeLength; i++) {
                        String qzhfilter =  " qzh like '%"+qzcodeSplit[i].toUpperCase()+"%' ";
                        selectUnitidByQzcodeFilter += qzhfilter;
                        if (i != qzcodeLength - 1){
                            selectUnitidByQzcodeFilter += " or ";
                        }
                    }
                    selectUnitidByQzcodeFilter += " )";
                }else{
                    selectUnitidByQzcodeFilter += " and qzh like '%"+qzcodeSplit[0].toUpperCase()+"%' ";
                }
            } else if (qzcodeData.indexOf("，")>0) {

            }else {
                selectUnitidByQzcodeFilter += " and qzh like '%"+qzcodeData.toUpperCase()+"%' ";
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

        String unitfullnames = migrationProgress.getUnitfullname();
        if(unitfullnames!=null&&!unitfullnames.isEmpty()){
            if (unitfullnames.indexOf(",")>0){
                String[] unitfullnamesDataSplit = unitfullnames.split(",");
                int unitfullnamesDataLength = unitfullnamesDataSplit.length;
                if (unitfullnamesDataLength > 1){
                    filter += " and ( ";
                    for (int i = 0; i < unitfullnamesDataLength; i++) {
                        String unitfullnamesfilter =  " unitfullname like '%"+unitfullnamesDataSplit[i]+"%' ";
                        filter += unitfullnamesfilter;
                        if (i != unitfullnamesDataLength - 1){
                            filter += " or ";
                        }
                    }
                    filter += " )";
                }else{
                    filter += " and unitfullname like '%"+unitfullnamesDataSplit[0]+"%' ";
                }
            } else if (unitfullnames.indexOf("，")>0) {

            }else {
                filter += " and unitfullname like '%"+unitfullnames+"%' ";
            }
        }

        List<MigrationProgress> migrationProgressList = migrationProgressMapper.getMigrationProgressList(filter);
        return migrationProgressList;
    }
}
