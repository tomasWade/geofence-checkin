package com.geofence.checkin.core.system.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 分组视图对象。
 */
@Data
public class GroupVO {
    private Long id;
    private String name;
    private Long managerUserId;
    private String managerName;
    private List<Long> memberUserIds;
    private List<String> memberNames;
    private LocalDateTime createdAt;
}
