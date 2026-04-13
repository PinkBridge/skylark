package cn.skylark.permission.authorization.mapper;

import cn.skylark.permission.authorization.entity.SysOperationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OperationLogMapper {

  SysOperationLog selectById(@Param("id") Long id);

  int insert(SysOperationLog row);

  List<SysOperationLog> selectPage(@Param("offset") Integer offset, @Param("limit") Integer limit);

  List<SysOperationLog> selectPageWithCondition(
      @Param("username") String username,
      @Param("httpMethod") String httpMethod,
      @Param("httpStatus") Integer httpStatus,
      @Param("requestUri") String requestUri,
      @Param("createdTimeStart") LocalDateTime createdTimeStart,
      @Param("createdTimeEnd") LocalDateTime createdTimeEnd,
      @Param("tenantId") Long tenantId,
      @Param("offset") Integer offset,
      @Param("limit") Integer limit);

  Long countAll();

  Long countWithCondition(
      @Param("username") String username,
      @Param("httpMethod") String httpMethod,
      @Param("httpStatus") Integer httpStatus,
      @Param("requestUri") String requestUri,
      @Param("createdTimeStart") LocalDateTime createdTimeStart,
      @Param("createdTimeEnd") LocalDateTime createdTimeEnd,
      @Param("tenantId") Long tenantId);
}
