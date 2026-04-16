package cn.skylark.permission.authorization.service;

import cn.skylark.permission.authorization.entity.SysOauthClientMeta;
import cn.skylark.permission.authorization.mapper.OauthClientMetaMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

@Service
public class OauthClientMetaService {

  @Resource
  private OauthClientMetaMapper oauthClientMetaMapper;

  public Integer portByClientId(String clientId) {
    if (!StringUtils.hasText(clientId)) {
      return null;
    }
    SysOauthClientMeta row = oauthClientMetaMapper.selectByClientId(clientId.trim());
    return row == null ? null : row.getPort();
  }
}

