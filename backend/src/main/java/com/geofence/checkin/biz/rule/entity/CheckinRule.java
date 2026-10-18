package com.geofence.checkin.biz.rule.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.geofence.checkin.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 考勤规则实体：固定班次（上/下班秒偏移 + 迟到宽限 + 生效日 bitmask）+ 绑定方式（ALL/ROLE/GROUP/USER）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("checkin_rule")
public class CheckinRule extends BaseEntity {
    private String name;
    private Long fenceId;
    private Integer workStartSec;
    private Integer workEndSec;
    private Integer lateToleranceSec;
    private Integer effectiveDays;
    private String bindType;
    private Integer status;
    private Integer deleted;

    @TableField(exist = false)
    private List<CheckinRuleScope> scopes;
}
