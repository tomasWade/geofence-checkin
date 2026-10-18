package com.geofence.checkin.core.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.geofence.checkin.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统用户实体（逻辑删）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {
    private String username;
    private String passwordHash;
    private String realName;
    private String mobile;
    private Integer status;
    private Integer deleted;
}
