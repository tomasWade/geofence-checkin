package com.geofence.checkin.core.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 角色-权限码关联实体。
 */
@Data
@TableName("sys_role_permission")
public class SysRolePermission implements Serializable {
    private Long roleId;
    private Long permissionId;
}
