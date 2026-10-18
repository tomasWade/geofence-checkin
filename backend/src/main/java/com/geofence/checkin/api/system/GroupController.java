package com.geofence.checkin.api.system;

import com.geofence.checkin.common.response.ApiResult;
import com.geofence.checkin.core.system.audit.Audit;
import com.geofence.checkin.core.system.dto.GroupSaveReq;
import com.geofence.checkin.core.system.dto.GroupVO;
import com.geofence.checkin.core.system.service.GroupService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 分组管理接口：增删改查。
 */
@RestController
@RequestMapping("/api/v1/system/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @GetMapping
    @PreAuthorize("@ss.hasPermi('sys:group:list')")
    public ApiResult<List<GroupVO>> list() {
        return ApiResult.ok(groupService.listAll());
    }

    @PostMapping
    @PreAuthorize("@ss.hasPermi('sys:group:add')")
    @Audit(action = "group:create")
    public ApiResult<Long> create(@Valid @RequestBody GroupSaveReq req) {
        return ApiResult.ok(groupService.create(req));
    }

    @PutMapping
    @PreAuthorize("@ss.hasPermi('sys:group:edit')")
    @Audit(action = "group:update")
    public ApiResult<Void> update(@Valid @RequestBody GroupSaveReq req) {
        groupService.update(req);
        return ApiResult.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('sys:group:delete')")
    @Audit(action = "group:delete")
    public ApiResult<Void> delete(@PathVariable Long id) {
        groupService.delete(id);
        return ApiResult.ok();
    }
}
