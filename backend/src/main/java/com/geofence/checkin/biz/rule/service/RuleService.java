package com.geofence.checkin.biz.rule.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.geofence.checkin.biz.rule.dto.RuleSaveReq;
import com.geofence.checkin.biz.rule.entity.CheckinRule;
import com.geofence.checkin.biz.rule.entity.CheckinRuleScope;
import com.geofence.checkin.biz.rule.mapper.CheckinRuleMapper;
import com.geofence.checkin.biz.rule.mapper.CheckinRuleScopeMapper;
import com.geofence.checkin.common.exception.BusinessException;
import com.geofence.checkin.core.geo.entity.GeoFence;
import com.geofence.checkin.core.geo.mapper.GeoFenceMapper;
import com.geofence.checkin.core.system.audit.Audit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 规则管理：CRUD + 绑定对象维护；校验围栏存在、不支持跨天班次、非全员规则必须绑定对象。
 */
@Service
@RequiredArgsConstructor
public class RuleService {

    private final CheckinRuleMapper ruleMapper;
    private final CheckinRuleScopeMapper scopeMapper;
    private final GeoFenceMapper fenceMapper;

    public Page<CheckinRule> page(long page, long size, String keyword) {
        Page<CheckinRule> result = ruleMapper.selectPage(Page.of(page, size),
                new LambdaQueryWrapper<CheckinRule>()
                        .and(keyword != null && !keyword.isBlank(),
                                w -> w.like(CheckinRule::getName, keyword))
                        .orderByDesc(CheckinRule::getId));
        result.getRecords().forEach(this::fillScopes);
        return result;
    }

    public CheckinRule get(Long id) {
        CheckinRule rule = ruleMapper.selectById(id);
        if (rule == null) {
            throw new BusinessException("规则不存在");
        }
        fillScopes(rule);
        return rule;
    }

    @Audit(action = "rule:create")
    @Transactional
    public Long create(RuleSaveReq req) {
        validate(req);
        CheckinRule rule = new CheckinRule();
        copyFields(req, rule);
        rule.setDeleted(0);
        rule.setStatus(req.getStatus() == null ? 1 : req.getStatus());
        ruleMapper.insert(rule);
        saveScopes(rule.getId(), req);
        return rule.getId();
    }

    @Audit(action = "rule:update")
    @Transactional
    public void update(RuleSaveReq req) {
        if (req.getId() == null) {
            throw new BusinessException("规则 id 不能为空");
        }
        CheckinRule rule = ruleMapper.selectById(req.getId());
        if (rule == null) {
            throw new BusinessException("规则不存在");
        }
        validate(req);
        copyFields(req, rule);
        ruleMapper.updateById(rule);
        scopeMapper.delete(new LambdaQueryWrapper<CheckinRuleScope>()
                .eq(CheckinRuleScope::getRuleId, rule.getId()));
        saveScopes(rule.getId(), req);
    }

    @Audit(action = "rule:delete")
    @Transactional
    public void delete(Long id) {
        CheckinRule rule = ruleMapper.selectById(id);
        if (rule == null) {
            return;
        }
        ruleMapper.deleteById(id);
        scopeMapper.delete(new LambdaQueryWrapper<CheckinRuleScope>()
                .eq(CheckinRuleScope::getRuleId, id));
    }

    private void validate(RuleSaveReq req) {
        GeoFence fence = fenceMapper.selectById(req.getFenceId());
        if (fence == null) {
            throw new BusinessException("绑定的围栏不存在");
        }
        if (req.getWorkEndSec() <= req.getWorkStartSec()) {
            throw new BusinessException("下班时刻必须晚于上班时刻（暂不支持跨天班次）");
        }
        String bindType = req.getBindType() == null ? "ALL" : req.getBindType();
        boolean hasScope = (req.getRoleIds() != null && !req.getRoleIds().isEmpty())
                || (req.getGroupIds() != null && !req.getGroupIds().isEmpty())
                || (req.getUserIds() != null && !req.getUserIds().isEmpty());
        if (!"ALL".equals(bindType) && !hasScope) {
            throw new BusinessException("非全员规则必须指定绑定对象");
        }
    }

    private void copyFields(RuleSaveReq req, CheckinRule rule) {
        rule.setName(req.getName());
        rule.setFenceId(req.getFenceId());
        rule.setWorkStartSec(req.getWorkStartSec());
        rule.setWorkEndSec(req.getWorkEndSec());
        rule.setLateToleranceSec(req.getLateToleranceSec() == null ? 0 : req.getLateToleranceSec());
        rule.setEffectiveDays(req.getEffectiveDays() == null ? 127 : req.getEffectiveDays());
        rule.setBindType(req.getBindType() == null ? "ALL" : req.getBindType());
        if (req.getStatus() != null) {
            rule.setStatus(req.getStatus());
        }
    }

    private void saveScopes(Long ruleId, RuleSaveReq req) {
        List<CheckinRuleScope> scopes = new ArrayList<>();
        if (req.getRoleIds() != null) {
            req.getRoleIds().forEach(id -> addScope(scopes, ruleId, id, null, null));
        }
        if (req.getGroupIds() != null) {
            req.getGroupIds().forEach(id -> addScope(scopes, ruleId, null, id, null));
        }
        if (req.getUserIds() != null) {
            req.getUserIds().forEach(id -> addScope(scopes, ruleId, null, null, id));
        }
        scopes.forEach(scopeMapper::insert);
    }

    private void addScope(List<CheckinRuleScope> scopes, Long ruleId, Long roleId, Long groupId, Long userId) {
        CheckinRuleScope s = new CheckinRuleScope();
        s.setRuleId(ruleId);
        s.setRoleId(roleId);
        s.setGroupId(groupId);
        s.setUserId(userId);
        scopes.add(s);
    }

    private void fillScopes(CheckinRule rule) {
        List<CheckinRuleScope> scopes = scopeMapper.selectList(
                new LambdaQueryWrapper<CheckinRuleScope>().eq(CheckinRuleScope::getRuleId, rule.getId()));
        rule.setScopes(scopes);
    }
}
