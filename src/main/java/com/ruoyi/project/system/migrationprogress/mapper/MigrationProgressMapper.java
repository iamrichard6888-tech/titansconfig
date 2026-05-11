package com.ruoyi.project.system.migrationprogress.mapper;

import com.ruoyi.project.system.file.domain.FilesData;
import com.ruoyi.project.system.migrationprogress.domain.MigrationProgress;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;


public interface MigrationProgressMapper
{
	@Select("select * FROM MIRATIONPROGRESS where ${filter}")
	public List<MigrationProgress> getMigrationProgressList(@Param("filter")String filter);
}
