package com.ruoyi.project.system.migrationprogress.service;

import com.ruoyi.project.system.migrationprogress.domain.MigrationProgress;
import java.util.List;

public interface IMigrationProgressService {
    public List<MigrationProgress> getTotalMigrationProcess(MigrationProgress migrationProgress);
}
