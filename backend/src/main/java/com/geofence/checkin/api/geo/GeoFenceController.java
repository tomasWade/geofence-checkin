package com.geofence.checkin.api.geo;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.geofence.checkin.common.response.ApiResult;
import com.geofence.checkin.core.geo.dto.FenceSaveReq;
import com.geofence.checkin.core.geo.entity.GeoFence;
import com.geofence.checkin.core.geo.service.GeoFenceService;
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

import java.util.List;

/**
 * 围栏管理接口：分页/全量启用列表/增删改（逻辑删）。
 */
@RestController
@RequestMapping("/api/v1/geo/fences")
@RequiredArgsConstructor
public class GeoFenceController {

    private final GeoFenceService fenceService;

    @GetMapping
    @PreAuthorize("@ss.hasPermi('fence:list')")
    public ApiResult<Page<GeoFence>> page(@RequestParam(defaultValue = "1") long page,
                                          @RequestParam(defaultValue = "20") long size,
                                          @RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) Integer status) {
        return ApiResult.ok(fenceService.page(page, size, keyword, status));
    }

    @GetMapping("/all")
    @PreAuthorize("@ss.hasPermi('fence:list')")
    public ApiResult<List<GeoFence>> listEnabled() {
        return ApiResult.ok(fenceService.listEnabled());
    }

    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('fence:list')")
    public ApiResult<GeoFence> get(@PathVariable Long id) {
        return ApiResult.ok(fenceService.get(id));
    }

    @PostMapping
    @PreAuthorize("@ss.hasPermi('fence:add')")
    public ApiResult<Long> create(@Valid @RequestBody FenceSaveReq req) {
        return ApiResult.ok(fenceService.create(req));
    }

    @PutMapping
    @PreAuthorize("@ss.hasPermi('fence:edit')")
    public ApiResult<Void> update(@Valid @RequestBody FenceSaveReq req) {
        fenceService.update(req);
        return ApiResult.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('fence:delete')")
    public ApiResult<Void> delete(@PathVariable Long id) {
        fenceService.delete(id);
        return ApiResult.ok();
    }
}
