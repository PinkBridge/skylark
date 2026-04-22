package cn.skylark.permission.authorization.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysMenu {
    private Long id;
    private Long parentId;
    private String name;
    /**
     * JSON object: zh / en (and optional zh_CN, en_US) for localized display.
     */
    private String nameI18n;
    private String path;
    private String icon;
    private Integer sort;
    private Boolean hidden;
    private String type;
    private String permlabel;
    private String moduleKey;
    /**
     * 所属前端应用，如 permission-web、iot-web
     */
    private String appCode;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

