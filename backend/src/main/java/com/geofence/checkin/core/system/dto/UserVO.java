package com.geofence.checkin.core.system.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户视图对象（不含密码哈希）。
 */
@Data
public class UserVO {
    private Long id;
    private String username;
    private String realName;
    private String mobile;
    private Integer status;
    private LocalDateTime createdAt;
    private List<Long> roleIds;
    private List<String> roleCodes;
}
