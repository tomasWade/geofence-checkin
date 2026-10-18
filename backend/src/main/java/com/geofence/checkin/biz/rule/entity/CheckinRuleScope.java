package com.geofence.checkin.biz.rule.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 规则绑定对象行：按 bindType 写入角色/分组/用户 ID。
 */
@Data
@TableName("checkin_rule_scope")
public class CheckinRuleScope {
    private Long ruleId;
    private Long roleId;
    private Long groupId;
    private Long userId;
}
