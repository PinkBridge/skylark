package cn.skylark.permission.authorization.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 将用户多角色下的数据域规则按「并集」解析后的结果，供业务服务或前端做行级过滤参考。
 * <p>
 * 语义优先级：{@code allPlatform} 为 true 时忽略其余字段；否则若 {@code wholeTenant} 为 true 表示本租户内不限组织；
 * 否则用 {@code orgIds} 与 {@code selfOnly} 做并集（用户可见 = 本人数据 ∪ 所列组织范围内数据）。
 */
@Data
public class ResolvedDataScopeDTO {

  private String version = "1.0";

  /** 是否等价于全平台（任一绑定数据域 type=ALL） */
  private boolean allPlatform;

  /** 是否本租户全量（任一 type=TENANT） */
  private boolean wholeTenant;

  /** 是否包含「仅本人」（任一 type=SELF） */
  private boolean selfOnly;

  /** 解析得到的组织 ID 并集（ORG_* / CUSTOM 中 JSON 数组解析出的 id） */
  private List<Long> orgIds = new ArrayList<>();

  /** 是否存在 CUSTOM 类型（业务侧需结合原始数据域配置自行解释） */
  private boolean hasCustom;

  /** 参与解析的数据域 id（便于审计） */
  private List<Long> sourceDataDomainIds = new ArrayList<>();
}
