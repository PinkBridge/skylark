package cn.skylark.permission.authorization.mapper;

import cn.skylark.permission.authorization.entity.SysPlatformInitState;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PlatformInitStateMapper {

  SysPlatformInitState selectSingleton();

  int insertSingleton(@Param("status") String status);

  int updateStatus(@Param("status") String status, @Param("finishedAt") java.time.LocalDateTime finishedAt);
}

