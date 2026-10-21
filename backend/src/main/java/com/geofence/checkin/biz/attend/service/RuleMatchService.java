package com.geofence.checkin.biz.attend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.geofence.checkin.biz.rule.entity.CheckinRule;
import com.geofence.checkin.biz.rule.entity.CheckinRuleScope;
import com.geofence.checkin.biz.rule.mapper.CheckinRuleMapper;
import com.geofence.checkin.biz.rule.mapper.CheckinRuleScopeMapper;
import com.geofence.checkin.core.system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 规则匹配：某用户某日适用的启用规则，优先级 USER > GROUP > ROLE > ALL，同级取 id 小者；生效日 bitmask 命中周一=bit0。
 * 用户角色/分组等上下文一律走 core 的 UserService，不直接依赖 core 的 Mapper。
 */
@Service
@RequiredArgsConstructor
public class RuleMatchService {

    private final CheckinRuleMapper ruleMapper;
    private final CheckinRuleScopeMapper scopeMapper;
    private final UserService userService;

    public Optional<CheckinRule> match(Long userId, LocalDate date) {
        int weekdayBit = 1 << (date.getDayOfWeek().getValue() - 1);
        List<CheckinRule> rules = ruleMapper.selectList(new LambdaQueryWrapper<CheckinRule>()
                .eq(CheckinRule::getStatus, 1)
                .orderByAsc(CheckinRule::getId));
        List<CheckinRule> effective = rules.stream()
                .filter(r -> (r.getEffectiveDays() & weekdayBit) != 0)
                .toList();
        if (effective.isEmpty()) {
            return Optional.empty();
        }

        List<Long> myRoleIds = userService.roleIds(userId);
        List<Long> myGroupIds = userService.groupIds(userId);

        CheckinRule best = pick(effective, "USER", userId, myRoleIds, myGroupIds);
        if (best == null) {
            best = pick(effective, "GROUP", userId, myRoleIds, myGroupIds);
        }
        if (best == null) {
            best = pick(effective, "ROLE", userId, myRoleIds, myGroupIds);
        }
        if (best == null) {
            best = pick(effective, "ALL", userId, myRoleIds, myGroupIds);
        }
        return Optional.ofNullable(best);
    }

    private CheckinRule pick(List<CheckinRule> rules, String bindType, Long userId,
                             List<Long> myRoleIds, List<Long> myGroupIds) {
        CheckinRule best = null;
        for (CheckinRule rule : rules) {
            if (!bindType.equals(rule.getBindType())) {
                continue;
            }
            boolean hit;
            if ("ALL".equals(bindType)) {
                hit = true;
            } else {
                List<CheckinRuleScope> scopes = scopeMapper.selectList(
                        new LambdaQueryWrapper<CheckinRuleScope>().eq(CheckinRuleScope::getRuleId, rule.getId()));
                hit = switch (bindType) {
                    case "USER" -> scopes.stream().anyMatch(s -> userId.equals(s.getUserId()));
                    case "GROUP" -> scopes.stream().anyMatch(s -> s.getGroupId() != null && myGroupIds.contains(s.getGroupId()));
                    case "ROLE" -> scopes.stream().anyMatch(s -> s.getRoleId() != null && myRoleIds.contains(s.getRoleId()));
                    default -> false;
                };
            }
            if (hit && (best == null || rule.getId() < best.getId())) {
                best = rule;
            }
        }
        return best;
    }
}
