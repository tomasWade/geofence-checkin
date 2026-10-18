package com.geofence.checkin.api.checkin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.geofence.checkin.common.response.ApiResult;
import com.geofence.checkin.core.auth.security.CurrentUser;
import com.geofence.checkin.core.checkin.dto.CheckinReq;
import com.geofence.checkin.core.checkin.dto.CheckinResultVO;
import com.geofence.checkin.core.checkin.dto.ManualCheckinReq;
import com.geofence.checkin.core.checkin.dto.RecordVO;
import com.geofence.checkin.core.checkin.service.CheckinService;
import com.geofence.checkin.core.checkin.service.ManualCheckinService;
import com.geofence.checkin.core.checkin.service.RecordQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * 打卡接口：打卡（mock/真实双轨）、我的今日、记录查询（数据范围）、代打卡。
 */
@RestController
@RequestMapping("/api/v1/checkin")
@RequiredArgsConstructor
public class CheckinController {

    private final CheckinService checkinService;
    private final RecordQueryService recordQueryService;
    private final ManualCheckinService manualCheckinService;

    @PostMapping
    public ApiResult<CheckinResultVO> checkin(@Valid @RequestBody CheckinReq req) {
        return ApiResult.ok(checkinService.doCheckin(CurrentUser.requireUserId(), req));
    }

    @GetMapping("/today")
    public ApiResult<List<RecordVO>> today() {
        return ApiResult.ok(recordQueryService.myToday(CurrentUser.requireUserId()));
    }

    @GetMapping("/records")
    @PreAuthorize("@ss.hasPermi('record:list')")
    public ApiResult<Page<RecordVO>> records(@RequestParam(defaultValue = "1") long page,
                                             @RequestParam(defaultValue = "20") long size,
                                             @RequestParam(required = false)
                                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                             @RequestParam(required = false) Long userId) {
        return ApiResult.ok(recordQueryService.page(page, size, date, userId));
    }

    @GetMapping("/records/{id}")
    @PreAuthorize("@ss.hasPermi('record:list')")
    public ApiResult<RecordVO> detail(@PathVariable Long id) {
        return ApiResult.ok(recordQueryService.getDetail(id));
    }

    @PostMapping("/records/manual")
    @PreAuthorize("@ss.hasPermi('record:manualFix')")
    public ApiResult<Long> manual(@Valid @RequestBody ManualCheckinReq req) {
        return ApiResult.ok(manualCheckinService.create(req));
    }
}
