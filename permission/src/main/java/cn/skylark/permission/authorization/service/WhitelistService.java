package cn.skylark.permission.authorization.service;

import cn.skylark.permission.authorization.dto.UpdateWhitelistDTO;
import cn.skylark.permission.authorization.dto.WhitelistPageRequest;
import cn.skylark.permission.authorization.dto.WhitelistResponseDTO;
import cn.skylark.permission.authorization.entity.SysWhitelist;
import cn.skylark.permission.authorization.mapper.WhitelistMapper;
import cn.skylark.permission.utils.PageRequest;
import cn.skylark.permission.utils.PageResult;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 白名单服务
 *
 * @author yaomianwei
 * @since 2025/11/3
 */
@Service
public class WhitelistService {

  @Resource
  private WhitelistMapper whitelistMapper;

  public SysWhitelist get(Long id) {
    return whitelistMapper.selectById(id);
  }

  public List<SysWhitelist> list() {
    return whitelistMapper.selectAll();
  }

  /**
   * 获取所有启用的白名单
   */
  public List<SysWhitelist> listEnabled() {
    return whitelistMapper.selectEnabled();
  }

  public int create(SysWhitelist whitelist) {
    return whitelistMapper.insert(whitelist);
  }

  public int update(SysWhitelist whitelist) {
    return whitelistMapper.update(whitelist);
  }

  public int delete(Long id) {
    return whitelistMapper.deleteById(id);
  }

  /**
   * 获取白名单列表（DTO）
   *
   * @return 白名单列表
   */
  public List<WhitelistResponseDTO> listDTO() {
    List<SysWhitelist> whitelists = whitelistMapper.selectAll();
    return whitelists.stream().map(this::convertToDTO).collect(Collectors.toList());
  }

  /**
   * 获取白名单信息（DTO）
   *
   * @param id 白名单ID
   * @return 白名单信息
   */
  public WhitelistResponseDTO getDTO(Long id) {
    SysWhitelist whitelist = whitelistMapper.selectById(id);
    return whitelist != null ? convertToDTO(whitelist) : null;
  }

  /**
   * 分页查询白名单列表（DTO）
   *
   * @param pageRequest 分页请求参数
   * @return 分页结果
   */
  public PageResult<WhitelistResponseDTO> pageDTO(PageRequest pageRequest) {
    List<SysWhitelist> records = whitelistMapper.selectPage(pageRequest.getOffset(), pageRequest.getLimit());
    Long total = whitelistMapper.countAll();
    List<WhitelistResponseDTO> dtoList = records.stream().map(this::convertToDTO).collect(Collectors.toList());
    return new PageResult<>(dtoList, total, pageRequest.getPage(), pageRequest.getSize());
  }

  /**
   * 分页查询白名单列表（带条件，DTO）
   *
   * @param pageRequest 分页请求参数（包含搜索条件）
   * @return 分页结果
   */
  public PageResult<WhitelistResponseDTO> pageDTOWithCondition(WhitelistPageRequest pageRequest) {
    // 判断是否有搜索条件
    boolean hasCondition = StringUtils.hasText(pageRequest.getMethod()) ||
                           StringUtils.hasText(pageRequest.getPath()) ||
                           StringUtils.hasText(pageRequest.getRemark()) ||
                           pageRequest.getEnabled() != null ||
                           pageRequest.getCreateTime() != null;

    List<SysWhitelist> records;
    Long total;

    if (hasCondition) {
      // 使用带条件的查询
      records = whitelistMapper.selectPageWithCondition(
          pageRequest.getMethod(),
          pageRequest.getPath(),
          pageRequest.getRemark(),
          pageRequest.getEnabled(),
          pageRequest.getCreateTime(),
          pageRequest.getOffset(),
          pageRequest.getLimit()
      );
      total = whitelistMapper.countWithCondition(
          pageRequest.getMethod(),
          pageRequest.getPath(),
          pageRequest.getRemark(),
          pageRequest.getEnabled(),
          pageRequest.getCreateTime()
      );
    } else {
      // 使用无条件的查询
      records = whitelistMapper.selectPage(pageRequest.getOffset(), pageRequest.getLimit());
      total = whitelistMapper.countAll();
    }

    List<WhitelistResponseDTO> dtoList = records.stream().map(this::convertToDTO).collect(Collectors.toList());
    return new PageResult<>(dtoList, total, pageRequest.getPage(), pageRequest.getSize());
  }

  /**
   * 更新白名单信息
   *
   * @param whitelistId 白名单ID
   * @param dto         更新白名单信息DTO
   * @return 更新行数
   */
  public int updateWhitelistInfo(Long whitelistId, UpdateWhitelistDTO dto) {
    return whitelistMapper.updateWhitelistInfo(whitelistId, dto);
  }

  /**
   * 将SysWhitelist转换为WhitelistResponseDTO
   *
   * @param whitelist 白名单实体
   * @return 白名单响应DTO
   */
  private WhitelistResponseDTO convertToDTO(SysWhitelist whitelist) {
    if (whitelist == null) {
      return null;
    }
    WhitelistResponseDTO dto = new WhitelistResponseDTO();
    BeanUtils.copyProperties(whitelist, dto);
    return dto;
  }
}

