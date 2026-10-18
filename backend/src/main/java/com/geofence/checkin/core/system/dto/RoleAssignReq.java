package com.geofence.checkin.core.system.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 给用户分配角色的请求体。
 */
@Data
public class RoleAssignReq {
    @NotNull
    private Long userId;
    @NotNull
    private List<Long> roleIds;
}
