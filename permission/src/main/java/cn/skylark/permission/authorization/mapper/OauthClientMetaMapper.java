package cn.skylark.permission.authorization.mapper;

import cn.skylark.permission.authorization.entity.SysOauthClientMeta;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OauthClientMetaMapper {
  SysOauthClientMeta selectByClientId(@Param("clientId") String clientId);
}

