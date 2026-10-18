package com.geofence.checkin.core.geo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.geofence.checkin.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 圆形围栏实体：圆心经纬度 + 半径（米），逻辑删。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("geo_fence")
public class GeoFence extends BaseEntity {
    private String name;
    private BigDecimal lng;
    private BigDecimal lat;
    private Integer radiusM;
    private String fenceType;
    private Integer status;
    private Integer deleted;
    private Long createdBy;
}
