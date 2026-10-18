package com.geofence.checkin.core.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 平铺分组实体：组长 managerUserId 是 GROUP 数据范围的锚点之一。
 */
@Data
@TableName("sys_group")
public class SysGroup {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Long managerUserId;
    private LocalDateTime createdAt;
}
