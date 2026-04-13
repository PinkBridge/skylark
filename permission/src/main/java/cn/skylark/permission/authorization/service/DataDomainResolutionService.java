package cn.skylark.permission.authorization.service;

import cn.skylark.permission.authorization.dto.ResolvedDataScopeDTO;
import cn.skylark.permission.authorization.entity.SysDataDomain;
import cn.skylark.permission.authorization.entity.SysRole;
import cn.skylark.permission.authorization.entity.SysUser;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * 将用户角色绑定的数据域解析为 {@link ResolvedDataScopeDTO}（多域并集语义）。
 */
@Service
public class DataDomainResolutionService {

  private static final String TYPE_ALL = "ALL";
  private static final String TYPE_TENANT = "TENANT";
  private static final String TYPE_ORG_ALL = "ORG_ALL";
  private static final String TYPE_ORG_AND_CHILD = "ORG_AND_CHILD";
  private static final String TYPE_ORG_ONLY = "ORG_ONLY";
  private static final String TYPE_SELF = "SELF";
  private static final String TYPE_CUSTOM = "CUSTOM";

  @Resource
  private DataDomainService dataDomainService;

  @Resource
  private OrganizationService organizationService;

  /**
   * @param user   当前用户（需已加载 orgId、tenantId）
   * @param roles  用户角色列表
   */
  public ResolvedDataScopeDTO resolve(SysUser user, List<SysRole> roles) {
    ResolvedDataScopeDTO out = new ResolvedDataScopeDTO();
    if (user == null) {
      return out;
    }
    Long tenantId = user.getTenantId();
    Long userOrgId = user.getOrgId();

    List<SysDataDomain> distinct = collectDistinctDomains(roles);

    Set<Long> orgUnion = new LinkedHashSet<>();
    for (SysDataDomain d : distinct) {
      if (!isEnabled(d)) {
        continue;
      }
      out.getSourceDataDomainIds().add(d.getId());
      String type = d.getType() == null ? "" : d.getType().trim().toUpperCase();
      switch (type) {
        case TYPE_ALL:
          out.setAllPlatform(true);
          out.setWholeTenant(false);
          out.setSelfOnly(false);
          out.setHasCustom(false);
          out.setOrgIds(Collections.emptyList());
          return out;
        case TYPE_TENANT:
          out.setWholeTenant(true);
          break;
        case TYPE_SELF:
          out.setSelfOnly(true);
          break;
        case TYPE_ORG_ONLY:
          if (userOrgId != null && tenantId != null && sameTenant(d, tenantId)) {
            orgUnion.add(userOrgId);
          }
          break;
        case TYPE_ORG_AND_CHILD:
          if (userOrgId != null && tenantId != null && sameTenant(d, tenantId)) {
            orgUnion.add(userOrgId);
            orgUnion.addAll(organizationService.collectDirectChildOrgIds(userOrgId, tenantId));
          }
          break;
        case TYPE_ORG_ALL:
          if (userOrgId != null && tenantId != null && sameTenant(d, tenantId)) {
            orgUnion.addAll(organizationService.collectOrgIdsInSubtree(userOrgId, tenantId));
          }
          break;
        case TYPE_CUSTOM:
          out.setHasCustom(true);
          orgUnion.addAll(parseCustomOrgIds(d.getScopeValue()));
          break;
        default:
          break;
      }
    }

    if (out.isAllPlatform()) {
      return out;
    }
    out.setOrgIds(new ArrayList<>(new TreeSet<>(orgUnion)));
    return out;
  }

  private static boolean sameTenant(SysDataDomain d, Long tenantId) {
    return d.getTenantId() == null || tenantId.equals(d.getTenantId());
  }

  private static boolean isEnabled(SysDataDomain d) {
    return d.getEnabled() == null || Boolean.TRUE.equals(d.getEnabled());
  }

  private List<SysDataDomain> collectDistinctDomains(List<SysRole> roles) {
    if (roles == null || roles.isEmpty()) {
      return Collections.emptyList();
    }
    Map<Long, SysDataDomain> map = new LinkedHashMap<>();
    for (SysRole role : roles) {
      if (role == null || role.getId() == null) {
        continue;
      }
      for (SysDataDomain d : dataDomainService.listByRole(role.getId())) {
        if (d != null && d.getId() != null) {
          map.putIfAbsent(d.getId(), d);
        }
      }
    }
    return new ArrayList<>(map.values());
  }

  private static List<Long> parseCustomOrgIds(String scopeValue) {
    if (!StringUtils.hasText(scopeValue)) {
      return Collections.emptyList();
    }
    String trimmed = scopeValue.trim();
    try {
      if (trimmed.startsWith("[")) {
        JSONArray arr = JSON.parseArray(trimmed);
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++) {
          Long n = arr.getLong(i);
          if (n != null) {
            ids.add(n);
          }
        }
        return ids;
      }
    } catch (Exception ignored) {
      // ignore malformed JSON
    }
    return Collections.emptyList();
  }
}
