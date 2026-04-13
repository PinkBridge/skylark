package cn.skylark.permission.authorization.service;

import cn.skylark.permission.authorization.dto.LoginLogPageRequest;
import cn.skylark.permission.authorization.dto.LoginLogResponseDTO;
import cn.skylark.permission.authorization.entity.SysLoginLog;
import cn.skylark.permission.authorization.mapper.LoginLogMapper;
import cn.skylark.permission.utils.PageRequest;
import cn.skylark.permission.utils.PageResult;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 登录日志服务
 *
 * @author yaomianwei
 * @since 2025/12/5
 */
@Service
public class LoginLogService {

  @Resource
  private LoginLogMapper loginLogMapper;

  /**
   * 根据ID查询登录日志
   *
   * @param id 日志ID
   * @return 登录日志
   */
  public SysLoginLog get(Long id) {
    return loginLogMapper.selectById(id);
  }

  /**
   * 查询所有登录日志
   *
   * @return 登录日志列表
   */
  public List<SysLoginLog> list() {
    return loginLogMapper.selectAll();
  }

  /**
   * 插入登录日志
   *
   * @param loginLog 登录日志
   * @return 插入行数
   */
  public int create(SysLoginLog loginLog) {
    return loginLogMapper.insert(loginLog);
  }

  /**
   * 更新登录日志
   *
   * @param loginLog 登录日志
   * @return 更新行数
   */
  public int update(SysLoginLog loginLog) {
    return loginLogMapper.update(loginLog);
  }

  /**
   * 删除登录日志
   *
   * @param id 日志ID
   * @return 删除行数
   */
  public int delete(Long id) {
    return loginLogMapper.deleteById(id);
  }

  /**
   * 获取登录日志列表（DTO）
   *
   * @return 登录日志列表
   */
  public List<LoginLogResponseDTO> listDTO() {
    List<SysLoginLog> loginLogs = loginLogMapper.selectAll();
    return loginLogs.stream().map(this::convertToDTO).collect(Collectors.toList());
  }

  /**
   * 获取登录日志信息（DTO）
   *
   * @param id 日志ID
   * @return 登录日志信息
   */
  public LoginLogResponseDTO getDTO(Long id) {
    SysLoginLog loginLog = loginLogMapper.selectById(id);
    return loginLog != null ? convertToDTO(loginLog) : null;
  }

  /**
   * 分页查询登录日志列表（DTO）
   *
   * @param pageRequest 分页请求参数
   * @return 分页结果
   */
  public PageResult<LoginLogResponseDTO> pageDTO(PageRequest pageRequest) {
    List<SysLoginLog> records = loginLogMapper.selectPage(pageRequest.getOffset(), pageRequest.getLimit());
    Long total = loginLogMapper.countAll();
    List<LoginLogResponseDTO> dtoList = records.stream().map(this::convertToDTO).collect(Collectors.toList());
    return new PageResult<>(dtoList, total, pageRequest.getPage(), pageRequest.getSize());
  }

  /**
   * 分页查询登录日志列表（带条件，DTO）
   *
   * @param pageRequest 分页请求参数（包含搜索条件）
   * @return 分页结果
   */
  public PageResult<LoginLogResponseDTO> pageDTOWithCondition(LoginLogPageRequest pageRequest) {
    // 判断是否有搜索条件
    boolean hasCondition = StringUtils.hasText(pageRequest.getUsername()) ||
                           StringUtils.hasText(pageRequest.getLoginIp()) ||
                           StringUtils.hasText(pageRequest.getLoginStatus()) ||
                           StringUtils.hasText(pageRequest.getClientId()) ||
                           pageRequest.getLoginTimeStart() != null ||
                           pageRequest.getLoginTimeEnd() != null ||
                           pageRequest.getTenantId() != null;

    List<SysLoginLog> records;
    Long total;

    if (hasCondition) {
      // 使用带条件的查询
      records = loginLogMapper.selectPageWithCondition(
          pageRequest.getUsername(),
          pageRequest.getLoginIp(),
          pageRequest.getLoginStatus(),
          pageRequest.getLoginTimeStart(),
          pageRequest.getLoginTimeEnd(),
          pageRequest.getTenantId(),
          pageRequest.getClientId(),
          pageRequest.getOffset(),
          pageRequest.getLimit()
      );
      total = loginLogMapper.countWithCondition(
          pageRequest.getUsername(),
          pageRequest.getLoginIp(),
          pageRequest.getLoginStatus(),
          pageRequest.getLoginTimeStart(),
          pageRequest.getLoginTimeEnd(),
          pageRequest.getTenantId(),
          pageRequest.getClientId()
      );
    } else {
      // 使用无条件的查询
      records = loginLogMapper.selectPage(pageRequest.getOffset(), pageRequest.getLimit());
      total = loginLogMapper.countAll();
    }

    List<LoginLogResponseDTO> dtoList = records.stream().map(this::convertToDTO).collect(Collectors.toList());
    return new PageResult<>(dtoList, total, pageRequest.getPage(), pageRequest.getSize());
  }

  /**
   * 将SysLoginLog转换为LoginLogResponseDTO
   *
   * @param loginLog 登录日志实体
   * @return 登录日志响应DTO
   */
  private LoginLogResponseDTO convertToDTO(SysLoginLog loginLog) {
    if (loginLog == null) {
      return null;
    }
    LoginLogResponseDTO dto = new LoginLogResponseDTO();
    BeanUtils.copyProperties(loginLog, dto);
    return dto;
  }
}

