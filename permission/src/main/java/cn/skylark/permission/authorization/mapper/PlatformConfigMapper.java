package cn.skylark.permission.authorization.mapper;

import cn.skylark.permission.authorization.entity.SysPlatformConfig;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PlatformConfigMapper {

  List<SysPlatformConfig> selectAll();

  SysPlatformConfig selectByKey(@Param("configKey") String configKey);

  int updateValueByKey(@Param("configKey") String configKey, @Param("configValue") String configValue);
}
