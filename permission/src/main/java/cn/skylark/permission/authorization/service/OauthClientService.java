package cn.skylark.permission.authorization.service;

import cn.skylark.permission.authorization.dto.OauthClientPageRequest;
import cn.skylark.permission.authorization.dto.OauthClientResponseDTO;
import cn.skylark.permission.authorization.dto.UpdateOauthClientDTO;
import cn.skylark.permission.authorization.entity.OauthClientDetails;
import cn.skylark.permission.authorization.entity.SysOauthClientMeta;
import cn.skylark.permission.authorization.mapper.OauthClientMapper;
import cn.skylark.permission.authorization.mapper.OauthClientMetaMapper;
import cn.skylark.permission.utils.PageRequest;
import cn.skylark.permission.utils.PageResult;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
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

  @Resource
  private OauthClientMetaMapper oauthClientMetaMapper;

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
    List<OauthClientResponseDTO> out = clients.stream().map(this::convertToDTO).collect(Collectors.toList());
    attachClientMeta(out);
    return out;
  }

  /**
   * /apps page bootstrap list:
   * only show sys_oauth_client_meta where is_show=true and terminal_type=PC-web, ordered by sort.
   */
  public List<OauthClientResponseDTO> listVisiblePcWebDTO() {
    List<SysOauthClientMeta> metas = oauthClientMetaMapper.selectVisiblePcWeb();
    if (metas == null || metas.isEmpty()) {
      return List.of();
    }
    Set<String> allowed = metas.stream()
        .filter(m -> m != null && StringUtils.hasText(m.getClientId()))
        .map(m -> m.getClientId().trim())
        .collect(Collectors.toSet());
    List<OauthClientDetails> clients = oauthClientMapper.selectAll();
    List<OauthClientResponseDTO> out = clients.stream()
        .filter(c -> c != null && StringUtils.hasText(c.getClientId()) && allowed.contains(c.getClientId().trim()))
        .map(this::convertToDTO)
        .collect(Collectors.toList());
    // Attach meta (name/port) in sort order.
    attachClientMeta(out, metas);
    // Sort output by meta.sort then clientId
    Map<String, Integer> sortById = metas.stream()
        .filter(m -> m != null && StringUtils.hasText(m.getClientId()))
        .collect(Collectors.toMap(m -> m.getClientId().trim(), m -> m.getSort() == null ? 0 : m.getSort(), (a, b) -> a));
    out.sort((a, b) -> {
      String aid = a == null ? null : a.getClientId();
      String bid = b == null ? null : b.getClientId();
      int as = aid != null && sortById.containsKey(aid) ? sortById.get(aid) : 0;
      int bs = bid != null && sortById.containsKey(bid) ? sortById.get(bid) : 0;
      int c = Integer.compare(as, bs);
      if (c != 0) return c;
      if (aid == null && bid == null) return 0;
      if (aid == null) return 1;
      if (bid == null) return -1;
      return aid.compareTo(bid);
    });
    return out;
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
    if (client == null) {
      return null;
    }
    OauthClientResponseDTO dto = convertToDTO(client);
    SysOauthClientMeta meta = oauthClientMetaMapper.selectByClientId(clientId);
    if (meta != null) {
      dto.setName(meta.getName());
      dto.setPort(meta.getPort());
    }
    return dto;
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
    attachClientMeta(dtoList);
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
    attachClientMeta(dtoList);
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

  private void attachClientMeta(List<OauthClientResponseDTO> dtos) {
    if (dtos == null || dtos.isEmpty()) {
      return;
    }
    List<SysOauthClientMeta> metas = oauthClientMetaMapper.selectAll();
    attachClientMeta(dtos, metas);
  }

  private void attachClientMeta(List<OauthClientResponseDTO> dtos, List<SysOauthClientMeta> metas) {
    if (metas == null || metas.isEmpty()) {
      return;
    }
    Map<String, SysOauthClientMeta> metaById = metas.stream()
        .filter(m -> m != null && StringUtils.hasText(m.getClientId()))
        .collect(Collectors.toMap(SysOauthClientMeta::getClientId, Function.identity(), (a, b) -> a));
    for (OauthClientResponseDTO dto : dtos) {
      if (dto == null || !StringUtils.hasText(dto.getClientId())) {
        continue;
      }
      SysOauthClientMeta meta = metaById.get(dto.getClientId());
      if (meta != null) {
        dto.setName(meta.getName());
        dto.setLogo(meta.getLogo());
        dto.setPort(meta.getPort());
        dto.setOpen(meta.getOpen());
      }
    }
  }
}

