package cn.skylark.permission.authorization.service;

import cn.skylark.permission.authorization.dto.OauthClientPageRequest;
import cn.skylark.permission.authorization.dto.OauthClientResponseDTO;
import cn.skylark.permission.authorization.dto.UpdateOauthClientDTO;
import cn.skylark.permission.authorization.entity.OauthClientDetails;
import cn.skylark.permission.authorization.mapper.OauthClientMapper;
import cn.skylark.permission.utils.PageRequest;
import cn.skylark.permission.utils.PageResult;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * OAuth2客户端服务
 *
 * @author yaomianwei
 * @since 2025/11/3
 */
@Service
public class OauthClientService {

  @Resource
  private OauthClientMapper oauthClientMapper;

  public OauthClientDetails get(String clientId) {
    return oauthClientMapper.selectByClientId(clientId);
  }

  public List<OauthClientDetails> list() {
    return oauthClientMapper.selectAll();
  }

  /**
   * 获取客户端列表（DTO，不包含密钥）
   *
   * @return 客户端列表
   */
  public List<OauthClientResponseDTO> listDTO() {
    List<OauthClientDetails> clients = oauthClientMapper.selectAll();
    return clients.stream().map(this::convertToDTO).collect(Collectors.toList());
  }

  public int create(OauthClientDetails client) {
    return oauthClientMapper.insert(client);
  }

  public int update(OauthClientDetails client) {
    return oauthClientMapper.update(client);
  }

  public int delete(String clientId) {
    return oauthClientMapper.deleteByClientId(clientId);
  }

  /**
   * 获取客户端信息（DTO，不包含密钥）
   *
   * @param clientId 客户端ID
   * @return 客户端信息
   */
  public OauthClientResponseDTO getDTO(String clientId) {
    OauthClientDetails client = oauthClientMapper.selectByClientId(clientId);
    return client != null ? convertToDTO(client) : null;
  }

  /**
   * 分页查询客户端列表（DTO，不包含密钥）
   *
   * @param pageRequest 分页请求参数
   * @return 分页结果
   */
  public PageResult<OauthClientResponseDTO> pageDTO(PageRequest pageRequest) {
    List<OauthClientDetails> records = oauthClientMapper.selectPage(pageRequest.getOffset(), pageRequest.getLimit());
    Long total = oauthClientMapper.countAll();
    List<OauthClientResponseDTO> dtoList = records.stream().map(this::convertToDTO).collect(Collectors.toList());
    return new PageResult<>(dtoList, total, pageRequest.getPage(), pageRequest.getSize());
  }

  /**
   * 分页查询客户端列表（带条件，DTO，不包含密钥）
   *
   * @param pageRequest 分页请求参数（包含搜索条件）
   * @return 分页结果
   */
  public PageResult<OauthClientResponseDTO> pageDTOWithCondition(OauthClientPageRequest pageRequest) {
    // 判断是否有搜索条件
    boolean hasCondition = StringUtils.hasText(pageRequest.getClientId()) ||
                           StringUtils.hasText(pageRequest.getAuthorizedGrantTypes()) ||
                           StringUtils.hasText(pageRequest.getScope());

    List<OauthClientDetails> records;
    Long total;

    if (hasCondition) {
      // 使用带条件的查询
      records = oauthClientMapper.selectPageWithCondition(
          pageRequest.getClientId(),
          pageRequest.getAuthorizedGrantTypes(),
          pageRequest.getScope(),
          pageRequest.getOffset(),
          pageRequest.getLimit()
      );
      total = oauthClientMapper.countWithCondition(
          pageRequest.getClientId(),
          pageRequest.getAuthorizedGrantTypes(),
          pageRequest.getScope()
      );
    } else {
      // 使用无条件的查询
      records = oauthClientMapper.selectPage(pageRequest.getOffset(), pageRequest.getLimit());
      total = oauthClientMapper.countAll();
    }

    List<OauthClientResponseDTO> dtoList = records.stream().map(this::convertToDTO).collect(Collectors.toList());
    return new PageResult<>(dtoList, total, pageRequest.getPage(), pageRequest.getSize());
  }

  /**
   * 更新客户端信息
   *
   * @param clientId 客户端ID
   * @param dto      更新客户端信息DTO
   * @return 更新行数
   */
  public int updateClientInfo(String clientId, UpdateOauthClientDTO dto) {
    return oauthClientMapper.updateClientInfo(clientId, dto);
  }

  /**
   * 将OauthClientDetails转换为OauthClientResponseDTO（不包含密钥）
   *
   * @param client 客户端实体
   * @return 客户端响应DTO
   */
  private OauthClientResponseDTO convertToDTO(OauthClientDetails client) {
    if (client == null) {
      return null;
    }
    OauthClientResponseDTO dto = new OauthClientResponseDTO();
    BeanUtils.copyProperties(client, dto);
    return dto;
  }
}

