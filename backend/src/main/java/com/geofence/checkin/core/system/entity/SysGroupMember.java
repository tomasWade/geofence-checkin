package com.geofence.checkin.core.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 分组成员关联实体，一人可属多组。
 */
@Data
@TableName("sys_group_member")
public class SysGroupMember implements Serializable {
    private Long groupId;
    private Long userId;
}
