package com.geofence.checkin.api.rule;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.geofence.checkin.biz.rule.dto.RuleSaveReq;
import com.geofence.checkin.biz.rule.entity.CheckinRule;
import com.geofence.checkin.biz.rule.service.RuleService;
import com.geofence.checkin.common.response.ApiResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 考勤规则管理接口。
 */
@RestController
@RequestMapping("/api/v1/rules")
@RequiredArgsConstructor
public class RuleController {

    private final RuleService ruleService;

    @GetMapping
    @PreAuthorize("@ss.hasPermi('rule:list')")
    public ApiResult<Page<CheckinRule>> page(@RequestParam(defaultValue = "1") long page,
                                             @RequestParam(defaultValue = "20") long size,
                                             @RequestParam(required = false) String keyword) {
        return ApiResult.ok(ruleService.page(page, size, keyword));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('rule:list')")
    public ApiResult<CheckinRule> get(@PathVariable Long id) {
        return ApiResult.ok(ruleService.get(id));
    }

    @PostMapping
    @PreAuthorize("@ss.hasPermi('rule:add')")
    public ApiResult<Long> create(@Valid @RequestBody RuleSaveReq req) {
        return ApiResult.ok(ruleService.create(req));
    }

    @PutMapping
    @PreAuthorize("@ss.hasPermi('rule:edit')")
    public ApiResult<Void> update(@Valid @RequestBody RuleSaveReq req) {
        ruleService.update(req);
        return ApiResult.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('rule:delete')")
    public ApiResult<Void> delete(@PathVariable Long id) {
        ruleService.delete(id);
        return ApiResult.ok();
    }
}
