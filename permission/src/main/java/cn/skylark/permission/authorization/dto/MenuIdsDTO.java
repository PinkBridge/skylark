package cn.skylark.permission.authorization.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 菜单ID数组DTO
 *
 * @author yaomianwei
 * @since 2025/11/3
 */
@Data
public class MenuIdsDTO {
  /**
   * 菜单ID数组
   */
  @JsonProperty("menuIds")
  private List<Long> menuIds;
}

