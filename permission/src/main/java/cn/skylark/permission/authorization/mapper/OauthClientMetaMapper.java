package cn.skylark.permission.authorization.mapper;

import cn.skylark.permission.authorization.entity.SysOauthClientMeta;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OauthClientMetaMapper {
  SysOauthClientMeta selectByClientId(@Param("clientId") String clientId);

  List<SysOauthClientMeta> selectAll();

  List<SysOauthClientMeta> selectVisiblePcWeb();
}

