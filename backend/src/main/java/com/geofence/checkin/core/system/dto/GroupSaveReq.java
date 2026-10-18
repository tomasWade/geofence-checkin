package com.geofence.checkin.core.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 分组新增/编辑请求体。
 */
@Data
public class GroupSaveReq {
    private Long id;
    @NotBlank
    @Size(max = 100)
    private String name;
    private Long managerUserId;
    private List<Long> memberUserIds;
}
